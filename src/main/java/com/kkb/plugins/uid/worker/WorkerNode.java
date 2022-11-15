/*
 * Copyright
 */
package com.kkb.plugins.uid.worker;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.Date;

/**
 * worker node
 *
 * @author ztkool
 * @since
 */
public class WorkerNode {

    /**
     * id
     */
    private Long id;

    /**
     * 实例名
     */
    private String instance;

    /**
     * hostName
     */
    private String hostName;

    /**
     * port
     */
    private String port;

    /**
     * {@link WorkerNodeType}
     */
    private int type;

    /**
     * pid
     */
    private String pid;

    /**
     * activeProfiles
     */
    private String activeProfiles;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getActiveProfiles() {
        return activeProfiles;
    }

    public void setActiveProfiles(String[] activeProfiles) {
        if (null != activeProfiles) {
            this.activeProfiles = Arrays.toString(activeProfiles);
        }
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
