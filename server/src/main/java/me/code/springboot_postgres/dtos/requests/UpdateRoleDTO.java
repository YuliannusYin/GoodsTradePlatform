package me.code.springboot_postgres.dtos.requests;

import java.util.Set;

public record UpdateRoleDTO(
        String name,
        String description,
        Set<String> permissionIds) {
}
