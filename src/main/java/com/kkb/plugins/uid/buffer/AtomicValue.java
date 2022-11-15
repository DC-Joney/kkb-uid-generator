/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

/**
 * ring buffer 值（前后填充，防止 value 值 CPU 三级缓存伪共享）
 * <p>
 * 每一个 ring buffer 的 slot 对应一个 AtomicValue
 *
 * @author ztkool
 * @since
 */
public class AtomicValue {

    private long p1 = 7L;
    private long p2 = 7L;
    private long p3 = 7L;
    private long p4 = 7L;
    private long p5 = 7L;
    private long p6 = 7L;
    private long p7 = 7L;
    private final AtomicLong value;
    private long q1 = 7L;
    private long q2 = 7L;
    private long q3 = 7L;
    private long q4 = 7L;
    private long q5 = 7L;
    private long q6 = 7L;
    private long q7 = 7L;

    public AtomicValue(long initialValue) {
        value = new AtomicLong(initialValue);
    }

    public final void set(long newValue) {
        value.set(newValue);
    }

    public final long get() {
        return value.get();
    }

    public final long incrementAndGet() {
        return value.incrementAndGet();
    }

    public final long updateAndGet(LongUnaryOperator updateFunction) {
        return value.updateAndGet(updateFunction);
    }

}
