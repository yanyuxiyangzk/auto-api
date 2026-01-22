package com.iflow.api.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Auto-API-Generator 独立启动应用
 * 用于演示和测试组件功能
 */
@SpringBootApplication(scanBasePackages = {
    "com.iflow.api.core",
    "com.iflow.api.starter"
})
public class AutoApiStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoApiStarterApplication.class, args);
    }
}
