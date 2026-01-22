package com.iflow.api.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据源配置实体
 * 
 * 引用：REQ-F4-001（配置第三方数据库连接）
 *        REQ-F4-002（动态添加/删除数据源）
 *        REQ-F4-004（第三方数据源的连接信息应加密存储）
 */
@Data
@TableName("api_datasource_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class DatasourceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @NotBlank(message = "数据源类型不能为空")
    private String type;

    @NotBlank(message = "主机地址不能为空")
    private String host;

    @NotNull(message = "端口号不能为空")
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号必须小于65536")
    private Integer port;

    @NotBlank(message = "数据库名称不能为空")
    private String database;

    private String schemaName;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String connectionParams;

    private String poolConfig;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Integer isPrimary;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    // 显式添加 getter 和 setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConnectionParams() { return connectionParams; }
    public void setConnectionParams(String connectionParams) { this.connectionParams = connectionParams; }
    public String getPoolConfig() { return poolConfig; }
    public void setPoolConfig(String poolConfig) { this.poolConfig = poolConfig; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Integer isPrimary) { this.isPrimary = isPrimary; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public static class DataSourceType {
        public static final String MYSQL = "mysql";
        public static final String POSTGRESQL = "postgresql";
        public static final String ORACLE = "oracle";
        public static final String SQLSERVER = "sqlserver";
    }

    public static class Status {
        public static final Integer DISABLED = 0;
        public static final Integer ENABLED = 1;
    }

    public String getJdbcUrl() {
        StringBuilder url = new StringBuilder();
        switch (type) {
            case DataSourceType.MYSQL:
                url.append("jdbc:mysql://").append(host).append(":").append(port).append("/").append(database);
                if (connectionParams == null || !connectionParams.contains("useUnicode")) {
                    url.append("?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai");
                }
                break;
            case DataSourceType.POSTGRESQL:
                url.append("jdbc:postgresql://").append(host).append(":").append(port).append("/").append(database);
                if (schemaName != null && !schemaName.isEmpty()) {
                    url.append("?currentSchema=").append(schemaName);
                }
                break;
            case DataSourceType.ORACLE:
                url.append("jdbc:oracle:thin:@").append(host).append(":").append(port).append(":").append(database);
                break;
            case DataSourceType.SQLSERVER:
                url.append("jdbc:sqlserver://").append(host).append(":").append(port).append(";databaseName=").append(database);
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + type);
        }
        if (connectionParams != null && !connectionParams.isEmpty()) {
            url.append(url.toString().contains("?") ? "&" : "?").append(connectionParams);
        }
        return url.toString();
    }

    public String getDriverClassName() {
        switch (type) {
            case DataSourceType.MYSQL: return "com.mysql.cj.jdbc.Driver";
            case DataSourceType.POSTGRESQL: return "org.postgresql.Driver";
            case DataSourceType.ORACLE: return "oracle.jdbc.OracleDriver";
            case DataSourceType.SQLSERVER: return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default: throw new IllegalArgumentException("不支持的数据源类型: " + type);
        }
    }
}