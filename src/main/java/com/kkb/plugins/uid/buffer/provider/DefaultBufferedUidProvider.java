/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer.provider;

import com.kkb.plugins.uid.generator.BitsAllocator;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述
 *
 * @author ztkool
 * @since 1.0.0
 */
public class DefaultBufferedUidProvider implements BufferedUidProvider {

    private final BitsAllocator bitsAllocator;
    private long epochSeconds;
    private long workerId;

    public DefaultBufferedUidProvider(BitsAllocator bitsAllocator, long epochSeconds, long workerId) {
        this.bitsAllocator = bitsAllocator;
        this.epochSeconds = epochSeconds;
        this.workerId = workerId;
    }

    @Override
    public List<Long> provide(long currentSecond) {
        int size = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(size);
        long firstSeqUid = bitsAllocator.allocate(currentSecond - epochSeconds, workerId, 0L);
        for (int i = 0; i < size; i++) {
            uidList.add(firstSeqUid + i);
        }
        return uidList;
    }

}
