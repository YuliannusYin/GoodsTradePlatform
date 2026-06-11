package me.code.springboot_postgres.exceptions.types.variants;

import lombok.Getter;
import me.code.springboot_postgres.dtos.responses.error.details.OrderErrorDetail;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import org.springframework.http.HttpStatus;

@Getter
public class OrderException extends CustomRuntimeException {

    private final OrderErrorDetail orderErrorDetail;

    public OrderException(HttpStatus status, String message, OrderErrorDetail orderErrorDetail) {
        super(status, message);
        this.orderErrorDetail = orderErrorDetail;
    }
}
