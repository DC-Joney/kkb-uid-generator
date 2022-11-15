/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer.rejected;

import com.kkb.plugins.uid.buffer.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读空拒绝策略
 *
 * @author ztkool
 * @since
 */
public class DefaultRejectedTakeBufferHandler implements RejectedTakeBufferHandler {

    private final static Logger log = LoggerFactory.getLogger(DefaultRejectedTakeBufferHandler.class);

    @Override
    public void rejectTakeBuffer(RingBuffer ringBuffer) {
        log.warn("uid generator warn log. Rejected take buffer. {}", ringBuffer);
        throw new RuntimeException("Rejected take buffer. " + ringBuffer);
    }
}
