package me.code.springboot_neo4j.dtos.requests;

import me.code.springboot_neo4j.models.nodes.Order;

public record PlaceOrderDTO(
        String[] productIds,
        String address,
        Order.DeliveryMethod deliveryMethod,
        Order.PaymentMethod paymentMethod) {
}
