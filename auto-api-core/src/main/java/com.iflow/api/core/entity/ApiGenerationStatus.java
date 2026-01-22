package com.iflow.api.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.iflow.api.core.dto.metadata.TableMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * API 生成状态实体
 * 
 * 引用：REQ-F0-010（刷新 API 配置接口）
 *        REQ-F7-001（脚本修改后立即生效）
 */
@Data
@TableName("api_generation_status")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ApiGenerationStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表选择记录 ID (非数据库字段，用于关联)
     */
    @TableField(exist = false)
    private Long tableSelectionId;

    /**
     * 表元数据 (非数据库字段)
     */
    @TableField(exist = false)
    private TableMeta tableMeta;

    /**
     * API 路径
     */
    private String apiPath;

    /**
     * 生成时间
     */
    private Date generateTime;

    /**
     * 数据源配置 ID
     */
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;

    /**
     * 表名
     */
    @NotNull(message = "表名不能为空")
    private String tableName;

    /**
     * 生成状态 (pending-待生成, generating-生成中, generated-已生成, 
     *           skipped-已跳过, error-错误, removed-已移除)
     */
    @NotNull(message = "生成状态不能为空")
    private String status;

    /**
     * REST API 路径
     */
    private String restApiPath;

    /**
     * REST API 是否已注册
     */
    private Boolean restApiRegistered;

    /**
     * GraphQL Type 名称
     */
    private String graphqlType;

    /**
     * GraphQL Schema 是否已注册
     */
    private Boolean graphqlRegistered;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 错误堆栈
     */
    private String errorStack;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 最后刷新时间
     */
    private LocalDateTime lastRefreshedAt;
    
    /**
     * 最后重新生成时间 (兼容 Date 类型)
     */
    @TableField(exist = false)
    private Date lastRegenerateTime;

    /**
     * 生成耗时 (毫秒)
     */
    private Long generationDuration;

    /**
     * API 版本号
     */
    private Integer apiVersion;

    /**
     * 元数据版本号 (表结构变更时递增)
     */
    private Long metadataVersion;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 生成状态常量
     */
    public static class Status {
        public static final String PENDING = "pending";
        public static final String GENERATING = "generating";
        public static final String GENERATED = "generated";
        public static final String ACTIVE = "ACTIVE";
        public static final String SKIPPED = "skipped";
        public static final String ERROR = "error";
        public static final String REMOVED = "removed";
    }

    /**
     * 检查 API 是否已生成并可用
     */
    public boolean isAvailable() {
        return Status.GENERATED.equals(status) 
            || Status.ACTIVE.equals(status)
            && (Boolean.TRUE.equals(restApiRegistered) || Boolean.TRUE.equals(graphqlRegistered));
    }

    /**
     * 检查是否需要重新生成
     */
    public boolean needsRegeneration() {
        return Status.ERROR.equals(status) 
            || Status.PENDING.equals(status)
            || Status.REMOVED.equals(status);
    }

    // 显式定义非数据库字段的 getter/setter (确保 Lombok 正确生成)
    public Long getTableSelectionId() {
        return tableSelectionId;
    }

    public void setTableSelectionId(Long tableSelectionId) {
        this.tableSelectionId = tableSelectionId;
    }

    public TableMeta getTableMeta() {
        return tableMeta;
    }

    public void setTableMeta(TableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }
    
    // 添加 lastRegenerateTime 兼容方法
    public Date getLastRegenerateTime() {
        return lastRegenerateTime;
    }
    
    public void setLastRegenerateTime(Date lastRegenerateTime) {
        this.lastRegenerateTime = lastRegenerateTime;
    }
}