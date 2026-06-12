/**
 * @file Review.java
 * @description 评价实体类，表示用户对商品的评价
 * @input 评分、评论内容、用户实体和商品实体
 * @output 持久化的评价记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * 评价实体
 * 职责：映射用户对商品的评价数据，同一用户对同一商品只能评价一次
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Review {

    // 评价唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 评分（1-5分）
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private int rating;

    // 评价内容
    @Column(columnDefinition = "TEXT")
    private String comment;

    // 评价创建时间
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 评价用户（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 被评价的商品（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 构造评价对象，自动设置创建时间为当前时间
     * @param rating 评分
     * @param comment 评论内容
     * @param user 评价用户
     * @param product 被评价的商品
     */
    public Review(int rating, String comment, User user, Product product) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
