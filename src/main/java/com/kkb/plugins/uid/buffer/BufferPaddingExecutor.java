/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer;

import com.kkb.plugins.uid.buffer.provider.BufferedUidProvider;
import com.kkb.plugins.uid.utils.Assert;
import com.kkb.plugins.uid.utils.thread.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * buffer 填充
 *
 * @author ztkool
 * @since
 */
public class BufferPaddingExecutor {

    private static final Logger log = LoggerFactory.getLogger(RingBuffer.class);

    public static final String WORKER_NAME = "uid-generator-padding-worker";
    public static final String SCHEDULE_NAME = "uid-generator-padding-schedule";

    private static final long DEFAULT_SCHEDULE_INTERVAL = 5 * 60L;

    /**
     * 填充任务执行标志（用来防止手动触发和定时触发冲突）
     */
    private final AtomicBoolean running;

    /**
     * 进行 uid 预分配缓存时，预先使用未来时间，此处记录已经使用了的时间（秒）
     */
    private final AtomicLong lastSecond;

    /**
     * ring buffer
     */
    private final RingBuffer ringBuffer;

    /**
     * buffer uid 生成器
     */
    private final BufferedUidProvider bufferedUidProvider;

    /**
     * buffer 填充执行线程池，主动触发式使用
     */
    private final ExecutorService bufferPadExecutors;

    /**
     * 定时 buffer 填充执行线程池
     */
    private final ScheduledExecutorService bufferPadSchedule;

    /**
     * 定时任务，默认五分钟执行一次，用来主动填充 buffer
     */
    private long scheduleInterval = DEFAULT_SCHEDULE_INTERVAL;

    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider uidProvider) {
        this(ringBuffer, uidProvider, true);
    }

    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider bufferedUidProvider, boolean usingSchedule) {
        this.running = new AtomicBoolean(false);
        this.lastSecond = new AtomicLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        this.ringBuffer = ringBuffer;
        this.bufferedUidProvider = bufferedUidProvider;

        // 初始化线程池（用于手动触发填充 buffer 时使用）
        int cores = Runtime.getRuntime().availableProcessors();
        this.bufferPadExecutors = ThreadPoolFactory.newFixedThreadPool(cores * 2, WORKER_NAME);

        // 初始化定时调度线程池
        if (usingSchedule) {
            this.bufferPadSchedule = ThreadPoolFactory.newSingleThreadScheduledExecutor(SCHEDULE_NAME);
        } else {
            this.bufferPadSchedule = null;
        }
    }

    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }

    /**
     * 异步执行填充（消费时，发现没有可用元素，被动触发）
     */
    public void asyncPadding() {
        bufferPadExecutors.submit(this::paddingBuffer);
    }

    /**
     * 分配 buffer
     */
    public void paddingBuffer() {
        log.info("uid generator log. Ready to padding buffer lastSecond:{}. {}", lastSecond.get(), ringBuffer);

        // 检查是否正在执行中，如果正在执行中，则此次不再执行
        if (!running.compareAndSet(false, true)) {
            log.info("uid generator log. Padding buffer is still running. {}", ringBuffer);
            return;
        }

        // 后续操作不需要控制线程，通过上面的 running cas 判断，已经保证下面的操作此时此刻只一个线程处理
        boolean isFull = false;
        while (!isFull) {
            // 生成一批 uid 并填充，知道填充满为止
            List<Long> uidList = bufferedUidProvider.provide(lastSecond.incrementAndGet());
            for (Long uid : uidList) {
                isFull = !ringBuffer.put(uid);
                if (isFull) {
                    break;
                }
            }
        }

        // running 设置为 false
        running.compareAndSet(true, false);
        log.info("uid generator log. End to padding buffer lastSecond:{}. {}", lastSecond.get(), ringBuffer);
    }

    public boolean isRunning() {
        return running.get();
    }

    /**
     * 启动器，默认五分钟执行一次 buffer 填充
     */
    public final void start() {
        if (bufferPadSchedule != null) {
            bufferPadSchedule.scheduleWithFixedDelay(this::paddingBuffer, scheduleInterval, scheduleInterval, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        if (!bufferPadExecutors.isShutdown()) {
            bufferPadExecutors.shutdownNow();
        }

        if (bufferPadSchedule != null && !bufferPadSchedule.isShutdown()) {
            bufferPadSchedule.shutdownNow();
        }
    }

}
