package com.iflow.api.core.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 命名转换器
 * 实现下划线命名、驼峰命名、帕斯卡命名的互转
 * 
 * 引用：设计文档-组件设计（命名转换）
 */
@Slf4j
public class NamingConverter {

    /**
     * 下划线命名转驼峰命名
     * 
     * @param name 下划线命名 (如 user_name)
     * @return 驼峰命名 (如 userName)
     */
    public static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            if (c == '_') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                result.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }

    /**
     * 下划线命名转帕斯卡命名 (首字母大写的驼峰)
     * 
     * @param name 下划线命名 (如 user_name)
     * @return 帕斯卡命名 (如 UserName)
     */
    public static String toPascalCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String camelCase = toCamelCase(name);
        if (camelCase.isEmpty()) {
            return camelCase;
        }

        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    /**
     * 驼峰命名转下划线命名
     * 
     * @param name 驼峰命名 (如 userName)
     * @return 下划线命名 (如 user_name)
     */
    public static String toSnakeCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    /**
     * 帕斯卡命名转下划线命名
     * 
     * @param name 帕斯卡命名 (如 UserName)
     * @return 下划线命名 (如 user_name)
     */
    public static String toSnakeCaseFromPascal(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // 先转小写首字母，再按驼峰处理
        String camelCase = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return toSnakeCase(camelCase);
    }

    /**
     * 驼峰命名转大写下划线命名
     * 
     * @param name 驼峰命名 (如 userName)
     * @return 大写下划线命名 (如 USER_NAME)
     */
    public static String toUpperSnakeCase(String name) {
        return toSnakeCase(name).toUpperCase();
    }

    /**
     * 简单名称转换（移除特殊字符，仅保留字母数字）
     * 
     * @param name 原始名称
     * @return 清理后的名称
     */
    public static String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 生成表名对应的 REST API 路径
     * 
     * @param tableName 表名 (如 user_info)
     * @return API 路径 (如 /api/user-info)
     */
    public static String toApiPath(String tableName) {
        String camelCase = toCamelCase(tableName);
        // 将驼峰转为 kebab-case
        return toKebabCase(camelCase);
    }

    /**
     * 驼峰命名转短横线命名 (kebab-case)
     * 
     * @param name 驼峰命名 (如 userName)
     * @return 短横线命名 (如 user-name)
     */
    public static String toKebabCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    /**
     * 生成 GraphQL 类型名称
     * 
     * @param tableName 表名 (如 user_info)
     * @return GraphQL 类型名 (如 UserInfo)
     */
    public static String toGraphQLTypeName(String tableName) {
        return toPascalCase(tableName);
    }

    /**
     * 生成 GraphQL 字段名称
     * 
     * @param columnName 字段名 (如 user_name)
     * @return GraphQL 字段名 (如 userName)
     */
    public static String toGraphQLFieldName(String columnName) {
        return toCamelCase(columnName);
    }

    /**
     * 移除表名前缀
     * 
     * @param tableName 表名
     * @param prefix 前缀 (如 tbl_, api_)
     * @return 移除前缀后的表名
     */
    public static String removePrefix(String tableName, String prefix) {
        if (tableName == null || prefix == null) {
            return tableName;
        }

        if (tableName.toLowerCase().startsWith(prefix.toLowerCase())) {
            return tableName.substring(prefix.length());
        }
        return tableName;
    }

    /**
     * 移除表名后缀
     * 
     * @param tableName 表名
     * @param suffix 后缀 (如 _table, _info)
     * @return 移除后缀后的表名
     */
    public static String removeSuffix(String tableName, String suffix) {
        if (tableName == null || suffix == null) {
            return tableName;
        }

        if (tableName.toLowerCase().endsWith(suffix.toLowerCase())) {
            return tableName.substring(0, tableName.length() - suffix.length());
        }
        return tableName;
    }

    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 首字母小写
     * 
     * @param str 字符串
     * @return 首字母小写的字符串
     */
    public static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
