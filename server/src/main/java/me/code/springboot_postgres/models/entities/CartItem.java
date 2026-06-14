/**
 * @file CartItem.java
 * @description 购物车项实体类，表示用户购物车中的商品条目
 * @input 用户实体、商品实体、商品数量
 * @output 持久化的购物车项记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * 购物车项实体
 * 职责：映射用户购物车中的商品条目，同一用户对同一商品只保留一条记录，通过数量字段表示购买份数
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class CartItem {

    // 购物车项唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 购物车所属用户（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 购物车项对应的商品（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 商品数量
    @Column(nullable = false)
    private int quantity;

    /**
     * 构造购物车项对象
     * @param user 购物车所属用户
     * @param product 购物车项对应的商品
     * @param quantity 商品数量
     */
    public CartItem(User user, Product product, int quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
    }
}
