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

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomRuntimeException(CustomRuntimeException exception) {
        return new ApiResponse<Void>(exception.getStatus(), exception.getMessage(), null).toResponseEntity();
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException exception) {
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.CONFLICT, "Data was modified by another request. Please retry.", null);
        return response.toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return new ApiResponse<Void>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null).toResponseEntity();
    }
}
