/**
 * @file Favorite.java
 * @description 收藏实体类，表示用户对商品的收藏关系
 * @input 用户实体和商品实体
 * @output 持久化的收藏记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * 收藏实体
 * 职责：映射用户与商品之间的收藏关系，同一用户对同一商品只能收藏一次
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Favorite {

    // 收藏记录唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 收藏创建时间
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 收藏该商品的用户（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 被收藏的商品（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 构造收藏对象，自动设置创建时间为当前时间
     * @param user 收藏用户
     * @param product 被收藏的商品
     */
    public Favorite(User user, Product product) {
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
