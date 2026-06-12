/**
 * @file ChangePasswordDTO.java
 * @description 修改密码请求数据传输对象
 * @input 当前密码和新密码
 * @output 包含当前密码和新密码的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求DTO
 * 职责：封装用户修改密码时提交的当前密码和新密码
 */
public record ChangePasswordDTO(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6, max = 17) String newPassword) {
}
