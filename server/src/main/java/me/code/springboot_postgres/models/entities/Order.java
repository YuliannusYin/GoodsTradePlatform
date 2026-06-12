package me.code.springboot_postgres.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryMethod deliveryMethod;

    private LocalDateTime received;

    @Column(name = "expected_delivery")
    private LocalDateTime expectedDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"sellingProducts"})
    User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"order"})
    private List<OrderItem> items = new java.util.ArrayList<>();

    public Order(User user, List<OrderItem> items, String address, DeliveryMethod deliveryMethod, PaymentMethod paymentMethod) {
        this.status = Status.PENDING;
        this.user = user;
        this.items = items;
        this.price = getTotalPrice();
        this.address = address;
        this.deliveryMethod = deliveryMethod;
        this.paymentMethod = paymentMethod;
        this.received = LocalDateTime.now();
        this.expectedDelivery = null;
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public enum Status {
        PENDING, SHIPPED, DELIVERED
    }

    public enum DeliveryMethod {
        STANDARD_DELIVERY, EXPRESS_DELIVERY
    }

    public enum PaymentMethod {
        ALIPAY, WECHAT_PAY, CASH_ON_DELIVERY
    }
}
