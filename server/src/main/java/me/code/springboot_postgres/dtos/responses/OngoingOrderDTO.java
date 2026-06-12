package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public record OngoingOrderDTO(
    List<OrderDTO.OrderItemDTO> items,
    BigDecimal totalPrice
) {}
