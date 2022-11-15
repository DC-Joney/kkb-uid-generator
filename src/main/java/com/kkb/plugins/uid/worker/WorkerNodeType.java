/*
 * Copyright
 */
package com.kkb.plugins.uid.worker;

/**
 * 机器节点类型
 *
 * @author ztkool
 * @since
 */
public enum WorkerNodeType {

    /**
     * 容器，比如 docker
     */
    CONTAINER(1),

    /**
     * 真实机器
     */
    ACTUAL(2);

    private final int type;

    WorkerNodeType(int type) {
        this.type = type;
    }

    public int value() {
        return type;
    }

}
