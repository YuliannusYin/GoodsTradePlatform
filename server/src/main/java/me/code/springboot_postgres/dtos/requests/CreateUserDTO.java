/**
 * @file CreateUserDTO.java
 * @description 用户注册请求数据传输对象
 * @input 用户名、邮箱、密码
 * @output 包含注册信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求DTO
 * 职责：封装用户注册时提交的用户名、邮箱和密码
 */
public record CreateUserDTO(
        @NotBlank @Size(min = 3, max = 14) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 17) String password) {
}
