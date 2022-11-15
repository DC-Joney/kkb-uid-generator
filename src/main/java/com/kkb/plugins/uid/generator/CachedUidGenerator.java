/*
 * Copyright
 */
package com.kkb.plugins.uid.generator;

import com.kkb.plugins.uid.buffer.BufferPaddingExecutor;
import com.kkb.plugins.uid.buffer.RingBuffer;
import com.kkb.plugins.uid.buffer.provider.BufferedUidProvider;
import com.kkb.plugins.uid.buffer.provider.DefaultBufferedUidProvider;
import com.kkb.plugins.uid.buffer.rejected.RejectedPutBufferHandler;
import com.kkb.plugins.uid.buffer.rejected.RejectedTakeBufferHandler;
import com.kkb.plugins.uid.exception.UidGenerateException;
import com.kkb.plugins.uid.worker.WorkerIdAssigner;
import com.kkb.plugins.uid.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * 缓存 uid 生成器
 *
 * @author ztkool
 * @since
 */
public class CachedUidGenerator extends DefaultUidGenerator implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(CachedUidGenerator.class);

    private static final int DEFAULT_BOOST_POWER = 3;

    /**
     * slots 长度 (maxSequence + 1) << boostPower
     * <p>
     * 如选取 sequenceBit = 13，可表示的最大序列号为 maxSequence = ~(-1L << 13) = 8195
     * 若 boostPower = 3，则 buffer 长度为 (8195 + 1) << 3 = 65536
     * 8195 + 1  = 8196 为 2 的指数倍
     */
    private int boostPower = DEFAULT_BOOST_POWER;

    /**
     * 填充因子，使用 ring buffer 的默认填充因子（50）
     */
    private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;

    /**
     * 定时调度时间间隔
     */
    private Long scheduleInterval;

    /**
     * 写失败策略
     */
    private RejectedPutBufferHandler rejectedPutBufferHandler;

    /**
     * 读失败策略
     */
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;

    /**
     * ring buffer
     */
    private RingBuffer ringBuffer;

    /**
     * ring buffer 填充执行器，用来执行填充任务
     */
    private BufferPaddingExecutor bufferPaddingExecutor;

    /**
     * uid provider 初始化时，缓存一批 uid
     */
    private BufferedUidProvider bufferedUidProvider;

    public CachedUidGenerator(WorkerIdAssigner workerIdAssigner) {
        super(workerIdAssigner);
    }

    @Override
    public Long get() {
        try {
            return ringBuffer.take();
        } catch (Exception e) {
            log.error("uid generator error log. Generate unique id exception. ", e);
            throw new UidGenerateException(e);
        }
    }

    @Override
    public String parse(Long uid) {
        return super.parse(uid);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化 BitsAllocator 对象和 workerId
        super.afterPropertiesSet();
        // 初始化 ring buffer
        this.initRingBuffer();
        log.info("uid generator log. Initialized RingBuffer successfully.");
    }

    /**
     * 初始化 RingBuffer、BufferPaddingExecutor
     */
    private void initRingBuffer() {
        // 初始化 RingBuffer
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        this.ringBuffer = new RingBuffer(bufferSize, paddingFactor);
        log.info("uid generator log. Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, paddingFactor);

        // 初始化 BufferPaddingExecutor
        boolean usingSchedule = (scheduleInterval != null);
        // 初始化 BufferedUidProvider
        this.bufferedUidProvider = new DefaultBufferedUidProvider(bitsAllocator, epochSeconds, workerId);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, bufferedUidProvider, usingSchedule);
        if (usingSchedule) {
            // 设置是否启用定时调度更新
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        log.info("uid generator log. Initialized BufferPaddingExecutor. Using schdule:{}, interval:{}", usingSchedule, scheduleInterval);

        if (rejectedPutBufferHandler != null) {
            // 设置拒绝写策略
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            // 设置拒绝读策略
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }

        // 初始化时填充 ring buffer
        bufferPaddingExecutor.paddingBuffer();

        // 启动定时任务
        bufferPaddingExecutor.start();
    }

    public void setBoostPower(int boostPower) {
        Assert.isTrue(boostPower > 0, "Boost power must be positive!");
        this.boostPower = boostPower;
    }

    public void setRejectedPutBufferHandler(RejectedPutBufferHandler rejectedPutBufferHandler) {
        Assert.notNull(rejectedPutBufferHandler, "RejectedPutBufferHandler can't be null!");
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
    }

    public void setRejectedTakeBufferHandler(RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        Assert.notNull(rejectedTakeBufferHandler, "RejectedTakeBufferHandler can't be null!");
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
    }

    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }

    @Override
    public void destroy() throws Exception {
        bufferPaddingExecutor.shutdown();
    }

}
