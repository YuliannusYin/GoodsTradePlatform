/**
 * @file AdjustBalanceDTO.java
 * @description 管理员调整用户余额请求DTO
 * @input 用户ID和调整金额
 * @output 包含调整信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 管理员调整余额请求DTO
 * 职责：封装管理员调整用户余额时的用户ID和调整金额
 */
public record AdjustBalanceDTO(
    /** 用户ID */
    @NotBlank String userId,
    /** 调整金额（正数为充值，负数为扣减） */
    @NotNull BigDecimal amount) {}
