package com.iflow.api.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 
 * 引用：REQ-F1-007（自动生成的接口应返回统一的响应格式）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求追踪 ID
     */
    private String traceId;

    /**
     * 成功状态
     */
    private Boolean success;

    // ========== 构造函数 ==========

    public Result() {
        this.timestamp = System.currentTimeMillis();
        this.success = true;
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
        this.success = code != null && code >= 200 && code < 300;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 失败响应（带状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败响应（带枚举状态）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 分页响应
     */
    public static <T> Result<PageData<T>> page(Long total, Integer page, Integer size, T list) {
        PageData<T> pageData = new PageData<>();
        pageData.setTotal(total);
        pageData.setPage(page);
        pageData.setSize(size);
        pageData.setPages((total + size - 1) / size);
        pageData.setList(list);
        return success(pageData);
    }

    // ========== 内部类 ==========

    /**
     * 分页数据
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageData<T> implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long total;
        private Integer page;
        private Integer size;
        private Long pages;
        private T list;
    }

    /**
     * 响应状态码枚举
     */
    public enum ResultCode {
        SUCCESS(200, "操作成功"),
        ERROR(500, "操作失败"),
        BAD_REQUEST(400, "请求参数错误"),
        UNAUTHORIZED(401, "未授权"),
        FORBIDDEN(403, "禁止访问"),
        NOT_FOUND(404, "资源不存在"),
        METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
        CONFLICT(409, "数据冲突"),
        VALIDATION_ERROR(422, "参数验证失败"),
        SERVER_ERROR(500, "服务器内部错误"),
        DATABASE_ERROR(501, "数据库错误"),
        SCRIPT_EXECUTION_ERROR(502, "脚本执行错误");

        private final Integer code;
        private final String message;

        ResultCode(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
