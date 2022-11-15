/*
 * Copyright
 */
package com.kkb.plugins.uid.config;

import com.kkb.plugins.uid.worker.DatabaseHolder;
import com.kkb.plugins.uid.worker.DatabaseWorkerIdAssigner;
import com.kkb.plugins.uid.worker.WorkerIdAssigner;
import com.kkb.plugins.uid.generator.CachedUidGenerator;
import com.kkb.plugins.uid.generator.DefaultUidGenerator;
import com.kkb.plugins.uid.generator.UidGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

/**
 * 类说明
 *
 * @author ztkool
 * @since
 */
@EnableConfigurationProperties({UidProperties.class})
public class UidConfig {

    @Resource
    private UidProperties properties;

    @Bean
    public SystemProperties systemProperties() {
        return new SystemProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public DatabaseHolder databaseHolder() {
        UidProperties.DataSource datasource = properties.getDataSource();
        return new DatabaseHolder(datasource);
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkerIdAssigner workerIdAssigner() {
        return new DatabaseWorkerIdAssigner();
    }

    @Bean
    @Primary
    public UidGenerator uidGenerator(WorkerIdAssigner workerIdAssigner) {
        UidProperties.Snowflake snowflake = properties.getSnowflake();
        if (snowflake.isUseCache()) {
            // 使用缓存 uid generator
            return cachedUidGenerator(snowflake, workerIdAssigner);
        }
        // 默认 uid generator
        return defaultUidGenerator(snowflake, workerIdAssigner);
    }

    private DefaultUidGenerator defaultUidGenerator(UidProperties.Snowflake snowflake,
                                                    WorkerIdAssigner workerIdAssigner) {
        DefaultUidGenerator uidGenerator = new DefaultUidGenerator(workerIdAssigner);
        // 初始时间 yyyy-MM-dd
        uidGenerator.setEpochStr(snowflake.getEpoch());
        // 时间 bit 长度
        uidGenerator.setTimeBits(snowflake.getTimeBits());
        // workerId bit 长度
        uidGenerator.setWorkerBits(snowflake.getWorkerBits());
        // sequence bit 长度
        uidGenerator.setSeqBits(snowflake.getSeqBits());
        return uidGenerator;
    }

    private CachedUidGenerator cachedUidGenerator(UidProperties.Snowflake snowflake,
                                                  WorkerIdAssigner workerIdAssigner) {
        CachedUidGenerator uidGenerator = new CachedUidGenerator(workerIdAssigner);
        // 初始时间 yyyy-MM-dd
        uidGenerator.setEpochStr(snowflake.getEpoch());
        // 时间 bit 长度
        uidGenerator.setTimeBits(snowflake.getTimeBits());
        // workerId bit 长度
        uidGenerator.setWorkerBits(snowflake.getWorkerBits());
        // sequence bit 长度
        uidGenerator.setSeqBits(snowflake.getSeqBits());
        // 是否循环使用 workerId（workerId &= ～(-1L << workerBits)）
        uidGenerator.setUseCycleWorkerId(snowflake.isUseCycleWorkerId());
        // 缓存大小 （bufferSize = (1 << SeqBits) << boostPower）
        uidGenerator.setBoostPower(snowflake.getBoostPower());
        // 定时更新缓存间隔，Null 时不启用定时更新
        uidGenerator.setScheduleInterval(snowflake.getScheduleInterval());
        return uidGenerator;
    }


}
