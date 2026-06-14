/**
 * @file Order.java
 * @description 订单实体类，表示用户的购买订单
 * @input 用户、订单项列表、收货人信息、配送方式
 * @output 持久化的订单记录
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体
 * 职责：映射用户订单数据，包含订单状态、价格、配送信息和订单项列表
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders",
        indexes = {
            @Index(name = "idx_orders_status", columnList = "status")
        })
public class Order {

    // 订单唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 订单状态
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status;

    // 订单总价格
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 支付方式
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentMethod paymentMethod;

    // 收货人姓名
    @Column(nullable = false, length = 50)
    private String receiverName;

    // 联系电话
    @Column(nullable = false, length = 20)
    private String receiverPhone;

    // 省/市/区
    @Column(nullable = false, length = 100)
    private String region;

    // 详细地址
    @Column(nullable = false, columnDefinition = "TEXT")
    private String detailAddress;

    // 配送方式
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryMethod deliveryMethod;

    // 订单接收时间
    private LocalDateTime received;

    // 预计送达时间
    @Column(name = "expected_delivery")
    private LocalDateTime expectedDelivery;

    // 下单用户（懒加载）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 订单项列表（级联保存、懒加载、孤立删除）
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new java.util.ArrayList<>();

    /**
     * 构造订单对象，自动计算总价并设置初始状态
     * 支付方式固定为余额支付
     * @param user 下单用户
     * @param items 订单项列表
     * @param receiverName 收货人姓名
     * @param receiverPhone 联系电话
     * @param region 省/市/区
     * @param detailAddress 详细地址
     * @param deliveryMethod 配送方式
     */
    public Order(User user, List<OrderItem> items, String receiverName, String receiverPhone,
                 String region, String detailAddress, DeliveryMethod deliveryMethod) {
        this.status = Status.PENDING;
        this.user = user;
        this.items = items;
        this.price = getTotalPrice();
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.region = region;
        this.detailAddress = detailAddress;
        this.deliveryMethod = deliveryMethod;
        // 支付方式固定为余额支付
        this.paymentMethod = PaymentMethod.ACCOUNT_BALANCE;
        this.received = LocalDateTime.now();
        this.expectedDelivery = null;
    }

    /**
     * 计算订单总价（所有订单项价格之和，保留两位小数）
     * @return 订单总价
     */
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 订单状态枚举
     */
    public enum Status {
        PENDING, SHIPPED, DELIVERED
    }

    /**
     * 配送方式枚举
     */
    public enum DeliveryMethod {
        STANDARD_DELIVERY, EXPRESS_DELIVERY
    }

    /**
     * 支付方式枚举（仅支持余额支付）
     */
    public enum PaymentMethod {
        ACCOUNT_BALANCE
    }
}
