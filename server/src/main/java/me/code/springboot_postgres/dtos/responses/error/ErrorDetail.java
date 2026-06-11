package me.code.springboot_postgres.dtos.responses.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorDetail {

    @JsonProperty("message")
    private String message;

    public ErrorDetail(String message) {
        this.message = message;
    }
}
