/**
 * @file GlobalExceptionHandler.java
 * @description 全局异常处理器，捕获并统一处理所有控制器抛出的异常
 * @input 各类异常对象
 * @output 标准化的API错误响应
 */
package me.code.springboot_postgres.exceptions;

import jakarta.persistence.EntityNotFoundException;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;

/**
 * 全局异常处理器
 * 职责：拦截控制器抛出的异常，转换为统一的API响应格式返回给前端
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
     * 处理请求体验证失败异常（@Valid触发）
     * @param exception 验证异常
     * @return HTTP 400错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        // 提取第一个验证错误消息
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("请求参数验证失败");
        return new ApiResponse<Void>(HttpStatus.BAD_REQUEST, message, null).toResponseEntity();
    }

    /**
     * 处理请求体JSON解析失败异常（格式错误、类型不匹配）
     * @param exception 消息不可读异常
     * @return HTTP 400错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return new ApiResponse<Void>(HttpStatus.BAD_REQUEST, "请求体格式错误或数据类型不匹配", null).toResponseEntity();
    }

    /**
     * 处理枚举值无效异常（Enum.valueOf传入无效值）
     * @param exception 非法参数异常
     * @return HTTP 400错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ApiResponse<Void>(HttpStatus.BAD_REQUEST, "无效的参数值: " + exception.getMessage(), null).toResponseEntity();
    }

    /**
     * 处理日期时间解析失败异常
     * @param exception 日期解析异常
     * @return HTTP 400错误响应
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse<Void>> handleDateTimeParseException(DateTimeParseException exception) {
        return new ApiResponse<Void>(HttpStatus.BAD_REQUEST, "日期时间格式错误，请使用 yyyy-MM-dd'T'HH:mm:ss 格式", null).toResponseEntity();
    }

    /**
     * 处理数据库约束违反异常（唯一约束、外键约束等）
     * @param exception 数据完整性异常
     * @return HTTP 409冲突响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return new ApiResponse<Void>(HttpStatus.CONFLICT, "数据操作违反约束，可能存在重复数据或关联数据不存在", null).toResponseEntity();
    }

    /**
     * 处理乐观锁冲突异常（并发修改数据时触发）
     * @param exception 乐观锁失败异常
     * @return HTTP 409冲突响应
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException exception) {
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.CONFLICT, "数据已被其他请求修改，请重试", null);
        return response.toResponseEntity();
    }

    /**
     * 处理JPA实体未找到异常（懒加载引用的实体已被删除时触发）
     * @param exception 实体未找到异常
     * @return HTTP 404未找到响应
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException exception) {
        return new ApiResponse<Void>(HttpStatus.NOT_FOUND, "请求的资源不存在或已被删除", null).toResponseEntity();
    }

    /**
     * 处理所有未捕获的异常（兜底处理）
     * @param exception 未预期的异常
     * @return HTTP 500内部服务器错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        // 记录异常堆栈信息，便于生产环境排查
        log.error("未预期的异常", exception);
        return new ApiResponse<Void>(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误", null).toResponseEntity();
    }
}
