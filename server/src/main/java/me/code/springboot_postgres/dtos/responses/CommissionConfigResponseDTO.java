/**
 * @file CommissionConfigResponseDTO.java
 * @description 佣金配置响应数据传输对象
 * @input 无
 * @output 佣金配置完整信息（含计算示例）
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.CommissionConfig;

import java.math.BigDecimal;

/**
 * 佣金配置响应DTO
 * 职责：封装返回给前端的佣金配置信息
 */
public record CommissionConfigResponseDTO(
    /** 佣金类型 */
    String commissionType,
    /** 佣金率 */
    BigDecimal commissionRate,
    /** 固定佣金金额 */
    BigDecimal fixedAmount
) {
    /**
     * 从佣金配置实体转换为响应DTO
     * @param config 佣金配置实体
     * @return 佣金配置响应DTO
     */
    public static CommissionConfigResponseDTO from(CommissionConfig config) {
        return new CommissionConfigResponseDTO(
            config.getCommissionType() != null ? config.getCommissionType().name() : null,
            config.getCommissionRate(),
            config.getFixedAmount()
        );
    }
}
