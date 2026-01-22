package com.iflow.api.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 命名转换器测试
 */
class NamingConverterTest {

    @Test
    void testToCamelCase() {
        assertEquals("userName", NamingConverter.toCamelCase("user_name"));
        assertEquals("userName", NamingConverter.toCamelCase("USER_NAME"));
        assertEquals("user", NamingConverter.toCamelCase("user"));
        assertEquals("", NamingConverter.toCamelCase(""));
        assertNull(NamingConverter.toCamelCase(null));
    }

    @Test
    void testToPascalCase() {
        assertEquals("UserName", NamingConverter.toPascalCase("user_name"));
        assertEquals("UserName", NamingConverter.toPascalCase("USER_NAME"));
        assertEquals("User", NamingConverter.toPascalCase("user"));
        assertEquals("", NamingConverter.toPascalCase(""));
    }

    @Test
    void testToSnakeCase() {
        assertEquals("user_name", NamingConverter.toSnakeCase("userName"));
        assertEquals("user_name", NamingConverter.toSnakeCase("UserName"));
        assertEquals("user", NamingConverter.toSnakeCase("user"));
        assertEquals("", NamingConverter.toSnakeCase(""));
    }

    @Test
    void testToUpperSnakeCase() {
        assertEquals("USER_NAME", NamingConverter.toUpperSnakeCase("userName"));
        assertEquals("USER", NamingConverter.toUpperSnakeCase("user"));
    }

    @Test
    void testToKebabCase() {
        assertEquals("user-name", NamingConverter.toKebabCase("userName"));
        assertEquals("user-name", NamingConverter.toKebabCase("UserName"));
        assertEquals("user", NamingConverter.toKebabCase("user"));
    }

    @Test
    void testToApiPath() {
        assertEquals("/api/user-info", NamingConverter.toApiPath("user_info"));
        assertEquals("/api/user-info", NamingConverter.toApiPath("userInfo"));
    }

    @Test
    void testToGraphQLTypeName() {
        assertEquals("UserInfo", NamingConverter.toGraphQLTypeName("user_info"));
        assertEquals("UserInfo", NamingConverter.toGraphQLTypeName("userInfo"));
    }

    @Test
    void testToGraphQLFieldName() {
        assertEquals("userName", NamingConverter.toGraphQLFieldName("user_name"));
        assertEquals("userName", NamingConverter.toGraphQLFieldName("USER_NAME"));
    }

    @Test
    void testRemovePrefix() {
        assertEquals("user", NamingConverter.removePrefix("tbl_user", "tbl_"));
        assertEquals("user", NamingConverter.removePrefix("user", "tbl_"));
    }

    @Test
    void testRemoveSuffix() {
        assertEquals("tbl_user", NamingConverter.removeSuffix("tbl_user_table", "_table"));
        assertEquals("tbl_user", NamingConverter.removeSuffix("tbl_user", "_table"));
    }

    @Test
    void testCapitalize() {
        assertEquals("User", NamingConverter.capitalize("user"));
        assertEquals("USER", NamingConverter.capitalize("uSER"));
    }

    @Test
    void testUncapitalize() {
        assertEquals("user", NamingConverter.uncapitalize("User"));
        assertEquals("user", NamingConverter.uncapitalize("USER"));
    }

    @Test
    void testSanitizeName() {
        assertEquals("user_name_123", NamingConverter.sanitizeName("user-name_123!"));
        assertEquals("user", NamingConverter.sanitizeName("user"));
    }
}
