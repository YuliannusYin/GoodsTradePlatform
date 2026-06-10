package me.code.springboot_neo4j.dtos.requests;

import java.util.List;

public record EditedProductDTO(
        String name,
        String description,
        List<String> imageUrls,
        double price,
        int quantity,
        String category,
        String condition,
        String source) {
}
