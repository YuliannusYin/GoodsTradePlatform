/**
 * @file GlobalExceptionHandler.java
 * @description 全局异常处理器，捕获并统一处理所有控制器抛出的异常
 * @input 各类异常对象
 * @output 标准化的API错误响应
 */
package me.code.springboot_postgres.exceptions;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理器
 * 职责：拦截控制器抛出的异常，转换为统一的API响应格式返回给前端
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     * @param exception 自定义运行时异常
     * @return 包含异常状态码和消息的API响应
     */
    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomRuntimeException(CustomRuntimeException exception) {
        return new ApiResponse<Void>(exception.getStatus(), exception.getMessage(), null).toResponseEntity();
    }

    /**
     * 处理乐观锁冲突异常（并发修改数据时触发）
     * @param exception 乐观锁失败异常
     * @return HTTP 409冲突响应
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException exception) {
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.CONFLICT, "Data was modified by another request. Please retry.", null);
        return response.toResponseEntity();
    }

    /**
     * 处理所有未捕获的异常（兜底处理）
     * @param exception 未预期的异常
     * @return HTTP 500内部服务器错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return new ApiResponse<Void>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null).toResponseEntity();
    }
}
