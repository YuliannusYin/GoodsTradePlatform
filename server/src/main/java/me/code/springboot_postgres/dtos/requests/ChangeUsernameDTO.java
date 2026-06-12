/**
 * @file ChangeUsernameDTO.java
 * @description 修改用户名请求数据传输对象
 * @input 新用户名
 * @output 包含新用户名的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改用户名请求DTO
 * 职责：封装用户修改用户名时提交的新用户名
 */
public record ChangeUsernameDTO(
        @NotBlank @Size(min = 3, max = 14) String newUsername) {
}
