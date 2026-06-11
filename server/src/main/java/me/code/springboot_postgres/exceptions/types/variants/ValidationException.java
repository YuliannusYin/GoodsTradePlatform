package me.code.springboot_postgres.exceptions.types.variants;

import lombok.Getter;
import me.code.springboot_postgres.dtos.responses.error.details.ValidationErrorDetail;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends CustomRuntimeException {

    private final ValidationErrorDetail validationError;

    public ValidationException(HttpStatus status, String message, ValidationErrorDetail validationError) {
        super(status, message);
        this.validationError = validationError;
    }
}
