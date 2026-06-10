package me.code.springboot_neo4j.dtos.requests;

public record CreateReviewDTO(
        int rating,
        String comment,
        String productId) {
}
