package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
    String id,
    BigDecimal price,
    String status,
    String paymentMethod,
    String deliveryMethod,
    String address,
    LocalDateTime received,
    LocalDateTime expectedDelivery,
    List<OrderItemDTO> items
) {
    public static OrderDTO from(Order order) {
        return new OrderDTO(
            order.getId(),
            order.getPrice(),
            order.getStatus() != null ? order.getStatus().name() : null,
            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
            order.getDeliveryMethod() != null ? order.getDeliveryMethod().name() : null,
            order.getAddress(),
            order.getReceived(),
            order.getExpectedDelivery(),
            order.getItems() != null ? order.getItems().stream().map(OrderItemDTO::from).toList() : List.of()
        );
    }

    public record OrderItemDTO(
        String id,
        int amount,
        BigDecimal price,
        ProductItemDTO product
    ) {
        public static OrderItemDTO from(OrderItem item) {
            return new OrderItemDTO(
                item.getId(),
                item.getAmount(),
                item.getPrice(),
                item.getProduct() != null ? ProductItemDTO.from(item.getProduct()) : null
            );
        }
    }

    public record ProductItemDTO(
        String id,
        String name,
        BigDecimal price,
        List<String> imageUrls
    ) {
        public static ProductItemDTO from(me.code.springboot_postgres.models.entities.Product product) {
            return new ProductItemDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrls()
            );
        }
    }
}
