package me.code.springboot_postgres.dtos.requests;

public record ChangePasswordDTO(String currentPassword, String newPassword) {
}
