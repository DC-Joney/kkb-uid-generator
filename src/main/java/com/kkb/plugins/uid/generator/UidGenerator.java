/*
 * Copyright
 */
package com.kkb.plugins.uid.generator;

import com.kkb.plugins.uid.exception.UidGenerateException;

/**
 * unique id generator
 *
 * @author ztkool
 * @since
 */
public interface UidGenerator<T> {

    /**
     * 生成 uid
     *
     * @return
     * @throws UidGenerateException
     */
    T get() throws UidGenerateException;

    /**
     * uid 解析
     *
     * @param uid
     * @return
     */
    String parse(T uid);

}
