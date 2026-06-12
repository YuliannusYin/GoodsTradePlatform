package me.code.springboot_postgres.dtos.responses;

import java.util.List;

public record AuthenticationDTO(
    List<String> userRoles,
    String token
) {}
