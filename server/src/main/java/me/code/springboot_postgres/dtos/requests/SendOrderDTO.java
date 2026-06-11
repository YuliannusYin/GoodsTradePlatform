package me.code.springboot_postgres.dtos.requests;

public record SendOrderDTO(String orderId, String expectedDelivery) {
}
