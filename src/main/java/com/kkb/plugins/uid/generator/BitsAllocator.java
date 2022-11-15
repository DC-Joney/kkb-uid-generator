/*
 * Copyright
 */
package com.kkb.plugins.uid.generator;

import com.kkb.plugins.uid.utils.Assert;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 类说明
 *
 * @author ztkool
 * @since
 */
public class BitsAllocator {

    /**
     * 使用 64 位 long 类型长度
     */
    public static final int TOTAL_BITS = 1 << 6;

    /**
     * 标识位长度，1 bit（第一位 0，表示正数）
     */
    private int signBits = 1;

    /**
     * 时间戳长度（bit）
     */
    private final int timestampBits;

    /**
     * workerId 长度（bit）
     */
    private final int workerIdBits;

    /**
     * 序列号长度（bit）
     */
    private final int sequenceBits;

    /**
     * 最大增量（秒），（当前时间 - 初始化时间）（seconds） = maxDeltaSeconds
     */
    private final long maxDeltaSeconds;

    /**
     * 最大 workerId
     */
    private final long maxWorkerId;

    /**
     * 最大序列号
     */
    private final long maxSequence;

    /**
     * 时间戳偏移量（timestampShift = workerIdShift + workerId bit 长度）
     */
    private final int timestampShift;

    /**
     * workerId 偏移量（workerIdShift = 序列号 bit 长度）
     */
    private final int workerIdShift;

    public BitsAllocator(int timestampBits, int workerIdBits, int sequenceBits) {
        int allocateTotalBits = signBits + timestampBits + workerIdBits + sequenceBits;
        Assert.isTrue(allocateTotalBits == TOTAL_BITS, "allocate not enough 64 bits");

        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        this.maxDeltaSeconds = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        this.timestampShift = workerIdBits + sequenceBits;
        this.workerIdShift = sequenceBits;
    }

    /**
     * 生成 uid
     *
     * @param deltaSeconds
     * @param workerId
     * @param sequence
     * @return
     */
    public long allocate(long deltaSeconds, long workerId, long sequence) {
        return (deltaSeconds << timestampShift) | (workerId << workerIdShift) | sequence;
    }

    public int getSignBits() {
        return signBits;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public int getWorkerIdBits() {
        return workerIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public long getMaxDeltaSeconds() {
        return maxDeltaSeconds;
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public int getTimestampShift() {
        return timestampShift;
    }

    public int getWorkerIdShift() {
        return workerIdShift;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}