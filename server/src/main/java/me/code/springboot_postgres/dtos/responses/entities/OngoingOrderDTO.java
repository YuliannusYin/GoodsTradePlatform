package me.code.springboot_postgres.dtos.responses.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import me.code.springboot_postgres.models.entities.OrderItem;

import java.math.BigDecimal;
import java.util.List;

@Setter
public class OngoingOrderDTO {

    @JsonProperty("items")
    private List<OrderItem> items;

    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;

    public OngoingOrderDTO(List<OrderItem> items, BigDecimal totalPrice) {
        this.items = items;
        this.totalPrice = totalPrice;
    }
}
