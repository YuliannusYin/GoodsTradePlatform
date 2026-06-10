package me.code.springboot_neo4j.dtos.responses.entities;

public record FavoriteDTO(
        String id,
        String productId,
        String productName,
        String productImageUrl,
        double productPrice,
        String createdAt) {
}
