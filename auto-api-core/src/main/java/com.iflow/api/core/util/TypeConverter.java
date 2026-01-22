package com.iflow.api.core.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库类型到 Java 类型转换器
 * 
 * 引用：REQ-F3-004（识别主键、字段类型）
 */
@Slf4j
public class TypeConverter {

    /**
     * MySQL 类型映射
     */
    private static final Map<String, String> MYSQL_TYPE_MAP = new HashMap<>();

    /**
     * PostgreSQL 类型映射
     */
    private static final Map<String, String> POSTGRESQL_TYPE_MAP = new HashMap<>();

    static {
        // MySQL 类型映射
        MYSQL_TYPE_MAP.put("TINYINT", "Integer");
        MYSQL_TYPE_MAP.put("SMALLINT", "Integer");
        MYSQL_TYPE_MAP.put("MEDIUMINT", "Integer");
        MYSQL_TYPE_MAP.put("INT", "Integer");
        MYSQL_TYPE_MAP.put("INTEGER", "Integer");
        MYSQL_TYPE_MAP.put("BIGINT", "Long");
        MYSQL_TYPE_MAP.put("FLOAT", "Float");
        MYSQL_TYPE_MAP.put("DOUBLE", "Double");
        MYSQL_TYPE_MAP.put("DECIMAL", "BigDecimal");
        MYSQL_TYPE_MAP.put("NUMERIC", "BigDecimal");
        MYSQL_TYPE_MAP.put("CHAR", "String");
        MYSQL_TYPE_MAP.put("VARCHAR", "String");
        MYSQL_TYPE_MAP.put("TINYTEXT", "String");
        MYSQL_TYPE_MAP.put("TEXT", "String");
        MYSQL_TYPE_MAP.put("MEDIUMTEXT", "String");
        MYSQL_TYPE_MAP.put("LONGTEXT", "String");
        MYSQL_TYPE_MAP.put("JSON", "String");
        MYSQL_TYPE_MAP.put("DATE", "LocalDate");
        MYSQL_TYPE_MAP.put("DATETIME", "LocalDateTime");
        MYSQL_TYPE_MAP.put("TIMESTAMP", "LocalDateTime");
        MYSQL_TYPE_MAP.put("TIME", "LocalTime");
        MYSQL_TYPE_MAP.put("YEAR", "Integer");
        MYSQL_TYPE_MAP.put("BIT", "Boolean");
        MYSQL_TYPE_MAP.put("BOOLEAN", "Boolean");
        MYSQL_TYPE_MAP.put("TINYINT(1)", "Boolean");
        MYSQL_TYPE_MAP.put("BINARY", "byte[]");
        MYSQL_TYPE_MAP.put("VARBINARY", "byte[]");
        MYSQL_TYPE_MAP.put("BLOB", "byte[]");
        MYSQL_TYPE_MAP.put("TINYBLOB", "byte[]");
        MYSQL_TYPE_MAP.put("MEDIUMBLOB", "byte[]");
        MYSQL_TYPE_MAP.put("LONGBLOB", "byte[]");
        MYSQL_TYPE_MAP.put("SET", "String");
        MYSQL_TYPE_MAP.put("ENUM", "String");
        MYSQL_TYPE_MAP.put("GEOMETRY", "String");
        MYSQL_TYPE_MAP.put("POINT", "String");
        MYSQL_TYPE_MAP.put("LINESTRING", "String");
        MYSQL_TYPE_MAP.put("POLYGON", "String");
        MYSQL_TYPE_MAP.put("MULTIPOINT", "String");
        MYSQL_TYPE_MAP.put("MULTILINESTRING", "String");
        MYSQL_TYPE_MAP.put("MULTIPOLYGON", "String");
        MYSQL_TYPE_MAP.put("GEOMETRYCOLLECTION", "String");

        // PostgreSQL 类型映射
        POSTGRESQL_TYPE_MAP.put("SMALLINT", "Integer");
        POSTGRESQL_TYPE_MAP.put("INT2", "Integer");
        POSTGRESQL_TYPE_MAP.put("INTEGER", "Integer");
        POSTGRESQL_TYPE_MAP.put("INT4", "Integer");
        POSTGRESQL_TYPE_MAP.put("BIGINT", "Long");
        POSTGRESQL_TYPE_MAP.put("INT8", "Long");
        POSTGRESQL_TYPE_MAP.put("REAL", "Float");
        POSTGRESQL_TYPE_MAP.put("FLOAT4", "Float");
        POSTGRESQL_TYPE_MAP.put("DOUBLE PRECISION", "Double");
        POSTGRESQL_TYPE_MAP.put("FLOAT8", "Double");
        POSTGRESQL_TYPE_MAP.put("NUMERIC", "BigDecimal");
        POSTGRESQL_TYPE_MAP.put("DECIMAL", "BigDecimal");
        POSTGRESQL_TYPE_MAP.put("MONEY", "BigDecimal");
        POSTGRESQL_TYPE_MAP.put("CHAR", "String");
        POSTGRESQL_TYPE_MAP.put("CHARACTER", "String");
        POSTGRESQL_TYPE_MAP.put("VARCHAR", "String");
        POSTGRESQL_TYPE_MAP.put("CHARACTER VARYING", "String");
        POSTGRESQL_TYPE_MAP.put("TEXT", "String");
        POSTGRESQL_TYPE_MAP.put("JSON", "String");
        POSTGRESQL_TYPE_MAP.put("JSONB", "String");
        POSTGRESQL_TYPE_MAP.put("XML", "String");
        POSTGRESQL_TYPE_MAP.put("DATE", "LocalDate");
        POSTGRESQL_TYPE_MAP.put("TIMESTAMP", "LocalDateTime");
        POSTGRESQL_TYPE_MAP.put("TIMESTAMP WITHOUT TIME ZONE", "LocalDateTime");
        POSTGRESQL_TYPE_MAP.put("TIMESTAMP WITH TIME ZONE", "LocalDateTime");
        POSTGRESQL_TYPE_MAP.put("TIME", "LocalTime");
        POSTGRESQL_TYPE_MAP.put("TIME WITHOUT TIME ZONE", "LocalTime");
        POSTGRESQL_TYPE_MAP.put("TIME WITH TIME ZONE", "LocalTime");
        POSTGRESQL_TYPE_MAP.put("INTERVAL", "String");
        POSTGRESQL_TYPE_MAP.put("BOOLEAN", "Boolean");
        POSTGRESQL_TYPE_MAP.put("BOOL", "Boolean");
        POSTGRESQL_TYPE_MAP.put("BYTEA", "byte[]");
        POSTGRESQL_TYPE_MAP.put("ARRAY", "String");
        POSTGRESQL_TYPE_MAP.put("UUID", "String");
        POSTGRESQL_TYPE_MAP.put("INET", "String");
        POSTGRESQL_TYPE_MAP.put("CIDR", "String");
        POSTGRESQL_TYPE_MAP.put("MACADDR", "String");
        POSTGRESQL_TYPE_MAP.put("POINT", "String");
        POSTGRESQL_TYPE_MAP.put("LINE", "String");
        POSTGRESQL_TYPE_MAP.put("LSEG", "String");
        POSTGRESQL_TYPE_MAP.put("BOX", "String");
        POSTGRESQL_TYPE_MAP.put("PATH", "String");
        POSTGRESQL_TYPE_MAP.put("POLYGON", "String");
        POSTGRESQL_TYPE_MAP.put("CIRCLE", "String");
        POSTGRESQL_TYPE_MAP.put("TSVECTOR", "String");
        POSTGRESQL_TYPE_MAP.put("TSQUERY", "String");
        POSTGRESQL_TYPE_MAP.put("REGCLASS", "String");
        POSTGRESQL_TYPE_MAP.put("BIT", "String");
        POSTGRESQL_TYPE_MAP.put("BIT VARYING", "String");
    }

    /**
     * 将数据库类型转换为 Java 类型
     * 
     * @param databaseType 数据库类型 (如 mysql, postgresql)
     * @param columnType 字段类型 (如 INT, VARCHAR)
     * @return Java 类型 (如 Long, String)
     */
    public static String toJavaType(String databaseType, String columnType) {
        if (columnType == null || columnType.trim().isEmpty()) {
            return "String";
        }

        // 统一转为大写
        String upperType = columnType.toUpperCase().trim();

        // 尝试精确匹配
        Map<String, String> typeMap = getTypeMap(databaseType);
        if (typeMap.containsKey(upperType)) {
            return typeMap.get(upperType);
        }

        // 尝试前缀匹配 (处理带长度的类型，如 VARCHAR(255))
        for (Map.Entry<String, String> entry : typeMap.entrySet()) {
            if (upperType.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        log.warn("未找到类型映射: databaseType={}, columnType={}, 默认为 String", 
            databaseType, columnType);
        return "String";
    }

    /**
     * 获取对应数据库类型的映射表
     */
    private static Map<String, String> getTypeMap(String databaseType) {
        if ("postgresql".equalsIgnoreCase(databaseType)) {
            return POSTGRESQL_TYPE_MAP;
        }
        // 默认使用 MySQL
        return MYSQL_TYPE_MAP;
    }

    /**
     * 将 Java 类型转换默认值
     * 
     * @param javaType Java 类型
     * @return 默认值
     */
    public static Object toDefaultValue(String javaType) {
        if (javaType == null) {
            return null;
        }

        switch (javaType) {
            case "Long":
                return 0L;
            case "Integer":
                return 0;
            case "Short":
                return (short) 0;
            case "Byte":
                return (byte) 0;
            case "Float":
                return 0.0f;
            case "Double":
                return 0.0;
            case "BigDecimal":
                return BigDecimal.ZERO;
            case "Boolean":
                return false;
            case "LocalDate":
                return LocalDate.now();
            case "LocalDateTime":
                return LocalDateTime.now();
            case "LocalTime":
                return LocalTime.now();
            default:
                return null;
        }
    }

    /**
     * 检查是否为数值类型
     */
    public static boolean isNumericType(String javaType) {
        if (javaType == null) {
            return false;
        }
        return javaType.equals("Long") ||
               javaType.equals("Integer") ||
               javaType.equals("Short") ||
               javaType.equals("Byte") ||
               javaType.equals("Float") ||
               javaType.equals("Double") ||
               javaType.equals("BigDecimal");
    }

    /**
     * 检查是否为字符串类型
     */
    public static boolean isStringType(String javaType) {
        if (javaType == null) {
            return false;
        }
        return javaType.equals("String") || javaType.equals("Character");
    }

    /**
     * 检查是否为日期时间类型
     */
    public static boolean isDateTimeType(String javaType) {
        if (javaType == null) {
            return false;
        }
        return javaType.equals("LocalDateTime") ||
               javaType.equals("LocalDate") ||
               javaType.equals("LocalTime");
    }

    /**
     * 检查是否为布尔类型
     */
    public static boolean isBooleanType(String javaType) {
        return "Boolean".equals(javaType);
    }

    /**
     * 检查是否为数组/字节类型
     */
    public static boolean isArrayType(String javaType) {
        if (javaType == null) {
            return false;
        }
        return javaType.endsWith("[]");
    }
}
