/**
 * @file CommissionConfigDTO.java
 * @description 佣金配置请求/响应数据传输对象
 * @input 佣金类型、佣金率、固定金额
 * @output 佣金配置信息
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 佣金配置DTO
 * 职责：封装佣金配置的请求和响应数据
 */
public record CommissionConfigDTO(
    /** 佣金类型：PERCENTAGE（百分比）或 FIXED（固定金额） */
    @NotNull(message = "佣金类型不能为空")
    String commissionType,

    /** 佣金率（百分比模式时使用，范围 0~1，如 0.05 表示 5%） */
    @DecimalMin(value = "0", message = "佣金率不能为负数")
    @DecimalMax(value = "1", message = "佣金率不能超过100%")
    BigDecimal commissionRate,

    /** 固定佣金金额（固定金额模式时使用） */
    @DecimalMin(value = "0", message = "固定佣金金额不能为负数")
    BigDecimal fixedAmount
) {}
