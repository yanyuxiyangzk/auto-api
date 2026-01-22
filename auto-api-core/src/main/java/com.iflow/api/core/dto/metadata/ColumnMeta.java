package com.iflow.api.core.dto.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 字段元数据 DTO
 * 
 * 引用：REQ-F3-003（读取表结构信息）
 *        REQ-F3-004（识别主键、字段类型）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ColumnMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private String typeName;
    private String javaType;
    private Integer jdbcType;
    private Integer ordinalPosition;
    private String defaultValue;
    private Boolean isNullable;
    private Boolean isPrimaryKey;
    private Boolean isUniqueKey;
    private Boolean isForeignKey;
    private Boolean isAutoIncrement;
    private Boolean isIndexed;
    private Integer length;
    private Integer scale;
    private Integer precision;
    private String comment;
    private String charset;
    private String collation;
    private String[] enumValues;

    // 添加兼容性 setter 方法（DatabaseMetaData 风格，接受原始类型）
    public void setColumnSize(int columnSize) {
        this.length = columnSize;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.scale = decimalDigits;
    }

    public void setNullable(boolean nullable) {
        this.isNullable = nullable;
    }

    public void setColumnDef(String columnDef) {
        this.defaultValue = columnDef;
    }

    public void setRemarks(String remarks) {
        this.comment = remarks;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.isAutoIncrement = autoIncrement;
    }

    // 添加兼容性 getter 方法
    public boolean isNullable() {
        return Boolean.TRUE.equals(isNullable);
    }

    public boolean isPrimaryKey() {
        return Boolean.TRUE.equals(isPrimaryKey);
    }

    public boolean isAutoIncrement() {
        return Boolean.TRUE.equals(isAutoIncrement);
    }

    public String toGraphQLType() {
        if (javaType == null) return "String";
        switch (javaType) {
            case "Long": case "Integer": case "Short": case "Byte": return "Int";
            case "Float": case "Double": case "BigDecimal": return "Float";
            case "Boolean": return "Boolean";
            case "String": case "Character": return "String";
            case "LocalDateTime": case "LocalDate": case "LocalTime": return "String";
            case "byte[]": return "String";
            default: return "String";
        }
    }

    public String toGraphQLNonNullType() {
        return toGraphQLType() + "!";
    }

    public boolean isNumericType() {
        return javaType != null && (
            javaType.equals("Long") || javaType.equals("Integer") ||
            javaType.equals("Short") || javaType.equals("Byte") ||
            javaType.equals("Float") || javaType.equals("Double") ||
            javaType.equals("BigDecimal")
        );
    }

    public boolean isStringType() {
        return javaType != null && (javaType.equals("String") || javaType.equals("Character"));
    }

    public boolean isDateTimeType() {
        return javaType != null && (
            javaType.equals("LocalDateTime") || javaType.equals("LocalDate") || javaType.equals("LocalTime")
        );
    }
}
