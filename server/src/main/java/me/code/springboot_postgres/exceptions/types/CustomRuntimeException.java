/**
 * @file CustomRuntimeException.java
 * @description 自定义运行时异常类，封装HTTP状态码和详细错误信息
 * @input HTTP状态码、错误消息、错误详情Map
 * @output 包含状态码和详情的异常对象
 */
package me.code.springboot_postgres.exceptions.types;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 自定义运行时异常
 * 职责：封装业务逻辑中的异常信息，携带HTTP状态码和可选的详细错误数据
 */
@Getter
public class CustomRuntimeException extends RuntimeException {

    // HTTP状态码
    private final HttpStatus status;
    // 错误详情（可选）
    private final Map<String, Object> details;

    /**
     * 构造自定义异常（无详情）
     * @param status HTTP状态码
     * @param message 错误消息
     */
    public CustomRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.details = null;
    }

    /**
     * 构造自定义异常（含详情）
     * @param status HTTP状态码
     * @param message 错误消息
     * @param details 错误详情Map
     */
    public CustomRuntimeException(HttpStatus status, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}
