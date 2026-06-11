package me.code.springboot_postgres.dtos.requests;

public record ChangeExpectedDeliveryDTO(String orderId, String newExpectedDelivery) {
}
