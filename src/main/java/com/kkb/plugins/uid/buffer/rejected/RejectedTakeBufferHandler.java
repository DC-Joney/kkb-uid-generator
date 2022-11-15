/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer.rejected;

import com.kkb.plugins.uid.buffer.RingBuffer;

/**
 * 读空拒绝策略
 *
 * @author ztkool
 * @since
 */
public interface RejectedTakeBufferHandler {

    /**
     * 拒绝读 buffer
     *
     * @param ringBuffer
     */
    void rejectTakeBuffer(RingBuffer ringBuffer);
}
