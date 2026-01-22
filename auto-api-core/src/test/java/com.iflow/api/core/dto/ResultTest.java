package com.iflow.api.core.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一响应 DTO 测试
 */
class ResultTest {

    @Test
    void testSuccessWithoutData() {
        Result<String> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertTrue(result.getSuccess());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testSuccessWithData() {
        Result<String> result = Result.success("test data");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertTrue(result.getSuccess());
        assertEquals("test data", result.getData());
    }

    @Test
    void testSuccessWithList() {
        Result<java.util.List<String>> result = Result.success(java.util.Arrays.asList("a", "b", "c"));

        assertEquals(200, result.getCode());
        assertTrue(result.getSuccess());
        assertEquals(3, result.getData().size());
    }

    @Test
    void testError() {
        Result<String> result = Result.error("error message");

        assertEquals(500, result.getCode());
        assertEquals("error message", result.getMessage());
        assertFalse(result.getSuccess());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithCode() {
        Result<String> result = Result.error(404, "not found");

        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMessage());
        assertFalse(result.getSuccess());
    }

    @Test
    void testTimestamp() {
        long before = System.currentTimeMillis();
        Result<String> result = Result.success();
        long after = System.currentTimeMillis();

        assertTrue(result.getTimestamp() >= before);
        assertTrue(result.getTimestamp() <= after);
    }

    @Test
    void testSuccessMessage() {
        Result<String> result = Result.success("custom message");

        assertEquals("success", result.getMessage());
        assertEquals("custom message", result.getData());
    }

    @Test
    void testNullData() {
        Result<Object> result = Result.success(null);

        assertNull(result.getData());
        assertTrue(result.getSuccess());
    }
}
