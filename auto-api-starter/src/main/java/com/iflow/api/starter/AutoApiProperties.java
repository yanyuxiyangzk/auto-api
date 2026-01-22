package com.iflow.api.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Auto-API-Generator 配置属性类
 * 
 * 引入方可以在 application.yml 中通过 auto.api.xxx 进行配置
 * 引用：REQ-F11-002（引入方应只需简单配置即可使用）
 */
@ConfigurationProperties(prefix = "auto.api")
@Validated
public class AutoApiProperties {

    /**
     * 是否启用自动 API 生成
     */
    @NotNull(message = "enabled 不能为空")
    private boolean enabled = true;

    /**
     * API 路径前缀
     */
    private String apiPrefix = "/api";

    /**
     * 是否启用 REST API
     */
    @NotNull(message = "enableRest 不能为空")
    private boolean enableRest = true;

    /**
     * 是否启用 GraphQL API
     */
    @NotNull(message = "enableGraphql 不能为空")
    private boolean enableGraphql = true;

    /**
     * 是否启用 Groovy 脚本
     */
    @NotNull(message = "enableGroovy 不能为空")
    private boolean enableGroovy = true;

    /**
     * 是否启用热部署
     */
    @NotNull(message = "enableHotDeploy 不能为空")
    private boolean enableHotDeploy = true;

    /**
     * 数据源配置
     */
    private DataSourceConfig datasource = new DataSourceConfig();

    /**
     * GraphQL 配置
     */
    private GraphqlConfig graphql = new GraphqlConfig();

    /**
     * Groovy 配置
     */
    private GroovyConfig groovy = new GroovyConfig();

    // ========== Getter/Setter ==========

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public boolean isEnableRest() {
        return enableRest;
    }

    public void setEnableRest(boolean enableRest) {
        this.enableRest = enableRest;
    }

    public boolean isEnableGraphql() {
        return enableGraphql;
    }

    public void setEnableGraphql(boolean enableGraphql) {
        this.enableGraphql = enableGraphql;
    }

    public boolean isEnableGroovy() {
        return enableGroovy;
    }

    public void setEnableGroovy(boolean enableGroovy) {
        this.enableGroovy = enableGroovy;
    }

    public boolean isEnableHotDeploy() {
        return enableHotDeploy;
    }

    public void setEnableHotDeploy(boolean enableHotDeploy) {
        this.enableHotDeploy = enableHotDeploy;
    }

    public DataSourceConfig getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSourceConfig datasource) {
        this.datasource = datasource;
    }

    public GraphqlConfig getGraphql() {
        return graphql;
    }

    public void setGraphql(GraphqlConfig graphql) {
        this.graphql = graphql;
    }

    public GroovyConfig getGroovy() {
        return groovy;
    }

    public void setGroovy(GroovyConfig groovy) {
        this.groovy = groovy;
    }

    // ========== 内部配置类 ==========

    /**
     * 数据源配置
     */
    public static class DataSourceConfig {
        /**
         * 默认主数据源类型 (mysql/postgresql)
         */
        private String defaultType = "mysql";

        /**
         * 是否允许动态添加数据源
         */
        private Boolean allowDynamicDatasource = true;

        public String getDefaultType() {
            return defaultType;
        }

        public void setDefaultType(String defaultType) {
            this.defaultType = defaultType;
        }

        public Boolean getAllowDynamicDatasource() {
            return allowDynamicDatasource;
        }

        public void setAllowDynamicDatasource(Boolean allowDynamicDatasource) {
            this.allowDynamicDatasource = allowDynamicDatasource;
        }
    }

    /**
     * GraphQL 配置
     */
    public static class GraphqlConfig {
        /**
         * GraphQL 端点路径
         */
        private String endpoint = "/graphql";

        /**
         * GraphiQL 界面是否启用
         */
        private Boolean graphiqlEnabled = true;

        /**
         * 是否允许查询 introspection
         */
        private Boolean introspectionEnabled = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Boolean getGraphiqlEnabled() {
            return graphiqlEnabled;
        }

        public void setGraphiqlEnabled(Boolean graphiqlEnabled) {
            this.graphiqlEnabled = graphiqlEnabled;
        }

        public Boolean getIntrospectionEnabled() {
            return introspectionEnabled;
        }

        public void setIntrospectionEnabled(Boolean introspectionEnabled) {
            this.introspectionEnabled = introspectionEnabled;
        }
    }

    /**
     * Groovy 脚本配置
     */
    public static class GroovyConfig {
        /**
         * 脚本执行超时时间 (毫秒)
         */
        private Long scriptTimeout = 5000L;

        /**
         * 是否启用沙箱安全限制
         */
        private Boolean sandboxEnabled = true;

        /**
         * 脚本缓存大小
         */
        private Integer scriptCacheSize = 100;

        /**
         * 脚本存储方式 (database/filesystem)
         */
        private String storageType = "database";

        public Long getScriptTimeout() {
            return scriptTimeout;
        }

        public void setScriptTimeout(Long scriptTimeout) {
            this.scriptTimeout = scriptTimeout;
        }

        public Boolean getSandboxEnabled() {
            return sandboxEnabled;
        }

        public void setSandboxEnabled(Boolean sandboxEnabled) {
            this.sandboxEnabled = sandboxEnabled;
        }

        public Integer getScriptCacheSize() {
            return scriptCacheSize;
        }

        public void setScriptCacheSize(Integer scriptCacheSize) {
            this.scriptCacheSize = scriptCacheSize;
        }

        public String getStorageType() {
            return storageType;
        }

        public void setStorageType(String storageType) {
            this.storageType = storageType;
        }
    }
}