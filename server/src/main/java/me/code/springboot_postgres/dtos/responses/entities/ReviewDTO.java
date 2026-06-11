package me.code.springboot_postgres.dtos.responses.entities;

public record ReviewDTO(
        String id,
        int rating,
        String comment,
        String createdAt,
        String username,
        String productId) {
}
