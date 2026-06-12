package me.code.springboot_postgres.dtos.requests;

public record ProductReviewDTO(
        String status,
        String rejectReason) {
}
