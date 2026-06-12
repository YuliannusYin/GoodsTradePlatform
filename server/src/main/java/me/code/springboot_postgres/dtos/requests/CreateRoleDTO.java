package me.code.springboot_postgres.dtos.requests;

import java.util.Set;

public record CreateRoleDTO(
        String name,
        String description,
        Set<String> permissionIds) {
}
