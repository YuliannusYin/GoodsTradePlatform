package me.code.springboot_postgres.dtos.responses.success.variants;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.code.springboot_postgres.dtos.responses.success.Success;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class UserDetailsSuccess extends Success {

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("isProtected")
    private boolean isProtected;

    @JsonProperty("role")
    private String role;

    public UserDetailsSuccess(HttpStatus status, String message, String email, String username,
                               BigDecimal balance, boolean isProtected, String role) {
        super(status, message);
        this.email = email;
        this.username = username;
        this.balance = balance;
        this.isProtected = isProtected;
        this.role = role;
    }
}
