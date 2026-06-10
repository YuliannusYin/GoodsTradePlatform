package me.code.springboot_neo4j.dtos.responses.entities;

public record ProductRatingDTO(
        double averageRating,
        int reviewCount) {
}
