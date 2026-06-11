package me.code.springboot_postgres.dtos.responses.entities;

public record ProductRatingDTO(
        double averageRating,
        int reviewCount) {
}
