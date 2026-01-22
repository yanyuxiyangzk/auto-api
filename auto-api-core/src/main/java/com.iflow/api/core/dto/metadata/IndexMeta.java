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
 * 索引元数据 DTO
 * 
 * 引用：REQ-F3-003（读取表结构信息）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class IndexMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private String indexName;
    private String tableName;
    private Boolean nonUnique;
    private String indexType;
    private String comment;
    private List<IndexColumn> columns = new ArrayList<>();

    public static class IndexType {
        public static final String BTREE = "BTREE";
        public static final String HASH = "HASH";
        public static final String FULLTEXT = "FULLTEXT";
        public static final String SPATIAL = "SPATIAL";
    }

    @Data
    public static class IndexColumn implements Serializable {
        private static final long serialVersionUID = 1L;
        private String columnName;
        private Integer ordinalPosition;
        private String sortOrder;
        private Long cardinality;
    }

    // 添加兼容性 setter 方法（DatabaseMetaData 风格）
    public void setName(String name) {
        this.indexName = name;
    }

    public void setColumns(List<IndexColumn> columns) {
        this.columns = columns;
    }

    public boolean isUnique() {
        return !Boolean.TRUE.equals(nonUnique);
    }

    public boolean isFullText() {
        return IndexType.FULLTEXT.equalsIgnoreCase(indexType);
    }

    public boolean isSpatial() {
        return IndexType.SPATIAL.equalsIgnoreCase(indexType);
    }

    public List<String> getColumnNames() {
        if (columns == null || columns.isEmpty()) {
            return Collections.emptyList();
        }
        return columns.stream()
            .map(IndexColumn::getColumnName)
            .collect(Collectors.toList());
    }
}
