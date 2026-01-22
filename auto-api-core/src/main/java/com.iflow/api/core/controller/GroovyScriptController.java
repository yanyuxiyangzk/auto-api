package com.iflow.api.core.controller;

import com.iflow.api.core.dto.Result;
import com.iflow.api.core.groovy.GroovyScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Groovy 脚本控制器
 * 
 * 引用：REQ-F3-001（支持 Groovy 脚本）
 *        REQ-F3-003（脚本热部署）
 */
@Slf4j
@RestController
@RequestMapping("/api/script")
public class GroovyScriptController {

    @Autowired
    private GroovyScriptEngine scriptEngine;

    /**
     * 执行脚本
     */
    @PostMapping("/execute")
    public Result<Object> execute(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");
        @SuppressWarnings("unchecked")
        Map<String, Object> bindings = (Map<String, Object>) request.get("bindings");

        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }

        try {
            Object result = scriptEngine.execute(scriptCode, bindings);
            return Result.success(result);
        } catch (Exception e) {
            log.error("脚本执行失败", e);
            return Result.error("脚本执行失败: " + e.getMessage());
        }
    }

    /**
     * 编译脚本
     */
    @PostMapping("/compile")
    public Result<Map<String, Object>> compile(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");

        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }

        try {
            Class<?> scriptClass = scriptEngine.parseScript(scriptCode);
            Map<String, Object> result = new HashMap<>();
            result.put("className", scriptClass.getName());
            result.put("methods", java.util.Arrays.stream(scriptClass.getMethods())
                .map(m -> m.getName())
                .filter(name -> !name.startsWith("$") && !name.equals("run"))
                .toArray());
            return Result.success(result);
        } catch (Exception e) {
            log.error("脚本编译失败", e);
            return Result.error("脚本编译失败: " + e.getMessage());
        }
    }

    /**
     * 调用脚本方法
     */
    @PostMapping("/invoke")
    public Result<Object> invoke(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");
        String methodName = (String) request.get("method");
        @SuppressWarnings("unchecked")
        Object[] args = request.get("args") != null ? 
            ((java.util.List<?>) request.get("args")).toArray() : new Object[0];

        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }
        if (methodName == null || methodName.isEmpty()) {
            return Result.error("方法名不能为空");
        }

        try {
            Object result = scriptEngine.invokeMethod(scriptCode, methodName, args);
            return Result.success(result);
        } catch (Exception e) {
            log.error("方法调用失败: method={}", methodName, e);
            return Result.error("方法调用失败: " + e.getMessage());
        }
    }

    /**
     * 从文件加载并执行脚本
     */
    @PostMapping("/execute/file")
    public Result<Object> executeFromFile(@RequestBody Map<String, Object> request) {
        String filePath = (String) request.get("path");
        @SuppressWarnings("unchecked")
        Map<String, Object> bindings = (Map<String, Object>) request.get("bindings");

        if (filePath == null || filePath.isEmpty()) {
            return Result.error("文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return Result.error("脚本文件不存在: " + filePath);
        }

        try {
            Object result = scriptEngine.executeFromFile(filePath, bindings);
            return Result.success(result);
        } catch (IOException e) {
            log.error("读取脚本文件失败: path={}", filePath, e);
            return Result.error("读取脚本文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("脚本执行失败", e);
            return Result.error("脚本执行失败: " + e.getMessage());
        }
    }

    /**
     * 定义脚本类
     */
    @PostMapping("/define-class")
    public Result<Map<String, Object>> defineClass(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");
        String className = (String) request.get("className");

        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }
        if (className == null || className.isEmpty()) {
            return Result.error("类名不能为空");
        }

        try {
            Class<?> clazz = scriptEngine.defineClass(scriptCode, className);
            Map<String, Object> result = new HashMap<>();
            result.put("className", clazz.getName());
            result.put("simpleName", clazz.getSimpleName());
            return Result.success(result);
        } catch (Exception e) {
            log.error("定义类失败: className={}", className, e);
            return Result.error("定义类失败: " + e.getMessage());
        }
    }

    /**
     * 创建脚本类实例
     */
    @PostMapping("/new-instance")
    public Result<Object> newInstance(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");
        String className = (String) request.get("className");

        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }
        if (className == null || className.isEmpty()) {
            return Result.error("类名不能为空");
        }

        try {
            Object instance = scriptEngine.newInstance(scriptCode, className);
            return Result.success(instance);
        } catch (Exception e) {
            log.error("创建实例失败: className={}", className, e);
            return Result.error("创建实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计
     */
    @GetMapping("/cache/stats")
    public Result<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", scriptEngine.getCacheSize());
        return Result.success(stats);
    }

    /**
     * 清空脚本缓存
     */
    @PostMapping("/cache/clear")
    public Result<Void> clearCache() {
        scriptEngine.clearCache();
        return Result.success();
    }

    /**
     * 检查脚本是否已缓存
     */
    @PostMapping("/cache/check")
    public Result<Boolean> checkCache(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");
        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }
        boolean exists = scriptEngine.hasScript(scriptCode);
        return Result.success(exists);
    }

    /**
     * 设置脚本目录
     */
    @PostMapping("/directory")
    public Result<Void> setScriptDirectory(@RequestBody Map<String, Object> request) {
        String directory = (String) request.get("directory");
        if (directory == null || directory.isEmpty()) {
            return Result.error("脚本目录不能为空");
        }
        scriptEngine.setScriptDirectory(directory);
        return Result.success();
    }

    /**
     * 测试脚本安全
     */
    @PostMapping("/security/test")
    public Result<Map<String, Object>> testSecurity(@RequestBody Map<String, Object> request) {
        String scriptCode = (String) request.get("code");
        if (scriptCode == null || scriptCode.isEmpty()) {
            return Result.error("脚本代码不能为空");
        }

        // 简单测试
        boolean containsDangerous = scriptCode.toLowerCase().contains("runtime") ||
            scriptCode.toLowerCase().contains("exec") ||
            scriptCode.toLowerCase().contains("processbuilder");

        Map<String, Object> result = new HashMap<>();
        result.put("safe", !containsDangerous);
        result.put("containsDangerousPatterns", containsDangerous);

        return Result.success(result);
    }
}
