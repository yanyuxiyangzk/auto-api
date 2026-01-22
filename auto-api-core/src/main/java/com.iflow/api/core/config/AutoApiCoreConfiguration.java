package com.iflow.api.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDateTime;

/**
 * Auto-API-Generator 核心配置类
 * 
 * 引用：REQ-F11-001（打包为 Spring Boot Starter）
 */
@Configuration
@EnableAsync
public class AutoApiCoreConfiguration {

    /**
     * MyBatis-Plus 分页插件配置
     * 引用：REQ-F1-003（自动生成的接口应支持分页查询功能）
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件，支持 MySQL 和 PostgreSQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }

    /**
     * MyBatis-Plus 自动填充处理器
     * 用于自动填充 createdAt、updatedAt 等字段
     */
    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 自动填充创建时间
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 自动填充更新时间
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }

    /**
     * 应用配置 Bean
     * 引用：REQ-F11-001（配置属性）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "auto.api", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AppConfig appConfig() {
        return new AppConfig();
    }

    /**
     * 应用配置类
     * 用于存储运行时配置
     */
    public static class AppConfig {
        private Boolean enabled = true;
        private String apiPrefix = "/api";
        private Boolean enableRest = true;
        private Boolean enableGraphql = true;
        private Boolean enableGroovy = true;
        private Boolean enableHotDeploy = true;

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

        public Boolean getEnableHotDeploy() {
            return enableHotDeploy;
        }

        public void setEnableHotDeploy(Boolean enableHotDeploy) {
            this.enableHotDeploy = enableHotDeploy;
        }
    }
}