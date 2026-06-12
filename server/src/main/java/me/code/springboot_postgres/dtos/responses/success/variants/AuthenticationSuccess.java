package me.code.springboot_postgres.dtos.responses.success.variants;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.code.springboot_postgres.dtos.responses.success.Success;
import org.springframework.http.HttpStatus;

import java.util.List;

public class AuthenticationSuccess extends Success {

    @JsonProperty("userRoles")
    private List<String> userRoles;

    @JsonProperty("token")
    private String token;

    public AuthenticationSuccess(HttpStatus status, String message, List<String> userRoles, String token) {
        super(status, message);
        this.userRoles = userRoles;
        this.token = token;
    }
}
