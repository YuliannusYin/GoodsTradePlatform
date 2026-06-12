package me.code.springboot_postgres.dtos.responses.entities;

import java.math.BigDecimal;

public record FavoriteDTO(
        String id,
        String productId,
        String productName,
        String productImageUrl,
        BigDecimal productPrice,
        String createdAt) {
}
