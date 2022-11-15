/*
 * Copyright
 */
package com.kkb.plugins.uid.worker;

import com.kkb.plugins.uid.config.UidProperties;
import com.kkb.plugins.uid.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 类说明
 *
 * @author ztkool
 * @since
 */
public class DatabaseHolder {

    private final static Logger log = LoggerFactory.getLogger(DatabaseHolder.class);

    private final UidProperties.DataSource dataSource;

    public DatabaseHolder(UidProperties.DataSource dataSource) {
        this.dataSource = dataSource;
        preCheck();
        if (log.isWarnEnabled()) {
            log.warn("uid generator log. use database for uid generator. url: {}", dataSource.getUrl());
        }
    }

    private void preCheck() {
        Assert.notNull(dataSource, "dataSource is null.");
        Assert.notNull(dataSource.getDriver(), "dataSource driver is null.");
        Assert.notNull(dataSource.getUrl(), "dataSource url is null.");
        Assert.notNull(dataSource.getUsername(), "dataSource username is null.");
        Assert.notNull(dataSource.getPassword(), "dataSource password is null.");
        try {
            Class.forName(dataSource.getDriver());
        } catch (Exception e) {
            throw new NullPointerException("driver (" + dataSource.getDriver() + ") not found.");
        }
    }

    public long insert(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
            statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (null != params) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            log.info("uid generator log. insert - sql: {}, params: {}", sql, params);
            int row = statement.executeUpdate();
            if (row > 0) {
                rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    long workerId = rs.getLong(1);
                    log.info("uid generator log. insert - result: {}", workerId);
                    return workerId;
                }
            }
            return -1;
        } catch (SQLException e) {
            log.error("uid generator error log. error when execute db.", e);
            throw new RuntimeException(e);
        } finally {
            close(conn, statement, rs);
        }
    }

    private static void close(Connection conn, Statement stat, ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != stat) {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
