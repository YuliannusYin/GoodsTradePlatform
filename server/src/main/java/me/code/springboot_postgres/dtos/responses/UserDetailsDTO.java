/**
 * @file UserDetailsDTO.java
 * @description 用户详情响应数据传输对象，返回当前登录用户的账户详情
 * @input 无（由服务层构造）
 * @output 邮箱、用户名、余额、保护状态和角色
 */
package me.code.springboot_postgres.dtos.responses;

import java.math.BigDecimal;

/**
 * 用户详情响应DTO
 * 职责：封装返回给前端的当前用户账户详情
 */
public record UserDetailsDTO(
    String email,
    String username,
    BigDecimal balance,
    boolean isProtected,
    String role
) {}
