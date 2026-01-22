package com.iflow.api.core.dto.view;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表信息展示 DTO (前端使用)
 * 
 * 引用：REQ-F9-002（表结构浏览界面）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TableInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源 ID
     */
    private Long datasourceId;

    /**
     * 数据源名称
     */
    private String datasourceName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表备注/描述
     */
    private String tableComment;

    /**
     * 字段数量
     */
    private Integer columnCount;

    /**
     * 索引数量
     */
    private Integer indexCount;

    /**
     * API 是否已生成
     */
    private Boolean apiGenerated;

    /**
     * 状态 (pending-待生成, generating-生成中, generated-已生成, skipped-已跳过, error-错误)
     */
    private String status;

    /**
     * 选择状态 (true-已选中, false-未选中)
     */
    private Boolean selected;

    /**
     * 冲突状态 (true-有冲突)
     */
    private Boolean conflict;

    /**
     * 生成模式 (auto-自动, manual-手动)
     */
    private String generateMode;

    /**
     * 跳过原因
     */
    private String skipReason;

    /**
     * 跳过原因详细说明
     */
    private String skipReasonDetail;

    /**
     * API 路径
     */
    private String apiPath;

    /**
     * REST API 是否可用
     */
    private Boolean restAvailable;

    /**
     * GraphQL Type 名称
     */
    private String graphqlType;

    /**
     * GraphQL 是否可用
     */
    private Boolean graphqlAvailable;

    /**
     * 错误消息（如果状态为 error）
     */
    private String errorMessage;

    /**
     * 生成时间
     */
    private String generatedAt;

    /**
     * 最后更新时间
     */
    private String updatedAt;

    /**
     * 字段列表（简要信息）
     */
    private List<ColumnBrief> columns = new ArrayList<>();

    /**
     * 添加 setConflict(boolean) 原始类型重载方法（兼容 DatabaseMetaData 风格）
     */
    public void setConflict(boolean conflict) {
        this.conflict = Boolean.valueOf(conflict);
    }

    /**
     * 添加 setApiGenerated(boolean) 原始类型重载方法
     */
    public void setApiGenerated(boolean apiGenerated) {
        this.apiGenerated = Boolean.valueOf(apiGenerated);
    }

    /**
     * 添加兼容性 setter 方法（DatabaseMetaData 风格）
     */
    public void setName(String name) {
        this.tableName = name;
    }

    public void setRemarks(String remarks) {
        this.tableComment = remarks;
    }

    public void setType(String type) {
        // 保留用于兼容性
    }

    public void setCatalog(String catalog) {
        // 保留用于兼容性
    }

    public void setSchema(String schema) {
        // 保留用于兼容性
    }

    /**
     * 字段简要信息
     */
    @Data
    public static class ColumnBrief implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String type;
        private Boolean isPrimaryKey;
        private Boolean isNullable;
        private String comment;
    }

    /**
     * 状态文本映射
     */
    public static String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "待生成";
            case "generating":
                return "生成中";
            case "generated":
                return "已生成";
            case "skipped":
                return "已跳过";
            case "error":
                return "错误";
            case "removed":
                return "已移除";
            default:
                return status;
        }
    }

    /**
     * 状态类型映射（用于前端 Tag 组件）
     */
    public static String getStatusType(String status) {
        switch (status) {
            case "pending":
                return "warning";
            case "generating":
                return "info";
            case "generated":
                return "success";
            case "skipped":
                return "info";
            case "error":
                return "danger";
            case "removed":
                return "info";
            default:
                return "info";
        }
    }

    /**
     * 检查是否可用
     */
    public boolean isAvailable() {
        return "generated".equals(status) && (
            Boolean.TRUE.equals(restAvailable) || Boolean.TRUE.equals(graphqlAvailable)
        );
    }
}