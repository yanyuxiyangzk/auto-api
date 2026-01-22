package com.iflow.api.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy 脚本引擎
 * 
 * 引用：REQ-F3-001（支持 Groovy 脚本）
 *        REQ-F3-003（脚本热部署）
 */
@Slf4j
@Component
public class GroovyScriptEngine {

    @Autowired
    private GroovySandbox sandbox;

    /**
     * Groovy 类加载器
     */
    private GroovyClassLoader classLoader;

    /**
     * 脚本缓存 (脚本名称 -> 编译后的 Class)
     */
    private final Map<String, Class<?>> scriptCache = new ConcurrentHashMap<>();

    /**
     * 脚本实例缓存 (脚本名称 -> Script 实例)
     */
    private final Map<String, Script> scriptInstances = new ConcurrentHashMap<>();

    /**
     * 脚本文件目录
     */
    private String scriptDirectory = "./scripts";

    @PostConstruct
    public void init() {
        classLoader = new GroovyClassLoader();
        // 创建脚本目录
        new File(scriptDirectory).mkdirs();
        log.info("Groovy 脚本引擎初始化完成: directory={}", scriptDirectory);
    }

    /**
     * 编译并执行脚本
     * 
     * @param scriptCode Groovy 脚本代码
     * @param bindings 变量绑定
     * @return 执行结果
     */
    public Object execute(String scriptCode, Map<String, Object> bindings) {
        // 安全检查
        if (!sandbox.isSafe(scriptCode)) {
            throw new SecurityException("脚本包含不安全的代码");
        }

        Binding binding = new Binding();
        if (bindings != null) {
            bindings.forEach(binding::setVariable);
        }

        try {
            // 编译脚本
            Class<?> scriptClass = parseScript(scriptCode);
            Script script = (Script) scriptClass.getDeclaredConstructor().newInstance();
            script.setBinding(binding);

            // 执行并返回结果
            Object result = script.run();

            log.debug("Groovy 脚本执行成功");
            return result;

        } catch (Exception e) {
            log.error("Groovy 脚本执行失败", e);
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 编译脚本
     * 
     * @param scriptCode 脚本代码
     * @return 编译后的 Class
     */
    public Class<?> parseScript(String scriptCode) {
        // 生成脚本名称（使用 hash）
        String scriptName = "Script_" + Integer.toHexString(scriptCode.hashCode());

        // 检查缓存
        Class<?> cachedClass = scriptCache.get(scriptName);
        if (cachedClass != null) {
            return cachedClass;
        }

        try {
            // 使用沙箱编译
            Class<?> scriptClass = sandbox.compileWithSandbox(scriptCode);

            // 缓存
            scriptCache.put(scriptName, scriptClass);

            log.debug("Groovy 脚本编译成功: name={}", scriptName);
            return scriptClass;

        } catch (Exception e) {
            log.error("Groovy 脚本编译失败", e);
            throw new RuntimeException("脚本编译失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从文件加载并执行脚本
     * 
     * @param filePath 脚本文件路径
     * @param bindings 变量绑定
     * @return 执行结果
     */
    public Object executeFromFile(String filePath, Map<String, Object> bindings) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("脚本文件不存在: " + filePath);
        }

        String scriptCode = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        return execute(scriptCode, bindings);
    }

    /**
     * 加载脚本类
     * 
     * @param scriptCode 脚本代码
     * @return 脚本类
     */
    public Class<?> loadScriptClass(String scriptCode) {
        return parseScript(scriptCode);
    }

    /**
     * 创建脚本实例
     * 
     * @param scriptCode 脚本代码
     * @return Script 实例
     */
    public Script newScriptInstance(String scriptCode) {
        Class<?> scriptClass = parseScript(scriptCode);

        try {
            Script script = (Script) scriptClass.getDeclaredConstructor().newInstance();
            String scriptName = "Script_" + Integer.toHexString(scriptCode.hashCode());
            scriptInstances.put(scriptName, script);
            return script;
        } catch (Exception e) {
            log.error("创建脚本实例失败", e);
            throw new RuntimeException("创建脚本实例失败", e);
        }
    }

    /**
     * 编译脚本类（支持定义方法和类）
     * 
     * @param scriptCode 脚本代码
     * @return 编译后的 Class
     */
    public Class<?> parseScriptClass(String scriptCode) {
        // 检查是否为脚本类（包含 class 关键字）
        if (scriptCode.contains("class ")) {
            // 编译为类
            return classLoader.parseClass(scriptCode);
        }
        // 编译为脚本
        return parseScript(scriptCode);
    }

    /**
     * 调用脚本中的方法
     * 
     * @param scriptCode 脚本代码
     * @param methodName 方法名
     * @param args 参数
     * @return 执行结果
     */
    public Object invokeMethod(String scriptCode, String methodName, Object... args) {
        Class<?> scriptClass = parseScript(scriptCode);
        Script script;

        try {
            script = (Script) scriptClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("创建脚本实例失败", e);
            throw new RuntimeException("创建脚本实例失败", e);
        }

        try {
            return script.invokeMethod(methodName, args);
        } catch (Exception e) {
            log.error("调用脚本方法失败: method={}", methodName, e);
            throw new RuntimeException("调用方法失败: " + e.getMessage(), e);
        }
    }

    /**
     * 定义脚本类
     * 
     * @param scriptCode 包含类定义的脚本代码
     * @param className 类名
     * @return 定义类
     */
    public Class<?> defineClass(String scriptCode, String className) {
        try {
            // 添加类定义包装
            String wrappedCode = scriptCode;
            if (!scriptCode.contains("class " + className)) {
                wrappedCode = "class " + className + " {\n" + scriptCode + "\n}";
            }

            return classLoader.parseClass(wrappedCode);
        } catch (Exception e) {
            log.error("定义脚本类失败: className={}", className, e);
            throw new RuntimeException("定义类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建脚本类的实例
     * 
     * @param scriptCode 脚本代码
     * @param className 类名
     * @return 实例对象
     */
    public Object newInstance(String scriptCode, String className) {
        try {
            Class<?> clazz = defineClass(scriptCode, className);
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("创建脚本类实例失败: className={}", className, e);
            throw new RuntimeException("创建实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置脚本目录
     */
    public void setScriptDirectory(String directory) {
        this.scriptDirectory = directory;
        new File(directory).mkdirs();
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        scriptCache.clear();
        scriptInstances.clear();
        log.info("Groovy 脚本缓存已清空");
    }

    /**
     * 清空指定脚本的缓存
     */
    public void clearCache(String scriptName) {
        scriptCache.remove(scriptName);
        scriptInstances.remove(scriptName);
    }

    /**
     * 获取缓存的脚本数量
     */
    public int getCacheSize() {
        return scriptCache.size();
    }

    /**
     * 检查脚本是否存在
     */
    public boolean hasScript(String scriptCode) {
        String scriptName = "Script_" + Integer.toHexString(scriptCode.hashCode());
        return scriptCache.containsKey(scriptName);
    }
}
