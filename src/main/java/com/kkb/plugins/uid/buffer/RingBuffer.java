/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer;

import com.kkb.plugins.uid.buffer.rejected.DefaultRejectedPutBufferHandler;
import com.kkb.plugins.uid.buffer.rejected.DefaultRejectedTakeBufferHandler;
import com.kkb.plugins.uid.buffer.rejected.RejectedPutBufferHandler;
import com.kkb.plugins.uid.buffer.rejected.RejectedTakeBufferHandler;
import com.kkb.plugins.uid.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ring buffer
 *
 * @author ztkool
 * @since
 */
public class RingBuffer {
    private static final Logger log = LoggerFactory.getLogger(RingBuffer.class);

    private static final int START_POINT = -1;
    private static final long CAN_PUT_FLAG = 0L;
    private static final long CAN_TAKE_FLAG = 1L;

    /**
     * 默认填充因子（百分之五十）
     */
    public static final int DEFAULT_PADDING_PERCENT = 50;

    //环形数组大小
    private final int bufferSize;

    private final long indexMask;

    /**
     * slots 用于存储 uid
     */
    private final long[] slots;

    /**
     * flags 用来存储标志（CAN_PUT_FLAG：0 - 允许插入；CAN_TAKE_FLAG：1 - 允许读取）
     */
    private final AtomicValue[] flags;

    /**
     * 当前生产数据的增量（每次加1），通过 (tail.get() & indexMask) 计算元素索引
     */
    private final AtomicValue tail = new AtomicValue(START_POINT);

    /**
     * 当前消费数据的增量（每次加1），通过 (tail.get() & indexMask) 计算元素索引
     */
    private final AtomicValue cursor = new AtomicValue(START_POINT);

    /**
     * 填充阀值
     */
    private final int paddingThreshold;

    /**
     * Reject put/take buffer handle policy
     */
    private RejectedPutBufferHandler rejectedPutHandler = new DefaultRejectedPutBufferHandler();
    private RejectedTakeBufferHandler rejectedTakeHandler = new DefaultRejectedTakeBufferHandler();

    /**
     * Executor of padding buffer
     */
    private BufferPaddingExecutor bufferPaddingExecutor;

    public RingBuffer(int bufferSize) {
        this(bufferSize, DEFAULT_PADDING_PERCENT);
    }

    public RingBuffer(int bufferSize, int paddingFactor) {
        Assert.isTrue(bufferSize > 0L, "RingBuffer size must be positive");
        Assert.isTrue(Integer.bitCount(bufferSize) == 1, "RingBuffer size must be a power of 2");
        Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "RingBuffer size must be positive in (0, 100)");

        this.bufferSize = bufferSize;
        this.indexMask = bufferSize - 1;
        this.slots = new long[bufferSize];
        this.flags = initFlags(bufferSize);

        this.paddingThreshold = bufferSize * paddingFactor / 100;
    }

    /**
     * 初始化所有 flags 状态为 CAN_PUT_FLAG（可插入）
     *
     * @param bufferSize
     * @return
     */
    private AtomicValue[] initFlags(int bufferSize) {
        AtomicValue[] flags = new AtomicValue[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            flags[i] = new AtomicValue(CAN_PUT_FLAG);
        }
        return flags;
    }

    /**
     * 生产
     *
     * @param uid
     * @return
     */
    public synchronized boolean put(long uid) {
        long currentTail = tail.get();
        long currentCursor = cursor.get();

        long distance = currentTail - (currentCursor == START_POINT ? 0 : currentCursor);
        if (distance == bufferSize - 1) {
            // buffer 已满，执行写拒绝策略
            rejectedPutHandler.rejectPutBuffer(this, uid);
            return false;
        }

        // 计算应插入的索引
        int nextTailIndex = calSlotIndex(currentTail + 1);

        if (flags[nextTailIndex].get() != CAN_PUT_FLAG) {
            // 如果待插入索引出的 flag 不是 CAN_PUT_FLAG，即不能插入，执行写拒绝策略
            rejectedPutHandler.rejectPutBuffer(this, uid);
            return false;
        }

        // 缓存 uid
        slots[nextTailIndex] = uid;
        // 设置 索引处的 flag 为 CAN_TAKE_FLAG（可读）
        flags[nextTailIndex].set(CAN_TAKE_FLAG);
        // tail 自增
        tail.incrementAndGet();
        return true;
    }

    /**
     * 消费
     *
     * @return
     */
    public long take() {
        // 获取当前已经消费的增量值
        long currentCursor = cursor.get();
        // 通过 cas 保证一个线程获取一个索引位置的值（比较 tail 增量值，如果相等，则表示已消费到最后一个元素）
        long nextCursor = cursor.updateAndGet(old -> old == tail.get() ? old : old + 1);
        Assert.isTrue(nextCursor >= currentCursor, "Cursor can't move back");
        // 获取当前已生产元素的增量值
        long currentTail = tail.get();
        if (currentTail - nextCursor < paddingThreshold) {
            // 如果剩余可读元素小于填充阀值(小于50%)，则触发填充操作
            log.info("uid generator log. Reach the padding threshold:{}. tail:{}, cursor:{}, rest:{}",
                    paddingThreshold, currentTail, nextCursor, currentTail - nextCursor);
            // 异步更新 buffer
            bufferPaddingExecutor.asyncPadding();
        }
        if (nextCursor == currentCursor) {
            // 上次消费就已经消费到最后一个元素，执行读拒绝逻辑
            rejectedTakeHandler.rejectTakeBuffer(this);
        }
        // 计算本次消费索引
        int nextCursorIndex = calSlotIndex(nextCursor);
        // 判断本次消费索引处的元素 flag 是否为 CAN_TAKE_FLAG：可读
        Assert.isTrue(flags[nextCursorIndex].get() == CAN_TAKE_FLAG, "Cursor not in can take status");
        // 获取本次消费索引处的数据
        long uid = slots[nextCursorIndex];
        // 将本次消费索引处的 flag 设置为 CAN_PUT_FLAG：可写
        flags[nextCursorIndex].set(CAN_PUT_FLAG);
        return uid;
    }

    /**
     * 计算索引
     *
     * @param sequence
     * @return
     */
    protected int calSlotIndex(long sequence) {
        return (int) (sequence & indexMask);
    }


    public long getTail() {
        return tail.get();
    }

    public long getCursor() {
        return cursor.get();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferPaddingExecutor(BufferPaddingExecutor bufferPaddingExecutor) {
        this.bufferPaddingExecutor = bufferPaddingExecutor;
    }

    public void setRejectedPutHandler(RejectedPutBufferHandler rejectedPutHandler) {
        this.rejectedPutHandler = rejectedPutHandler;
    }

    public void setRejectedTakeHandler(RejectedTakeBufferHandler rejectedTakeHandler) {
        this.rejectedTakeHandler = rejectedTakeHandler;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RingBuffer [bufferSize=").append(bufferSize)
                .append(", tail=").append(tail)
                .append(", cursor=").append(cursor)
                .append(", paddingThreshold=").append(paddingThreshold).append("]");
        return builder.toString();
    }

    public static void main(String[] args) {
        AtomicLong value = new AtomicLong(1);
        long update = value.updateAndGet(old -> old == 1 ? old : old + 1);
        System.out.println(update);
        System.out.println(value.get());
    }

}
