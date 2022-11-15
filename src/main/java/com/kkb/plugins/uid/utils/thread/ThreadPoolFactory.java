/*
 * Copyright
 */
package com.kkb.plugins.uid.utils.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 类描述
 *
 * @author ztkool
 * @since 1.0.0
 */
public class ThreadPoolFactory {

    /**
     * 创建 fixed 线程池
     *
     * @param nThreads
     * @param name
     * @return
     */
    public static ThreadPoolExecutor newFixedThreadPool(int nThreads, String name) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory(name));
    }

    /**
     * 创建单线程定时调度线程池
     *
     * @param name
     * @return
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String name) {
        return Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
    }
}
