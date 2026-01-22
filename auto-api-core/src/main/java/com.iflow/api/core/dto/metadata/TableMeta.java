package com.iflow.api.core.dto.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表元数据 DTO
 * 
 * 引用：REQ-F3-003（读取表结构信息）
 *        REQ-F3-004（识别主键、字段类型）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TableMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long datasourceId;
    private String datasourceType;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private String tableType;
    private String tableComment;
    private List<ColumnMeta> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<IndexMeta> indexes = new ArrayList<>();
    private List<ForeignKeyMeta> foreignKeys = new ArrayList<>();
    private Integer columnCount;
    private Boolean apiGenerated;
    private String apiPath;

    // 添加兼容性 setter 方法（DatabaseMetaData 风格）
    public void setName(String name) {
        this.tableName = name;
    }

    public void setType(String type) {
        this.tableType = type;
    }

    public void setRemarks(String remarks) {
        this.tableComment = remarks;
    }

    public void setCatalog(String catalog) {
        this.databaseName = catalog;
    }

    public void setSchema(String schema) {
        this.schemaName = schema;
    }

    // 添加兼容性 getter 方法（DatabaseMetaData 风格）
    public String getName() {
        return tableName;
    }

    public String getType() {
        return tableType;
    }

    public String getRemarks() {
        return tableComment;
    }

    public String getCatalog() {
        return databaseName;
    }

    public String getSchema() {
        return schemaName;
    }

    public String getPrimaryKeyType() {
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            return null;
        }
        if (primaryKeys.size() == 1) {
            String pk = primaryKeys.get(0);
            return columns.stream()
                .filter(c -> c.getName().equals(pk))
                .map(ColumnMeta::getJavaType)
                .findFirst()
                .orElse("Long");
        }
        return "CompositeKey";
    }

    public ColumnMeta getPrimaryKeyColumn() {
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            return null;
        }
        if (primaryKeys.size() == 1) {
            return columns.stream()
                .filter(c -> c.getName().equals(primaryKeys.get(0)))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    public ColumnMeta getColumn(String columnName) {
        return columns.stream()
            .filter(c -> c.getName().equalsIgnoreCase(columnName))
            .findFirst()
            .orElse(null);
    }

    public List<ColumnMeta> getNonPrimaryKeyColumns() {
        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }
        return columns.stream()
            .filter(c -> !primaryKeys.contains(c.getName()))
            .collect(Collectors.toList());
    }

    public List<ColumnMeta> getInsertableColumns() {
        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }
        return columns.stream()
            .filter(c -> !(Boolean.TRUE.equals(c.getIsAutoIncrement()) && primaryKeys.contains(c.getName())))
            .collect(Collectors.toList());
    }
}