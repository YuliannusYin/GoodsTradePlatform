package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

public record ProductDTO(
        @NotBlank String name,
        String description,
        List<String> imageUrls,
        @NotNull @Positive BigDecimal price,
        @NotNull @Min(0) int quantity,
        @NotBlank String category,
        String condition,
        String source) {
}
