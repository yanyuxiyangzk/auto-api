package com.iflow.api.core.groovy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Groovy 脚本引擎测试
 */
class GroovyScriptEngineTest {

    private GroovyScriptEngine scriptEngine;
    private GroovySandbox sandbox;

    @BeforeEach
    void setUp() {
        sandbox = new GroovySandbox();
        sandbox.init();
        scriptEngine = new GroovyScriptEngine();
        scriptEngine.setSandbox(sandbox);
        scriptEngine.init();
    }

    @Test
    void testExecuteSimpleScript() {
        String script = "return 1 + 1";
        Object result = scriptEngine.execute(script, null);
        assertEquals(2, result);
    }

    @Test
    void testExecuteWithBindings() {
        String script = "return x + y";
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("x", 10);
        bindings.put("y", 20);
        
        Object result = scriptEngine.execute(script, bindings);
        assertEquals(30, result);
    }

    @Test
    void testExecuteWithStringBindings() {
        String script = "return name.toUpperCase()";
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("name", "test");
        
        Object result = scriptEngine.execute(script, bindings);
        assertEquals("TEST", result);
    }

    @Test
    void testExecuteListOperation() {
        String script = "return list.collect { it * 2 }";
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("list", java.util.Arrays.asList(1, 2, 3));
        
        Object result = scriptEngine.execute(script, bindings);
        assertEquals(java.util.Arrays.asList(2, 4, 6), result);
    }

    @Test
    void testExecuteMapOperation() {
        String script = "return map.name";
        Map<String, Object> bindings = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        bindings.put("map", map);
        
        Object result = scriptEngine.execute(script, bindings);
        assertEquals("test", result);
    }

    @Test
    void testExecuteWithClosure() {
        String script = "return [1, 2, 3].findAll { it > 1 }.sum()";
        Object result = scriptEngine.execute(script, null);
        assertEquals(5, result); // 2 + 3
    }

    @Test
    void testParseScript() {
        String script = "def greeting = 'Hello'; return greeting";
        Class<?> scriptClass = scriptEngine.parseScript(script);
        assertNotNull(scriptClass);
        assertEquals("groovy.lang.Script", scriptClass.getSuperclass().getName());
    }

    @Test
    void testHasScript() {
        String script = "return 1";
        assertFalse(scriptEngine.hasScript(script));
        
        scriptEngine.parseScript(script);
        assertTrue(scriptEngine.hasScript(script));
    }

    @Test
    void testGetCacheSize() {
        int initialSize = scriptEngine.getCacheSize();
        scriptEngine.parseScript("return 1");
        assertEquals(initialSize + 1, scriptEngine.getCacheSize());
    }

    @Test
    void testClearCache() {
        scriptEngine.parseScript("return 1");
        scriptEngine.parseScript("return 2");
        assertTrue(scriptEngine.getCacheSize() > 0);
        
        scriptEngine.clearCache();
        assertEquals(0, scriptEngine.getCacheSize());
    }

    @Test
    void testClearCacheByName() {
        String script1 = "return 1";
        String script2 = "return 2";
        
        scriptEngine.parseScript(script1);
        scriptEngine.parseScript(script2);
        int sizeAfterParse = scriptEngine.getCacheSize();
        
        scriptEngine.clearCache("Script_" + Integer.toHexString(script1.hashCode()));
        assertTrue(scriptEngine.getCacheSize() < sizeAfterParse);
    }

    @Test
    void testInvokeMethod() {
        String script = "def doubleValue(x) { return x * 2 }";
        Object result = scriptEngine.invokeMethod(script, "doubleValue", 5);
        assertEquals(10, result);
    }

    @Test
    void testDefineClass() {
        String script = "String getMessage() { return 'Hello' }";
        Class<?> clazz = scriptEngine.defineClass(script, "Greeter");
        assertNotNull(clazz);
        assertEquals("Greeter", clazz.getSimpleName());
    }

    @Test
    void testNewInstance() {
        String script = "int getValue() { return 42 }";
        Object instance = scriptEngine.newInstance(script, "Answer");
        assertNotNull(instance);
    }

    @Test
    void testScriptWithException() {
        String script = "throw new RuntimeException('test error')";
        assertThrows(RuntimeException.class, () -> scriptEngine.execute(script, null));
    }

    @Test
    void testUnsafeScriptBlocked() {
        String unsafeScript = "Runtime.getRuntime().exec('ls')";
        assertThrows(SecurityException.class, () -> scriptEngine.execute(unsafeScript, null));
    }

    @Test
    void testNewScriptInstance() {
        String script = "return 'test'";
        groovy.lang.Script scriptInstance = scriptEngine.newScriptInstance(script);
        assertNotNull(scriptInstance);
    }

    @Test
    void testParseScriptClass() {
        String classScript = "class Person { String name; int age }";
        Class<?> clazz = scriptEngine.parseScriptClass(classScript);
        assertNotNull(clazz);
        assertEquals("Person", clazz.getSimpleName());
    }
}
