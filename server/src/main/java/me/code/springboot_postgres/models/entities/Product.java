package me.code.springboot_postgres.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> imageUrls;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Condition condition;

    @Column(length = 20)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.APPROVED;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id")
    @JsonIgnoreProperties({"sellingProducts"})
    private User seller;

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

    public enum Condition {
        NEW, LIKE_NEW, GOOD, FAIR
    }

    public enum Category {
        ANIME_FIGURE, POSTER, KEYCHAIN, BADGE, PILLOW, STAND, CLOTHING, ALBUM, ACCESSORY, OTHER
    }

    public enum Status {
        PENDING, APPROVED, REJECTED, DISABLED
    }
}
