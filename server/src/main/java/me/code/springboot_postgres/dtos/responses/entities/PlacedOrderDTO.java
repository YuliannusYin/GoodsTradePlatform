package me.code.springboot_postgres.dtos.responses.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import me.code.springboot_postgres.models.entities.OrderItem;
import me.code.springboot_postgres.models.entities.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
public class PlacedOrderDTO {

    @JsonProperty("id")
    String id;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("status")
    private Order.Status status;

    @JsonProperty("received")
    private LocalDateTime received;

    @JsonProperty("expectedDelivery")
    private LocalDateTime expectedDelivery;

    @JsonProperty("items")
    private List<OrderItem> items;

    public PlacedOrderDTO(Order order) {
        this.id = order.getId();
        this.price = order.getPrice();
        this.status = order.getStatus();
        this.received = order.getReceived();
        this.expectedDelivery = order.getExpectedDelivery();
        this.items = order.getItems();
    }

}
