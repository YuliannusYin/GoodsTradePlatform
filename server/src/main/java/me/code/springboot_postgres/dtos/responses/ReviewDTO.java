package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Review;
import java.time.LocalDateTime;

public record ReviewDTO(
    String id,
    int rating,
    String comment,
    LocalDateTime createdAt,
    String username,
    String productId
) {
    public static ReviewDTO from(Review review) {
        return new ReviewDTO(
            review.getId(),
            review.getRating(),
            review.getComment(),
            review.getCreatedAt(),
            review.getUser() != null ? review.getUser().getUsername() : null,
            review.getProduct() != null ? review.getProduct().getId() : null
        );
    }
}
