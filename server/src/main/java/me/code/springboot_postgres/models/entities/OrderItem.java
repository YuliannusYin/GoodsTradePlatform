/**
 * @file OrderItem.java
 * @description 订单项实体类，表示订单中的单个商品购买记录
 * @input 商品实体和购买数量
 * @output 持久化的订单项记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

/**
 * 订单项实体
 * 职责：映射订单中的商品购买明细，包含商品、数量和小计价格
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    // 订单项唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 购买数量
    @Column(nullable = false)
    private int amount;

    // 小计价格（单价 × 数量）
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 关联的商品（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 关联的订单（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 构造订单项对象，自动计算小计价格
     * @param product 商品实体
     * @param amount 购买数量
     */
    public OrderItem(Product product, int amount) {
        this.product = product;
        this.amount = amount;
        // 小计价格 = 商品单价 × 数量
        this.price = product.getPrice().multiply(BigDecimal.valueOf(amount));
    }
}
