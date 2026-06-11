package me.code.springboot_postgres.dtos.requests;

public record CreateUserDTO(String username, String email, String password) {
}
