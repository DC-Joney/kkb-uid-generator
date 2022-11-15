/*
 * Copyright
 */
package com.kkb.plugins.uid.exception;

/**
 * 类说明
 *
 * @author ztkool
 * @since
 */
public class UidGenerateException extends RuntimeException {

    public UidGenerateException() {
        super();
    }

    public UidGenerateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UidGenerateException(String message) {
        super(message);
    }

    public UidGenerateException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

    public UidGenerateException(Throwable cause) {
        super(cause);
    }

}
