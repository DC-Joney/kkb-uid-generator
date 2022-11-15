/*
 * Copyright
 */
package com.kkb.plugins.uid.buffer.provider;

import java.util.List;

/**
 * 缓存 uid 提供
 *
 * @author ztkool
 * @since
 */
public interface BufferedUidProvider {

    /**
     * 基于当前时间（秒）生成一批 uid（供 ringBuffer 使用）
     *
     * @param currentSecond
     * @return
     */
    List<Long> provide(long currentSecond);
}
