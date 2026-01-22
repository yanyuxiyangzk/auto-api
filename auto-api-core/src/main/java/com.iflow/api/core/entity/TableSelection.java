package com.iflow.api.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.iflow.api.core.dto.metadata.TableMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表选择配置实体
 * 
 * 引用：REQ-F0-001（全量自动选择）
 *        REQ-F0-002（手动选择）
 *        REQ-F0-003（黑白名单机制）
 *        REQ-F0-005（检测已存在API，避免冲突）
 */
@Data
@TableName("api_table_selection")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TableSelection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 表备注/描述
     */
    private String tableComment;

    /**
     * 表元数据 (非数据库字段)
     */
    @TableField(exist = false)
    private TableMeta tableMeta;

    /**
     * 是否自动选择 (true-自动, false-手动)
     */
    @TableField(exist = false)
    private Boolean autoSelected;

    /**
     * 是否已生成 API (非数据库字段)
     */
    @TableField(exist = false)
    private Boolean apiGenerated;

    /**
     * 是否选择生成 API (true-选中, false-未选中/跳过)
     */
    @NotNull(message = "选择状态不能为空")
    private Boolean selected;

    /**
     * 生成模式 (auto-自动, manual-手动)
     */
    private String generateMode;

    /**
     * API 路径前缀 (可选自定义)
     */
    private String apiPrefix;

    /**
     * 跳过原因 (already_exists-已存在API, filtered-正则过滤, manual-手动取消)
     */
    private String skipReason;

    /**
     * 跳过说明
     */
    private String skipReasonDetail;

    /**
     * 包含字段列表 (JSON 数组格式，如 ["id","name","status"])
     * 为空表示包含所有字段
     */
    private String includeColumns;

    /**
     * 排除字段列表 (JSON 数组格式，如 ["password","secret"])
     */
    private String excludeColumns;

    /**
     * 自定义 API 规则 (JSON 格式)
     */
    private String customRules;

    /**
     * 优先级 (数字越小优先级越高)
     */
    private Integer priority;

    /**
     * 状态 (0-草稿, 1-已发布)
     */
    private Integer status;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除标记 (0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer deleted;

    /**
     * 生成模式常量
     */
    public static class GenerateMode {
        public static final String AUTO = "auto";
        public static final String MANUAL = "manual";
    }

    /**
     * 跳过原因常量
     */
    public static class SkipReason {
        public static final String ALREADY_EXISTS = "already_exists";
        public static final String FILTERED = "filtered";
        public static final String MANUAL = "manual";
        public static final String ERROR = "error";
    }

    /**
     * 状态常量
     */
    public static class Status {
        public static final Integer DRAFT = 0;
        public static final Integer PUBLISHED = 1;
    }

    /**
     * 检查是否应该生成 API
     */
    public boolean shouldGenerate() {
        return Boolean.TRUE.equals(selected) 
            && skipReason == null 
            && !SkipReason.ERROR.equals(skipReason);
    }

    // 显式定义非数据库字段的 getter/setter (确保 Lombok 正确生成)
    public TableMeta getTableMeta() {
        return tableMeta;
    }

    public void setTableMeta(TableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }

    public Boolean getAutoSelected() {
        return autoSelected;
    }

    public void setAutoSelected(Boolean autoSelected) {
        this.autoSelected = autoSelected;
    }

    public Boolean getApiGenerated() {
        return apiGenerated;
    }

    public void setApiGenerated(Boolean apiGenerated) {
        this.apiGenerated = apiGenerated;
    }
}