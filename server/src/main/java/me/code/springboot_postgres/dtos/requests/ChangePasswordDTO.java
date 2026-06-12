package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6, max = 17) String newPassword) {
}
