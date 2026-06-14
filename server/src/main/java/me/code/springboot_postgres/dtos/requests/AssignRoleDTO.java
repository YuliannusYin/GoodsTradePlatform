/**
 * @file AssignRoleDTO.java
 * @description 管理员分配角色请求数据传输对象
 * @input 角色名称
 * @output 包含角色信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * 分配角色请求DTO
 * 职责：封装管理员为用户分配角色时提交的角色名称
 *
 * @param role 角色名称
 */
public record AssignRoleDTO(
        @NotBlank String role
) {
}
