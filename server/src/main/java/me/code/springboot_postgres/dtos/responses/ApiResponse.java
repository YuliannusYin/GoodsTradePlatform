/**
 * @file ApiResponse.java
 * @description 统一API响应包装类，封装所有接口的返回数据格式
 * @input HTTP状态码、消息、数据
 * @output 标准化的JSON响应体
 */
package me.code.springboot_postgres.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * 统一API响应包装类
 * 职责：提供标准化的API响应格式，包含时间戳、成功标志、状态码、消息和数据
 * @param <T> 响应数据的泛型类型
 */
@Getter
public class ApiResponse<T> {
    // 响应时间戳
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // 请求是否成功
    @JsonProperty("success")
    private boolean success;

    // HTTP状态码
    @JsonProperty("status")
    private int status;

    // 响应消息
    @JsonProperty("message")
    private String message;

    // 响应数据
    @JsonProperty("data")
    private T data;

    /**
     * 构造API响应对象
     * @param status HTTP状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    public ApiResponse(HttpStatus status, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.success = status.is2xxSuccessful();
        this.status = status.value();
        this.message = message;
        this.data = data;
    }

    /**
     * 将当前响应对象转换为ResponseEntity
     * @return Spring ResponseEntity对象
     */
    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return ResponseEntity.status(this.status).body(this);
    }

    /**
     * 创建HTTP 200成功的响应
     * @param message 响应消息
     * @param data 响应数据
     * @return API响应对象
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, message, data);
    }

    /**
     * 创建HTTP 200成功的响应（无数据）
     * @param message 响应消息
     * @return API响应对象
     */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(HttpStatus.OK, message, null);
    }

    /**
     * 创建HTTP 201创建成功的响应
     * @param message 响应消息
     * @param data 响应数据
     * @return API响应对象
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED, message, data);
    }
}
