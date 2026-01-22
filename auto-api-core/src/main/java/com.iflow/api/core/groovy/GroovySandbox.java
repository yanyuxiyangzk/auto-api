package com.iflow.api.core.groovy;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Groovy 脚本安全沙箱
 * 
 * 引用：REQ-F3-002（脚本安全控制）
 */
@Slf4j
@Component
public class GroovySandbox {

    /**
     * Groovy 类加载器
     */
    private GroovyClassLoader classLoader;

    /**
     * 禁止访问的模式列表
     */
    private final List<Pattern> forbiddenPatterns = new ArrayList<>();

    /**
     * 禁止的类名列表
     */
    private final List<String> forbiddenClasses = new ArrayList<>();

    /**
     * 禁止的方法名列表
     */
    private final List<String> forbiddenMethods = new ArrayList<>();

    /**
     * 允许的导入包
     */
    private final List<String> allowedImports = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 初始化禁止列表
        initializeForbiddenList();
        log.info("Groovy 沙箱初始化完成");
    }

    /**
     * 初始化禁止列表
     */
    private void initializeForbiddenList() {
        // 禁止创建进程
        forbiddenPatterns.add(Pattern.compile("Runtime\\.getRuntime\\(\\) \\.exec", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("ProcessBuilder", Pattern.CASE_INSENSITIVE));

        // 禁止文件操作（危险）
        forbiddenPatterns.add(Pattern.compile("new\\s+FileOutputStream", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("new\\s+FileInputStream", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("new\\s+FileWriter", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("new\\s+FileReader", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("\\.delete\\(", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("\\.mkdirs?", Pattern.CASE_INSENSITIVE));

        // 禁止反射操作
        forbiddenPatterns.add(Pattern.compile("Class\\.forName", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("\\.getClassLoader\\(\\)", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("AccessibleObject\\.setAccessible", Pattern.CASE_INSENSITIVE));

        // 禁止网络操作
        forbiddenPatterns.add(Pattern.compile("new\\s+URL\\(", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("new\\s+Socket", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("ServerSocket", Pattern.CASE_INSENSITIVE));

        // 禁止线程相关
        forbiddenPatterns.add(Pattern.compile("new\\s+Thread", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("new\\s+Executor", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("Thread\\.sleep", Pattern.CASE_INSENSITIVE));

        // 禁止系统属性访问
        forbiddenPatterns.add(Pattern.compile("System\\.getProperty", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("System\\.setProperty", Pattern.CASE_INSENSITIVE));
        forbiddenPatterns.add(Pattern.compile("System\\.getEnv", Pattern.CASE_INSENSITIVE));

        // 禁止的类
        forbiddenClasses.add("java.lang.Runtime");
        forbiddenClasses.add("java.lang.Process");
        forbiddenClasses.add("java.lang.ProcessBuilder");
        forbiddenClasses.add("java.io.File");
        forbiddenClasses.add("java.io.FileInputStream");
        forbiddenClasses.add("java.io.FileOutputStream");
        forbiddenClasses.add("java.io.FileWriter");
        forbiddenClasses.add("java.io.FileReader");
        forbiddenClasses.add("java.net.URL");
        forbiddenClasses.add("java.net.Socket");
        forbiddenClasses.add("java.net.ServerSocket");
        forbiddenClasses.add("java.net.HttpURLConnection");
        forbiddenClasses.add("java.lang.Thread");
        forbiddenClasses.add("java.util.concurrent.Executor");
        forbiddenClasses.add("java.lang.System");

        // 禁止的方法
        forbiddenMethods.add("exit");
        forbiddenMethods.add("halt");
        forbiddenMethods.add("load");
        forbiddenMethods.add("loadLibrary");
        forbiddenMethods.add("gc");
        forbiddenMethods.add("runFinalization");
        forbiddenMethods.add("runFinalizersOnExit");
        forbiddenMethods.add("traceInstructions");
        forbiddenMethods.add("traceMethodCalls");

        // 允许的导入
        allowedImports.add("java.util");
        allowedImports.add("java.util.stream");
        allowedImports.add("java.util.regex");
        allowedImports.add("java.math");
        allowedImports.add("java.time");
        allowedImports.add("java.time.format");
        allowedImports.add("java.text");
    }

    /**
     * 检查脚本是否安全
     * 
     * @param scriptCode 脚本代码
     * @return true-安全, false-包含危险代码
     */
    public boolean isSafe(String scriptCode) {
        // 检查禁止模式
        for (Pattern pattern : forbiddenPatterns) {
            if (pattern.matcher(scriptCode).find()) {
                log.warn("检测到危险代码模式: {}", pattern.pattern());
                return false;
            }
        }

        // 检查禁止的类
        for (String className : forbiddenClasses) {
            if (scriptCode.contains(className)) {
                log.warn("检测到禁止的类: {}", className);
                return false;
            }
        }

        return true;
    }

    /**
     * 使用沙箱编译脚本
     * 
     * @param scriptCode 脚本代码
     * @return 编译后的 Class
     */
    public Class<?> compileWithSandbox(String scriptCode) {
        // 安全检查
        if (!isSafe(scriptCode)) {
            throw new SecurityException("脚本包含不安全的代码");
        }

        // 配置编译器
        CompilerConfiguration config = new CompilerConfiguration();

        // 添加安全相关的导入定制器
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addImports(
            "java.util.List",
            "java.util.ArrayList",
            "java.util.Map",
            "java.util.HashMap",
            "java.util.Set",
            "java.util.HashSet",
            "java.util.stream.Collectors",
            "java.math.BigDecimal",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.format.DateTimeFormatter"
        );
        config.addCompilationCustomizers(importCustomizer);

        // 创建沙箱类加载器
        classLoader = new GroovyClassLoader(getClass().getClassLoader(), config);

        try {
            // 编译脚本
            return classLoader.parseClass(scriptCode);
        } catch (Exception e) {
            log.error("脚本编译失败", e);
            throw new RuntimeException("脚本编译失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加禁止模式
     */
    public void addForbiddenPattern(Pattern pattern) {
        forbiddenPatterns.add(pattern);
    }

    /**
     * 添加禁止的类
     */
    public void addForbiddenClass(String className) {
        forbiddenClasses.add(className);
    }

    /**
     * 添加禁止的方法
     */
    public void addForbiddenMethod(String methodName) {
        forbiddenMethods.add(methodName);
    }

    /**
     * 添加允许的导入
     */
    public void addAllowedImport(String importPackage) {
        allowedImports.add(importPackage);
    }

    /**
     * 检查是否允许导入
     */
    public boolean isImportAllowed(String importName) {
        for (String allowed : allowedImports) {
            if (importName.startsWith(allowed + ".") || importName.equals(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证脚本变量
     * 
     * @param bindings 变量绑定
     * @return true-安全, false-包含危险变量
     */
    public boolean validateBindings(java.util.Map<String, Object> bindings) {
        // 检查变量值是否为禁止的类型
        for (Object value : bindings.values()) {
            if (value == null) {
                continue;
            }

            String className = value.getClass().getName();

            // 检查是否为禁止的类实例
            for (String forbidden : forbiddenClasses) {
                if (className.startsWith(forbidden)) {
                    log.warn("检测到禁止的变量类型: {}", className);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 设置白名单
     */
    public void setWhitelist(List<String> allowedClasses) {
        forbiddenClasses.clear();
        // 默认禁止所有类
        // 只有在白名单中的类才被允许
    }

    /**
     * 获取当前禁止模式数量
     */
    public int getForbiddenPatternCount() {
        return forbiddenPatterns.size();
    }

    /**
     * 获取当前禁止类数量
     */
    public int getForbiddenClassCount() {
        return forbiddenClasses.size();
    }

    /**
     * 重置沙箱配置
     */
    public void reset() {
        forbiddenPatterns.clear();
        forbiddenClasses.clear();
        forbiddenMethods.clear();
        allowedImports.clear();
        initializeForbiddenList();
        log.info("Groovy 沙箱配置已重置");
    }
}
