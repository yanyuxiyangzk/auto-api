package com.iflow.api.core.dto.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 外键元数据 DTO
 * 
 * 引用：REQ-F3-003（读取表结构信息）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ForeignKeyMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fkName;
    private String fkColumnName;
    private String pkTableName;
    private String pkColumnName;
    private String onUpdateRule;
    private String onDeleteRule;
    private String fkTableName;  // 外键所在表名

    public static class OnDeleteRule {
        public static final String CASCADE = "CASCADE";
        public static final String SET_NULL = "SET NULL";
        public static final String RESTRICT = "RESTRICT";
        public static final String NO_ACTION = "NO ACTION";
        public static final String SET_DEFAULT = "SET DEFAULT";
    }

    public static class OnUpdateRule {
        public static final String CASCADE = "CASCADE";
        public static final String SET_NULL = "SET NULL";
        public static final String RESTRICT = "RESTRICT";
        public static final String NO_ACTION = "NO ACTION";
        public static final String SET_DEFAULT = "SET DEFAULT";
    }

    // 添加兼容性 setter 方法（DatabaseMetaData 风格）
    public void setDeleteRule(String rule) {
        this.onDeleteRule = rule;
    }

    public void setUpdateRule(String rule) {
        this.onUpdateRule = rule;
    }

    public String getDescription() {
        return String.format("%s(%s) -> %s(%s)", 
            fkTableName, fkColumnName, pkTableName, pkColumnName);
    }

    public boolean isCascadeDelete() {
        return OnDeleteRule.CASCADE.equals(onDeleteRule);
    }

    public boolean isCascadeUpdate() {
        return OnUpdateRule.CASCADE.equals(onUpdateRule);
    }

    // 添加 getFkTableName() 方法
    public String getFkTableName() {
        return fkTableName;
    }
}
