package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record OrderDeliveryDTO(
        @NotBlank String orderId,
        @NotBlank String expectedDelivery) {
}
