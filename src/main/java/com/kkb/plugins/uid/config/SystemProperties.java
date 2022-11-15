/*
 * Copyright
 */
package com.kkb.plugins.uid.config;

import com.kkb.plugins.uid.worker.WorkerNodeType;
import com.kkb.plugins.uid.utils.NetUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 类描述
 *
 * @author ztkool
 * @since 1.0.0
 */
public class SystemProperties implements EnvironmentAware {

    private final static String ENV_KEY_PID = "PID";
    private final static String ENV_KEY_INSTANCE = "spring.application.name";
    private final static String ENV_KEY_PORT = "server.port";

    /**
     * 用于标识是否是容器启动（-DUSE_CONTAINER=true）
     */
    private final static String USE_CONTAINER = "use_container";

    /**
     * instance
     */
    private String instance;

    /**
     * hostname
     */
    private String hostname;

    /**
     * port
     */
    private String port;

    /**
     * pid
     */
    private String pid;

    /**
     * type
     */
    private int type;

    /**
     * activeProfiles
     */
    private String[] activeProfiles;

    @Override
    public void setEnvironment(Environment environment) {
        this.pid = environment.getProperty(ENV_KEY_PID);
        if (null == pid) {
            this.pid = checkOutPid();
        }
        this.activeProfiles = environment.getActiveProfiles();
        if (this.activeProfiles.length == 0) {
            this.activeProfiles = environment.getDefaultProfiles();
        }
        this.instance = environment.getProperty(ENV_KEY_INSTANCE);
        this.hostname = NetUtil.getLocalAddress();
        this.port = environment.getProperty(ENV_KEY_PORT);
        String useContainer = environment.getProperty(USE_CONTAINER);
        if (Boolean.parseBoolean(useContainer)) {
            this.type = WorkerNodeType.CONTAINER.value();
        } else {
            this.type = WorkerNodeType.ACTUAL.value();
        }
    }

    private String checkOutPid() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String name = bean.getName();
        String[] pidAndWorker = name.split("@");
        return pidAndWorker[0];
    }

    public String getInstance() {
        return instance;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getPid() {
        return pid;
    }

    public int getType() {
        return type;
    }

    public String[] getActiveProfiles() {
        return activeProfiles;
    }
}
