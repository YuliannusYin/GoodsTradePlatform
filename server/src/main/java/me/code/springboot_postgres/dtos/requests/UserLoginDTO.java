/**
 * @file UserLoginDTO.java
 * @description 用户登录请求数据传输对象
 * @input 邮箱和密码
 * @output 包含登录凭据的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

/**
 * 用户登录请求DTO
 * 职责：封装用户登录时提交的邮箱和密码
 */
public record UserLoginDTO(String email, String password) {
}
