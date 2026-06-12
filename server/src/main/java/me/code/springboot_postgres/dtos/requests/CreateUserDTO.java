package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
        @NotBlank @Size(min = 3, max = 14) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 17) String password) {
}
