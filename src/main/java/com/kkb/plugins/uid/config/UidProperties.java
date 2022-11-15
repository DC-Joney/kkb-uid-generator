/*
 * Copyright
 */
package com.kkb.plugins.uid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 类描述
 *
 * @author ztkool
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = UidProperties.PREFIX)
public class UidProperties {

    public static final String PREFIX = "kkb.plugins.uid-generator";

    /**
     * db 配置
     */
    private DataSource dataSource;

    /**
     * 雪花算法配置
     */
    private Snowflake snowflake;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setSnowflake(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    public Snowflake getSnowflake() {
        return snowflake;
    }

    public static class DataSource {
        private String driver;
        private String url;
        private String username;
        private String password;

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public String getDriver() {
            return driver;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class Snowflake {

        /**
         * 是否使用缓存
         */
        private boolean useCache = false;

        /**
         * 初始时间 yyyy-MM-dd
         */
        private String epoch = "2021-11-11";

        /**
         * 28 位 时间戳
         */
        private int timeBits = 28;

        /**
         * 22 位 workerId
         */
        private int workerBits = 22;

        /**
         * 13 位 序列号，最大 1 << 13 = 8192
         */
        private int seqBits = 13;

        /**
         * 设置缓存大小（大小为 (maxSequence + 1) << boostPower）
         */
        private int boostPower = 3;

        /**
         * workerId 基于数据库 id，使用 & 运算计算 id
         */
        private boolean useCycleWorkerId = false;

        /**
         * 填充缓存线程定时调度执行周期，默认不启用定时填充
         */
        private Long scheduleInterval;

        public boolean isUseCache() {
            return useCache;
        }

        public void setUseCache(boolean useCache) {
            this.useCache = useCache;
        }

        public String getEpoch() {
            return epoch;
        }

        public void setEpoch(String epoch) {
            this.epoch = epoch;
        }

        public int getTimeBits() {
            return timeBits;
        }

        public void setTimeBits(int timeBits) {
            this.timeBits = timeBits;
        }

        public int getWorkerBits() {
            return workerBits;
        }

        public void setWorkerBits(int workerBits) {
            this.workerBits = workerBits;
        }

        public int getSeqBits() {
            return seqBits;
        }

        public void setSeqBits(int seqBits) {
            this.seqBits = seqBits;
        }

        public int getBoostPower() {
            return boostPower;
        }

        public void setBoostPower(int boostPower) {
            this.boostPower = boostPower;
        }

        public boolean isUseCycleWorkerId() {
            return useCycleWorkerId;
        }

        public void setUseCycleWorkerId(boolean useCycleWorkerId) {
            this.useCycleWorkerId = useCycleWorkerId;
        }

        public Long getScheduleInterval() {
            return scheduleInterval;
        }

        public void setScheduleInterval(Long scheduleInterval) {
            this.scheduleInterval = scheduleInterval;
        }
    }

}
