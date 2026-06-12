package me.code.springboot_postgres.exceptions;

import me.code.springboot_postgres.dtos.responses.error.Error;
import me.code.springboot_postgres.dtos.responses.error.ErrorDetail;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity<Error> buildResponseEntity(HttpStatus status, Throwable exception) {
        return new Error(status, exception).toResponseEntity();
    }

    private ResponseEntity<Error> buildResponseEntity(HttpStatus status, Throwable exception, ErrorDetail errorDetail) {
        Error error = new Error(status, exception);
        error.addErrorDetail(errorDetail);
        return error.toResponseEntity();
    }

    @ExceptionHandler({CustomRuntimeException.class})
    public ResponseEntity<Error> handleCustomRuntimeException(CustomRuntimeException exception) {
        HttpStatus status = exception.getStatus();
        ErrorDetail errorDetail = new ErrorDetail(exception.getMessage());
        return buildResponseEntity(status, exception, errorDetail);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Error> handleException(Exception exception) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }
}
