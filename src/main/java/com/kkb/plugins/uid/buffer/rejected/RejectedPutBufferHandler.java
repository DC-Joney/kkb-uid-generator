/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer.rejected;

import com.kkb.plugins.uid.buffer.RingBuffer;

/**
 * 写满拒绝策略
 *
 * @author ztkool
 * @since
 */
public interface RejectedPutBufferHandler {

    /**
     * 拒绝写 buffer
     *
     * @param ringBuffer
     * @param uid
     */
    void rejectPutBuffer(RingBuffer ringBuffer, long uid);
}
