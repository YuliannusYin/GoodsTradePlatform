package me.code.springboot_postgres.dtos.responses.entities;

public record FavoriteDTO(
        String id,
        String productId,
        String productName,
        String productImageUrl,
        double productPrice,
        String createdAt) {
}
