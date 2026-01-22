package com.iflow.api.core.groovy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Groovy 安全沙箱测试
 */
class GroovySandboxTest {

    private GroovySandbox sandbox;

    @BeforeEach
    void setUp() {
        sandbox = new GroovySandbox();
        sandbox.init();
    }

    @Test
    void testSafeScript() {
        String safeScript = "def list = [1, 2, 3]; return list.sum()";
        assertTrue(sandbox.isSafe(safeScript));
    }

    @Test
    void testSafeScriptWithString() {
        String safeScript = "String name = 'test'; return name.toUpperCase()";
        assertTrue(sandbox.isSafe(safeScript));
    }

    @Test
    void testUnsafeRuntimeExec() {
        String unsafeScript = "def process = Runtime.getRuntime().exec('ls')";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeProcessBuilder() {
        String unsafeScript = "def builder = new ProcessBuilder('echo', 'hello')";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeFileDelete() {
        String unsafeScript = "new File('/tmp/test').delete()";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeFileOutputStream() {
        String unsafeScript = "new FileOutputStream('/tmp/test')";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeClassForName() {
        String unsafeScript = "Class.forName('java.lang.Runtime')";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeGetClassLoader() {
        String unsafeScript = "this.class.classLoader.loadClass('SomeClass')";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeNewSocket() {
        String unsafeScript = "new Socket('localhost', 8080)";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeNewThread() {
        String unsafeScript = "new Thread({ println 'test' }).start()";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testUnsafeSystemGetProperty() {
        String unsafeScript = "System.getProperty('user.dir')";
        assertFalse(sandbox.isSafe(unsafeScript));
    }

    @Test
    void testCompileSafeScript() {
        String safeScript = "return [name: 'test', value: 123]";
        assertDoesNotThrow(() -> sandbox.compileWithSandbox(safeScript));
    }

    @Test
    void testCompileUnsafeScript() {
        String unsafeScript = "Runtime.getRuntime().exec('rm -rf /')";
        assertThrows(SecurityException.class, () -> sandbox.compileWithSandbox(unsafeScript));
    }

    @Test
    void testValidateBindings() {
        java.util.Map<String, Object> safeBindings = new java.util.HashMap<>();
        safeBindings.put("name", "test");
        safeBindings.put("value", 123);
        
        assertTrue(sandbox.validateBindings(safeBindings));
    }

    @Test
    void testForbiddenPatternCount() {
        assertTrue(sandbox.getForbiddenPatternCount() > 0);
    }

    @Test
    void testForbiddenClassCount() {
        assertTrue(sandbox.getForbiddenClassCount() > 0);
    }

    @Test
    void testReset() {
        int before = sandbox.getForbiddenPatternCount();
        sandbox.reset();
        int after = sandbox.getForbiddenPatternCount();
        assertEquals(before, after);
    }

    @Test
    void testAddForbiddenPattern() {
        int before = sandbox.getForbiddenPatternCount();
        sandbox.addForbiddenPattern(java.util.regex.Pattern.compile("dangerous"));
        assertTrue(sandbox.getForbiddenPatternCount() > before);
    }

    @Test
    void testAddForbiddenClass() {
        int before = sandbox.getForbiddenClassCount();
        sandbox.addForbiddenClass("com.example.DangerousClass");
        assertTrue(sandbox.getForbiddenClassCount() > before);
    }

    @Test
    void testIsImportAllowed() {
        assertTrue(sandbox.isImportAllowed("java.util.List"));
        assertTrue(sandbox.isImportAllowed("java.util.stream.Collectors"));
        assertFalse(sandbox.isImportAllowed("java.lang.Runtime"));
    }

    @Test
    void testAddAllowedImport() {
        sandbox.addAllowedImport("com.example");
        assertTrue(sandbox.isImportAllowed("com.example.SafeClass"));
    }
}
