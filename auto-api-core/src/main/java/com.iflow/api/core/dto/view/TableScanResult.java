package com.iflow.api.core.dto.view;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 表扫描结果 DTO (前端使用)
 * 
 * 引用：REQ-F9-002（表结构浏览界面）
 *        REQ-F0-001（全量自动选择）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TableScanResult implements Serializable {

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
     * 数据源类型
     */
    private String datasourceType;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 模式名称
     */
    private String schemaName;

    /**
     * 表总数
     */
    private Integer totalCount;

    /**
     * 已选择数量
     */
    private Integer selectedCount;

    /**
     * 已生成数量
     */
    private Integer generatedCount;

    /**
     * 已跳过数量
     */
    private Integer skippedCount;

    /**
     * 错误数量
     */
    private Integer errorCount;

    /**
     * 冲突数量
     */
    private Integer conflictCount;

    /**
     * 表信息列表
     */
    private List<TableInfo> tables = new ArrayList<>();

    /**
     * 扫描时间
     */
    private Date scanTime;

    /**
     * 扫描耗时 (毫秒)
     */
    private Long scanDuration;

    /**
     * 添加 setter 方法
     */
    public void setConflictCount(Integer conflictCount) {
        this.conflictCount = conflictCount;
    }

    /**
     * 添加原始类型重载方法（兼容 DatabaseMetaData 风格）
     */
    public void setConflictCount(int conflictCount) {
        this.conflictCount = Integer.valueOf(conflictCount);
    }

    public void setScanTime(Date scanTime) {
        this.scanTime = scanTime;
    }

    /**
     * 获取未选择数量
     */
    public Integer getUnselectedCount() {
        if (totalCount == null || selectedCount == null) {
            return 0;
        }
        return totalCount - selectedCount;
    }

    /**
     * 获取选择百分比
     */
    public Double getSelectedPercentage() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (selectedCount * 100.0) / totalCount;
    }

    /**
     * 获取生成百分比
     */
    public Double getGeneratedPercentage() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (generatedCount * 100.0) / totalCount;
    }

    /**
     * 统计汇总
     */
    public void calculateSummary() {
        if (tables == null || tables.isEmpty()) {
            this.totalCount = 0;
            this.selectedCount = 0;
            this.generatedCount = 0;
            this.skippedCount = 0;
            this.errorCount = 0;
            this.conflictCount = 0;
            return;
        }

        this.totalCount = tables.size();
        this.selectedCount = (int) tables.stream()
            .filter(t -> Boolean.TRUE.equals(t.getSelected()))
            .count();
        this.generatedCount = (int) tables.stream()
            .filter(t -> "generated".equals(t.getStatus()))
            .count();
        this.skippedCount = (int) tables.stream()
            .filter(t -> "skipped".equals(t.getStatus()))
            .count();
        this.errorCount = (int) tables.stream()
            .filter(t -> "error".equals(t.getStatus()))
            .count();
        this.conflictCount = (int) tables.stream()
            .filter(t -> Boolean.TRUE.equals(t.getConflict()))
            .count();
    }
}