package com.iflow.api.starter;

import com.iflow.api.core.config.AutoApiCoreConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-API-Generator 自动配置类
 * 
 * 当引入方引入 auto-api-starter 依赖时，此配置自动生效
 * 引用：REQ-F11-001（打包为 Spring Boot Starter）
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.SpringApplication")
@EnableConfigurationProperties(AutoApiProperties.class)
@Import(AutoApiCoreConfiguration.class)
public class AutoApiAutoConfiguration {

    /**
     * 条件：Spring Boot 环境下自动配置
     */
    @Bean
    @ConditionalOnMissingBean
    public AutoApiConfig autoApiConfig(AutoApiProperties properties) {
        AutoApiConfig config = new AutoApiConfig();
        config.setEnabled(properties.isEnabled());
        config.setApiPrefix(properties.getApiPrefix());
        config.setEnableRest(properties.isEnableRest());
        config.setEnableGraphql(properties.isEnableGraphql());
        config.setEnableGroovy(properties.isEnableGroovy());
        return config;
    }
}
