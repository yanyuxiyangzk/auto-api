package com.iflow.api.starter;

/**
 * Auto-API-Generator 运行时配置类
 * 
 * 引用：REQ-F11-004（组件应支持自定义配置）
 */
public class AutoApiConfig {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * API 路径前缀
     */
    private String apiPrefix;

    /**
     * 是否启用 REST API
     */
    private Boolean enableRest;

    /**
     * 是否启用 GraphQL API
     */
    private Boolean enableGraphql;

    /**
     * 是否启用 Groovy 脚本
     */
    private Boolean enableGroovy;

    // ========== Getter/Setter ==========

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public Boolean getEnableRest() {
        return enableRest;
    }

    public void setEnableRest(Boolean enableRest) {
        this.enableRest = enableRest;
    }

    public Boolean getEnableGraphql() {
        return enableGraphql;
    }

    public void setEnableGraphql(Boolean enableGraphql) {
        this.enableGraphql = enableGraphql;
    }

    public Boolean getEnableGroovy() {
        return enableGroovy;
    }

    public void setEnableGroovy(Boolean enableGroovy) {
        this.enableGroovy = enableGroovy;
    }
}
