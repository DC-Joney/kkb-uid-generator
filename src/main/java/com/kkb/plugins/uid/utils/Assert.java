/*
 * Copyright
 */
package com.kkb.plugins.uid.utils;

import org.springframework.lang.Nullable;

/**
 * 类描述
 *
 * @author ztkool
 * @since 1.0.0
 */
public class Assert {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

}
