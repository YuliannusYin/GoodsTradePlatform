package me.code.springboot_postgres.dtos.responses;

import java.math.BigDecimal;

public record UserDetailsDTO(
    String email,
    String username,
    BigDecimal balance,
    boolean isProtected,
    String role
) {}
