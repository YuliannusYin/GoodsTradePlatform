package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailDTO(
        @NotBlank @Email String newEmail) {
}
