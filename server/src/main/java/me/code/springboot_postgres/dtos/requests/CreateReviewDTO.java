package me.code.springboot_postgres.dtos.requests;

public record CreateReviewDTO(
        int rating,
        String comment,
        String productId) {
}
