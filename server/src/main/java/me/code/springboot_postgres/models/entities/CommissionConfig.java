/**
 * @file CommissionConfig.java
 * @description 佣金配置实体类，存储平台的佣金规则（仅一条记录，单例模式）
 * @input 佣金类型（百分比/固定值）和佣金率/固定金额
 * @output 持久化的佣金配置记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 佣金配置实体
 * 职责：存储平台佣金规则，支持百分比佣金和固定佣金两种模式
 * 数据库中仅保留一条记录（singleton_id = 1），实现单例配置
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "commission_config")
public class CommissionConfig {

    // 固定主键，确保全局仅一条配置记录
    @Id
    private String id = "1";

    // 佣金类型：PERCENTAGE（百分比）或 FIXED（固定金额）
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionType commissionType = CommissionType.PERCENTAGE;

    // 佣金率（百分比模式时使用，如 0.05 表示 5%）
    @Column(precision = 5, scale = 4)
    private BigDecimal commissionRate = new BigDecimal("0.05");

    // 固定佣金金额（固定金额模式时使用，每笔订单固定收取）
    @Column(precision = 10, scale = 2)
    private BigDecimal fixedAmount = BigDecimal.ZERO;

    /**
     * 佣金类型枚举
     */
    public enum CommissionType {
        // 按订单金额的百分比收取佣金
        PERCENTAGE,
        // 每笔订单收取固定金额的佣金
        FIXED
    }

    /**
     * 根据订单金额计算佣金
     * @param orderAmount 订单金额
     * @return 佣金金额
     */
    public BigDecimal calculateCommission(BigDecimal orderAmount) {
        if (commissionType == CommissionType.PERCENTAGE) {
            // 百分比模式：佣金 = 订单金额 × 佣金率
            return orderAmount.multiply(commissionRate);
        } else {
            // 固定金额模式：佣金 = 固定金额（但不超过订单金额）
            return fixedAmount.min(orderAmount);
        }
    }
}
