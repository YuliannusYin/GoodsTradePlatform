package me.code.springboot_postgres.dtos.responses.success.variants;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.code.springboot_postgres.dtos.responses.success.Success;
import org.springframework.http.HttpStatus;

public class UserDetailsSuccess extends Success {

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("balance")
    private double balance;

    @JsonProperty("isProtected")
    private boolean isProtected;

    public UserDetailsSuccess(HttpStatus status, String message, String email, String username,
                               double balance, boolean isProtected) {
        super(status, message);
        this.email = email;
        this.username = username;
        this.balance = balance;
        this.isProtected = isProtected;
    }
}
