package me.code.springboot_postgres.dtos.requests;

import java.util.Set;

public record AssignRolesDTO(
        Set<String> roleIds) {
}
