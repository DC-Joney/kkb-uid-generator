/*
 * Copyright
 */
package com.kkb.plugins.uid.worker;

/**
 * workerId 分配器（算法集群核心）
 *
 * @author ztkool
 * @since
 */
public interface WorkerIdAssigner {

    /**
     * 分配 workerId
     *
     * @return
     */
    long assignWorkerId();

}
