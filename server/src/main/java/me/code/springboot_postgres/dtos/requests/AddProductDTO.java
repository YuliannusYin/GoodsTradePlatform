package me.code.springboot_postgres.dtos.requests;

import java.util.List;

public record AddProductDTO(
        String name,
        String description,
        List<String> imageUrls,
        double price,
        int quantity,
        String category,
        String condition,
        String source) {
}
