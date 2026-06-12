package me.code.springboot_postgres.dtos.responses;

public record UnavailableProductDTO(
    String message,
    String productId,
    int requestedAmount,
    int availableAmount
) {}
