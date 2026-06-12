package me.code.springboot_postgres.dtos.responses.success.variants;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.code.springboot_postgres.dtos.responses.success.Success;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

public class UserDetailsSuccess extends Success {

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("isProtected")
    private boolean isProtected;

    @JsonProperty("roles")
    private List<String> roles;

    public UserDetailsSuccess(HttpStatus status, String message, String email, String username,
                               BigDecimal balance, boolean isProtected, List<String> roles) {
        super(status, message);
        this.email = email;
        this.username = username;
        this.balance = balance;
        this.isProtected = isProtected;
        this.roles = roles;
    }
}
