package me.code.springboot_postgres.dtos.requests;

import me.code.springboot_postgres.models.entities.Order;

public record PlaceOrderDTO(
        String[] productIds,
        String address,
        Order.DeliveryMethod deliveryMethod,
        Order.PaymentMethod paymentMethod) {
}
