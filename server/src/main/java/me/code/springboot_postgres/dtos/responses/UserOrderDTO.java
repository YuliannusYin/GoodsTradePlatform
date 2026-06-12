package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Order;
import java.time.LocalDateTime;
import java.util.List;

public record UserOrderDTO(
    String id,
    String userEmail,
    String username,
    java.math.BigDecimal price,
    String status,
    String paymentMethod,
    String deliveryMethod,
    String address,
    LocalDateTime received,
    LocalDateTime expectedDelivery,
    List<OrderDTO.OrderItemDTO> items
) {
    public static UserOrderDTO from(Order order) {
        return new UserOrderDTO(
            order.getId(),
            order.getUser() != null ? order.getUser().getEmail() : null,
            order.getUser() != null ? order.getUser().getUsername() : null,
            order.getPrice(),
            order.getStatus() != null ? order.getStatus().name() : null,
            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
            order.getDeliveryMethod() != null ? order.getDeliveryMethod().name() : null,
            order.getAddress(),
            order.getReceived(),
            order.getExpectedDelivery(),
            order.getItems() != null ? order.getItems().stream().map(OrderDTO.OrderItemDTO::from).toList() : List.of()
        );
    }
}
