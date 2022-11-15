/*
 * Copyright
 */
package com.kkb.plugins.uid.generator;

import com.kkb.plugins.uid.exception.UidGenerateException;
import com.kkb.plugins.uid.utils.Assert;
import com.kkb.plugins.uid.utils.DateUtil;
import com.kkb.plugins.uid.worker.WorkerIdAssigner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * default uid generator
 * <p>
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 * 1bit          28bits              22bits         13bits
 *
 * @author ztkool
 * @since
 */
public class DefaultUidGenerator implements UidGenerator<Long>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DefaultUidGenerator.class);

    /**
     * 28 位 时间戳
     */
    protected int timeBits = 28;

    /**
     * 22 位 workerId
     */
    protected int workerBits = 22;

    /**
     * 13 位 序列号，最大 2 ^ 13 = 8192
     */
    protected int seqBits = 13;

    /**
     * 初始化时间戳
     */
    protected String epochStr = "2021-11-11";
    protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(DateUtil.parseByDayPattern(epochStr).getTime());

    /**
     * bit 分配器
     */
    protected BitsAllocator bitsAllocator;

    /**
     * workerId
     */
    protected long workerId;

    /**
     * 起始 序列号
     */
    protected long sequence = 0L;

    /**
     * 最后生成 uid 的时间（秒）
     */
    protected long lastSecond = -1L;

    /**
     * Spring property
     */
    protected WorkerIdAssigner workerIdAssigner;

    /**
     * 是否循环使用 workerId
     * <p>
     * 数据库 id 超过 2 ^ workerIdBit 后 （2 ^ 22 = 4194304），所有服务将不可用，使用循环方式，即取 workerId & （2 ^ 22）
     */
    protected boolean useCycleWorkerId;

    public DefaultUidGenerator(WorkerIdAssigner workerIdAssigner) {
        Assert.notNull(workerIdAssigner, "workerIdAssigner is null.");
        this.workerIdAssigner = workerIdAssigner;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);
        workerId = workerIdAssigner.assignWorkerId();
        workerId = checkWorkerId(workerId);
        log.info("uid generator log. Initialized bits(1, {}, {}, {}) for workerID: {}", timeBits, workerBits, seqBits, workerId);
    }

    /**
     * 检查 workerId
     *
     * @return
     */
    private long checkWorkerId(long workerId) {
        if (useCycleWorkerId) {
            return workerId & bitsAllocator.getMaxWorkerId();
        }
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            throw new RuntimeException("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
        }
        return workerId;
    }

    @Override
    public Long get() throws UidGenerateException {
        try {
            return nextId();
        } catch (Exception e) {
            log.error("uid generator error log. Generate unique id exception. ", e);
            throw new UidGenerateException(e);
        }
    }

    @Override
    public String parse(Long uid) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long signBits = bitsAllocator.getSignBits();
        long timestampBits = bitsAllocator.getTimestampBits();
        long workerIdBits = bitsAllocator.getWorkerIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();

        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long workerId = (uid << (timestampBits + signBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

        Date thatTime = new Date(TimeUnit.SECONDS.toMillis(epochSeconds + deltaSeconds));
        String thatTimeStr = DateUtil.formatByDateTimePattern(thatTime);

        return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                uid, thatTimeStr, workerId, sequence);
    }

    /**
     * 生成 uid
     *
     * @return
     */
    protected synchronized long nextId() {

        // 获取当前时间戳（秒）
        long currentSecond = getCurrentSecond();

        if (currentSecond < lastSecond) {
            // 时钟回拨情况，抛出异常
            long refusedSeconds = lastSecond - currentSecond;
            throw new UidGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
        }

        // 如果当前时间戳和上次生成 uid 使用的时间戳相同，则序列号递增，否则重置序列号
        if (currentSecond == lastSecond) {
            // 通过 & 运算判断序号是否已经达到当前时间戳下最大值
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            if (sequence == 0) {
                // 序列号已达到最大值，通过自旋，将当前时间戳更新到下一秒
                currentSecond = getNextSecond(lastSecond);
            }
        } else {
            sequence = 0L;
        }

        // 存储最后使用的时间戳，用于判断时钟回拨和序号递增
        lastSecond = currentSecond;

        // 生成 uid
        return bitsAllocator.allocate(currentSecond - epochSeconds, workerId, sequence);
    }

    /**
     * 自旋获取下一秒时间戳
     */
    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }
        return timestamp;
    }

    /**
     * 获取当前 second
     */
    private long getCurrentSecond() {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
            throw new UidGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    public void setTimeBits(int timeBits) {
        if (timeBits > 0) {
            this.timeBits = timeBits;
        }
    }

    public void setWorkerBits(int workerBits) {
        if (workerBits > 0) {
            this.workerBits = workerBits;
        }
    }

    public void setSeqBits(int seqBits) {
        if (seqBits > 0) {
            this.seqBits = seqBits;
        }
    }

    public void setEpochStr(String epochStr) {
        if (StringUtils.isNotBlank(epochStr)) {
            this.epochStr = epochStr;
            this.epochSeconds = TimeUnit.MILLISECONDS.toSeconds(DateUtil.parseByDayPattern(epochStr).getTime());
        }
    }

    public void setUseCycleWorkerId(boolean useCycleWorkerId) {
        this.useCycleWorkerId = useCycleWorkerId;
    }
}
