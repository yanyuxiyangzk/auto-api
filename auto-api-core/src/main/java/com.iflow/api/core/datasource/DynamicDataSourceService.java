package com.iflow.api.core.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.entity.DatasourceConfig;
import com.iflow.api.core.repository.DatasourceConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理服务
 * 
 * 引用：REQ-F4-002（动态添加/删除第三方数据源）
 *        REQ-F4-005（连接池管理，保证连接稳定性）
 */
@Slf4j
@Service
public class DynamicDataSourceService {

    @Autowired
    private DatasourceConfigRepository datasourceConfigRepository;

    /**
     * 数据源缓存 (数据源ID -> DruidDataSource)
     */
    private final Map<Long, DruidDataSource> datasourceCache = new ConcurrentHashMap<>();

    /**
     * 数据源连接缓存 (数据源ID -> Connection)
     */
    private final Map<Long, Connection> connectionCache = new ConcurrentHashMap<>();

    /**
     * 获取所有启用的数据源
     */
    public List<DatasourceConfig> getActiveDataSources() {
        return datasourceConfigRepository.findAllActive();
    }

    /**
     * 根据 ID 获取数据源配置
     */
    public Optional<DatasourceConfig> getById(Long id) {
        return datasourceConfigRepository.findByIdWithoutDeleted(id);
    }

    /**
     * 测试数据源连接
     * 
     * @param config 数据源配置
     * @return true-连接成功, false-连接失败
     */
    public boolean testConnection(DatasourceConfig config) {
        DruidDataSource dataSource = null;
        try {
            dataSource = createDataSource(config);
            try (Connection conn = dataSource.getConnection()) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            log.warn("数据源连接测试失败: {}", e.getMessage());
            return false;
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    /**
     * 获取或创建数据源
     * 
     * @param datasourceId 数据源 ID
     * @return DruidDataSource
     */
    public synchronized DruidDataSource getOrCreateDataSource(Long datasourceId) {
        // 先从缓存获取
        DruidDataSource cached = datasourceCache.get(datasourceId);
        if (cached != null && !cached.isClosed()) {
            return cached;
        }

        // 获取配置
        DatasourceConfig config = datasourceConfigRepository
            .findByIdWithoutDeleted(datasourceId)
            .orElseThrow(() -> new IllegalArgumentException("数据源配置不存在: " + datasourceId));

        // 创建数据源
        DruidDataSource dataSource = createDataSource(config);
        datasourceCache.put(datasourceId, dataSource);

        log.info("创建数据源: id={}, name={}, type={}", 
            datasourceId, config.getName(), config.getType());

        return dataSource;
    }

    /**
     * 获取数据库连接
     * 
     * @param datasourceId 数据源 ID
     * @return Connection
     */
    public Connection getConnection(Long datasourceId) {
        // 先检查缓存的连接
        Connection cached = connectionCache.get(datasourceId);
        if (cached != null && isConnectionValid(cached)) {
            return cached;
        }

        // 创建新连接
        DruidDataSource dataSource = getOrCreateDataSource(datasourceId);
        try {
            Connection conn = dataSource.getConnection();
            connectionCache.put(datasourceId, conn);
            return conn;
        } catch (SQLException e) {
            log.error("获取数据库连接失败: datasourceId={}", datasourceId, e);
            throw new RuntimeException("获取数据库连接失败", e);
        }
    }

    /**
     * 获取数据库元数据
     * 
     * @param datasourceId 数据源 ID
     * @return DatabaseMetaData
     */
    public DatabaseMetaData getDatabaseMetaData(Long datasourceId) {
        try {
            return getConnection(datasourceId).getMetaData();
        } catch (SQLException e) {
            log.error("获取数据库元数据失败: datasourceId={}", datasourceId, e);
            throw new RuntimeException("获取数据库元数据失败", e);
        }
    }

    /**
     * 创建 Druid 数据源
     */
    private DruidDataSource createDataSource(DatasourceConfig config) {
        DruidDataSource dataSource = new DruidDataSource();

        // 基础配置
        dataSource.setUrl(config.getJdbcUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setDriverClassName(config.getDriverClassName());

        // 连接池配置
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        // 开启 PSCache
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);

        return dataSource;
    }

    /**
     * 检查连接是否有效
     */
    private boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 刷新数据源（重新创建）
     * 
     * @param datasourceId 数据源 ID
     */
    public synchronized void refreshDataSource(Long datasourceId) {
        // 关闭旧数据源
        DruidDataSource oldDataSource = datasourceCache.remove(datasourceId);
        if (oldDataSource != null && !oldDataSource.isClosed()) {
            oldDataSource.close();
            log.info("关闭旧数据源: {}", datasourceId);
        }

        // 关闭旧连接
        Connection oldConn = connectionCache.remove(datasourceId);
        if (oldConn != null) {
            try {
                oldConn.close();
            } catch (SQLException e) {
                log.warn("关闭旧连接失败", e);
            }
        }

        log.info("数据源已刷新: {}", datasourceId);
    }

    /**
     * 关闭数据源
     * 
     * @param datasourceId 数据源 ID
     */
    public synchronized void closeDataSource(Long datasourceId) {
        DruidDataSource dataSource = datasourceCache.remove(datasourceId);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("数据源已关闭: {}", datasourceId);
        }

        Connection conn = connectionCache.remove(datasourceId);
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("关闭连接失败", e);
            }
        }
    }

    /**
     * 关闭所有数据源
     */
    public synchronized void closeAllDataSources() {
        datasourceCache.forEach((id, ds) -> {
            if (ds != null && !ds.isClosed()) {
                ds.close();
                log.info("数据源已关闭: {}", id);
            }
        });
        datasourceCache.clear();
        connectionCache.clear();
    }

    /**
     * 获取缓存的数据源数量
     */
    public int getCachedDataSourceCount() {
        return datasourceCache.size();
    }

    /**
     * 检查数据源是否已缓存
     */
    public boolean isDataSourceCached(Long datasourceId) {
        DruidDataSource ds = datasourceCache.get(datasourceId);
        return ds != null && !ds.isClosed();
    }
}