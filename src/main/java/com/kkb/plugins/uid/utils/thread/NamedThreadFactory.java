/*
 * Copyright
 */
package com.kkb.plugins.uid.utils.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * thread factory
 *
 * @author ztkool
 * @since
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final Logger log = LoggerFactory.getLogger(NamedThreadFactory.class);

    /**
     * name
     */
    private String name;

    /**
     * 是否是 daemon 线程
     */
    private boolean daemon;

    /**
     * 未捕获异常处理器
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * sequence
     */
    private AtomicLong sequence = new AtomicLong(0);

    public NamedThreadFactory(String name) {
        this(name, false, null);
    }

    public NamedThreadFactory(String name, boolean daemon) {
        this(name, daemon, null);
    }

    public NamedThreadFactory(String name, boolean daemon, UncaughtExceptionHandler handler) {
        this.name = name;
        this.daemon = daemon;
        this.uncaughtExceptionHandler = handler;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(this.daemon);
        thread.setName(this.name + "-" + sequence.incrementAndGet());
        if (this.uncaughtExceptionHandler != null) {
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        } else {
            thread.setUncaughtExceptionHandler(this::doUncaughtExceptionHandler);
        }
        return thread;
    }

    private void doUncaughtExceptionHandler(Thread thread, Throwable throwable) {
        log.error("uid generator error log. unhandled exception in thread: " + thread.getId() + ":" + thread.getName(), throwable);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        this.uncaughtExceptionHandler = handler;
    }

}
