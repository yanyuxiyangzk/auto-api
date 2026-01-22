package com.iflow.api.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 类型转换器测试
 */
class TypeConverterTest {

    @Test
    void testMySQLTypeConversion() {
        // 整数类型
        assertEquals("Integer", TypeConverter.toJavaType("mysql", "INT"));
        assertEquals("Integer", TypeConverter.toJavaType("mysql", "INTEGER"));
        assertEquals("Long", TypeConverter.toJavaType("mysql", "BIGINT"));
        assertEquals("Short", TypeConverter.toJavaType("mysql", "SMALLINT"));
        assertEquals("Byte", TypeConverter.toJavaType("mysql", "TINYINT"));

        // 浮点类型
        assertEquals("Float", TypeConverter.toJavaType("mysql", "FLOAT"));
        assertEquals("Double", TypeConverter.toJavaType("mysql", "DOUBLE"));
        assertEquals("BigDecimal", TypeConverter.toJavaType("mysql", "DECIMAL"));
        assertEquals("BigDecimal", TypeConverter.toJavaType("mysql", "NUMERIC"));

        // 字符串类型
        assertEquals("String", TypeConverter.toJavaType("mysql", "VARCHAR"));
        assertEquals("String", TypeConverter.toJavaType("mysql", "CHAR"));
        assertEquals("String", TypeConverter.toJavaType("mysql", "TEXT"));
        assertEquals("String", TypeConverter.toJavaType("mysql", "LONGTEXT"));
        assertEquals("String", TypeConverter.toJavaType("mysql", "JSON"));

        // 日期时间类型
        assertEquals("Date", TypeConverter.toJavaType("mysql", "DATE"));
        assertEquals("LocalTime", TypeConverter.toJavaType("mysql", "TIME"));
        assertEquals("LocalDateTime", TypeConverter.toJavaType("mysql", "DATETIME"));
        assertEquals("LocalDateTime", TypeConverter.toJavaType("mysql", "TIMESTAMP"));

        // 布尔类型
        assertEquals("Boolean", TypeConverter.toJavaType("mysql", "BOOLEAN"));
        assertEquals("Boolean", TypeConverter.toJavaType("mysql", "TINYINT(1)"));

        // 二进制类型
        assertEquals("byte[]", TypeConverter.toJavaType("mysql", "BLOB"));
        assertEquals("byte[]", TypeConverter.toJavaType("mysql", "VARBINARY"));
    }

    @Test
    void testPostgreSQLTypeConversion() {
        // 整数类型
        assertEquals("Integer", TypeConverter.toJavaType("postgresql", "INTEGER"));
        assertEquals("Integer", TypeConverter.toJavaType("postgresql", "INT"));
        assertEquals("Long", TypeConverter.toJavaType("postgresql", "BIGINT"));
        assertEquals("Short", TypeConverter.toJavaType("postgresql", "SMALLINT"));

        // 浮点类型
        assertEquals("Double", TypeConverter.toJavaType("postgresql", "DOUBLE PRECISION"));
        assertEquals("BigDecimal", TypeConverter.toJavaType("postgresql", "NUMERIC"));
        assertEquals("BigDecimal", TypeConverter.toJavaType("postgresql", "DECIMAL"));

        // 字符串类型
        assertEquals("String", TypeConverter.toJavaType("postgresql", "VARCHAR"));
        assertEquals("String", TypeConverter.toJavaType("postgresql", "TEXT"));
        assertEquals("String", TypeConverter.toJavaType("postgresql", "CHAR"));

        // 日期时间类型
        assertEquals("Date", TypeConverter.toJavaType("postgresql", "DATE"));
        assertEquals("LocalTime", TypeConverter.toJavaType("postgresql", "TIME"));
        assertEquals("LocalDateTime", TypeConverter.toJavaType("postgresql", "TIMESTAMP"));
        assertEquals("LocalDateTime", TypeConverter.toJavaType("postgresql", "TIMESTAMPTZ"));

        // 布尔类型
        assertEquals("Boolean", TypeConverter.toJavaType("postgresql", "BOOLEAN"));

        // 数组类型
        assertEquals("String", TypeConverter.toJavaType("postgresql", "TEXT[]"));
        assertEquals("Integer", TypeConverter.toJavaType("postgresql", "INTEGER[]"));
    }

    @Test
    void testToDefaultValue() {
        assertEquals(0, TypeConverter.toDefaultValue("Integer"));
        assertEquals(0L, TypeConverter.toDefaultValue("Long"));
        assertEquals(0.0f, TypeConverter.toDefaultValue("Float"));
        assertEquals(0.0, TypeConverter.toDefaultValue("Double"));
        assertEquals(false, TypeConverter.toDefaultValue("Boolean"));
        assertNull(TypeConverter.toDefaultValue("String"));
        assertNull(TypeConverter.toDefaultValue("Date"));
    }

    @Test
    void testIsNumericType() {
        assertTrue(TypeConverter.isNumericType("Integer"));
        assertTrue(TypeConverter.isNumericType("Long"));
        assertTrue(TypeConverter.isNumericType("Short"));
        assertTrue(TypeConverter.isNumericType("Byte"));
        assertTrue(TypeConverter.isNumericType("Float"));
        assertTrue(TypeConverter.isNumericType("Double"));
        assertTrue(TypeConverter.isNumericType("BigDecimal"));

        assertFalse(TypeConverter.isNumericType("String"));
        assertFalse(TypeConverter.isNumericType("Boolean"));
        assertFalse(TypeConverter.isNumericType("Date"));
    }

    @Test
    void testUnknownType() {
        assertEquals("String", TypeConverter.toJavaType("mysql", "UNKNOWN_TYPE"));
        assertEquals("String", TypeConverter.toJavaType("unknown", "SOME_TYPE"));
    }
}
