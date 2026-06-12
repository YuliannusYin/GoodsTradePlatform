package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReviewDTO(
        @Min(1) @Max(5) int rating,
        String comment,
        @NotBlank String productId) {
}
