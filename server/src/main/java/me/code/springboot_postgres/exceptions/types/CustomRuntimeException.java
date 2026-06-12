package me.code.springboot_postgres.exceptions.types;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class CustomRuntimeException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, Object> details;

    public CustomRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.details = null;
    }

    public CustomRuntimeException(HttpStatus status, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}
