package me.code.springboot_postgres.dtos.responses.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import me.code.springboot_postgres.models.entities.Order;

@Setter
public class UserOrderDTO extends PlacedOrderDTO {

    @JsonProperty("userEmail")
    private String userEmail;

    public UserOrderDTO(Order order, String userEmail) {
        super(order);
        this.userEmail = userEmail;
    }

}
