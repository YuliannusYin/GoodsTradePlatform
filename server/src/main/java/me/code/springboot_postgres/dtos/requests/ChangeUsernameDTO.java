package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUsernameDTO(
        @NotBlank @Size(min = 3, max = 14) String newUsername) {
}
