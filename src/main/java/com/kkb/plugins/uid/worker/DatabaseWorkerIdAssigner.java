/*
 * Copyright
 */
package com.kkb.plugins.uid.worker;

import com.kkb.plugins.uid.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 一次性 workerId 分配器，通过生成一条数据库记录，获取该记录的 id
 *
 * @author ztkool
 * @since
 */
public class DatabaseWorkerIdAssigner implements WorkerIdAssigner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseWorkerIdAssigner.class);

    private final static String WORKER_NODE_INSERT
            = "insert into worker_node (instance, pid, active_profiles, host_name, port, type) values (?, ?, ?, ?, ?, ?)";

    @Resource
    private DatabaseHolder databaseHolder;

    @Resource
    private SystemProperties systemProperties;

    @Override
    public long assignWorkerId() {
        // 构建 workerNodeEntity
        WorkerNode workerNode = getWorkerNode();
        // 数据库添加一条记录
        saveWorkerNode(workerNode);
        log.info("uid generator log. save worker node: {}", workerNode);
        return workerNode.getId();
    }

    /**
     * 添加 worker node
     *
     * @author ztkool
     * @since
     */
    private void saveWorkerNode(WorkerNode workerNode) {
        long id = databaseHolder.insert(WORKER_NODE_INSERT, workerNode.getInstance(),
                workerNode.getPid(), workerNode.getActiveProfiles(),
                workerNode.getHostName(), workerNode.getPort(), workerNode.getType());
        if (id == -1) {
            log.error("uid generator error log. error when insert worker_node. node: {}", workerNode);
            throw new RuntimeException("uid generator error. error when insert worker_node.");
        }
        workerNode.setId(id);
    }

    private WorkerNode getWorkerNode() {
        WorkerNode workerNode = new WorkerNode();
        workerNode.setInstance(systemProperties.getInstance());
        workerNode.setHostName(systemProperties.getHostname());
        workerNode.setPort(systemProperties.getPort());
        workerNode.setPid(systemProperties.getPid());
        workerNode.setType(systemProperties.getType());
        workerNode.setActiveProfiles(systemProperties.getActiveProfiles());
        return workerNode;
    }

}
