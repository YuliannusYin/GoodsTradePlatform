package me.code.springboot_postgres.dtos.requests;

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
