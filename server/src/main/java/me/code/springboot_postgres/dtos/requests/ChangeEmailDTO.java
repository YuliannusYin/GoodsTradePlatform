/**
 * @file ChangeEmailDTO.java
 * @description 修改邮箱请求数据传输对象
 * @input 新邮箱地址
 * @output 包含新邮箱的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 修改邮箱请求DTO
 * 职责：封装用户修改邮箱时提交的新邮箱地址
 */
public record ChangeEmailDTO(
        @NotBlank @Email String newEmail) {
}
