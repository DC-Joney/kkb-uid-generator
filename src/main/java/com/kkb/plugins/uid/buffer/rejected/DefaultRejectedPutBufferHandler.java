/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer.rejected;

import com.kkb.plugins.uid.buffer.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 写满拒绝策略
 *
 * @author ztkool
 * @since
 */
public class DefaultRejectedPutBufferHandler implements RejectedPutBufferHandler {

    private final static Logger log = LoggerFactory.getLogger(DefaultRejectedPutBufferHandler.class);

    @Override
    public void rejectPutBuffer(RingBuffer ringBuffer, long uid) {
        log.warn("uid generator warn log. Rejected putting buffer for uid:{}. {}", uid, ringBuffer);
    }

}
