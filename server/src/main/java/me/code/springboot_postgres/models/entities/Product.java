/**
 * @file Product.java
 * @description 商品实体类，表示平台上的商品信息
 * @input 商品名称、描述、图片、价格、数量、分类、成色、来源
 * @output 持久化的商品记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品实体
 * 职责：映射商品数据，包含商品基本信息、状态、审核信息和乐观锁版本控制
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products",
        indexes = {
            @Index(name = "idx_products_seller_id", columnList = "seller_id"),
            @Index(name = "idx_products_status", columnList = "status")
        })
public class Product {

    // 商品唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 商品名称
    @Column(nullable = false)
    private String name;

    // 商品描述
    @Column(columnDefinition = "TEXT")
    private String description;

    // 商品图片URL列表（JSON格式存储）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> imageUrls;

    // 商品价格
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 库存数量
    @Column(nullable = false)
    private int quantity;

    // 商品分类
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    // 商品成色
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Condition condition;

    // 商品来源（PLATFORM/USER）
    @Column(length = 20)
    private String source;

    // 商品审核状态，默认为已审核
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.APPROVED;

    // 驳回原因
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    // 商品卖家（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    // 乐观锁版本号，防止并发修改冲突
    @Version
    private int version;

    /**
     * 构造商品对象
     * @param name 商品名称
     * @param description 商品描述
     * @param imageUrls 图片URL列表
     * @param price 价格
     * @param quantity 库存数量
     * @param category 分类
     * @param condition 成色
     * @param source 来源
     */
    public Product(String name, String description, List<String> imageUrls, BigDecimal price, int quantity, Category category, Condition condition, String source) {
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.condition = condition;
        this.source = source;
    }

    /**
     * 商品成色枚举
     */
    public enum Condition {
        NEW, LIKE_NEW, GOOD, FAIR
    }

    /**
     * 商品分类枚举
     */
    public enum Category {
        ANIME_FIGURE, POSTER, KEYCHAIN, BADGE, PILLOW, STAND, CLOTHING, ALBUM, ACCESSORY, OTHER
    }

    /**
     * 商品审核状态枚举
     */
    public enum Status {
        PENDING, APPROVED, REJECTED, DISABLED
    }
}
