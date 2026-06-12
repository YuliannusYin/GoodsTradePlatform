/**
 * @file OrderDTO.java
 * @description 订单响应数据传输对象，返回订单的完整信息
 * @input 无（由实体转换构造）
 * @output 订单ID、价格、状态、支付方式、配送方式、地址、时间和订单项列表
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应DTO
 * 职责：封装返回给前端的订单完整信息，包含嵌套的订单项和商品信息
 */
public record OrderDTO(
    String id,
    BigDecimal price,
    String status,
    String paymentMethod,
    String deliveryMethod,
    String address,
    LocalDateTime received,
    LocalDateTime expectedDelivery,
    List<OrderItemDTO> items
) {
    /**
     * 从订单实体转换为订单DTO
     * @param order 订单实体
     * @return 订单DTO对象
     */
    public static OrderDTO from(Order order) {
        return new OrderDTO(
            order.getId(),
            order.getPrice(),
            // 枚举转字符串，空值安全处理
            order.getStatus() != null ? order.getStatus().name() : null,
            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
            order.getDeliveryMethod() != null ? order.getDeliveryMethod().name() : null,
            order.getAddress(),
            order.getReceived(),
            order.getExpectedDelivery(),
            // 订单项列表转换，空值安全处理
            order.getItems() != null ? order.getItems().stream().map(OrderItemDTO::from).toList() : List.of()
        );
    }

    /**
     * 订单项响应DTO
     * 职责：封装订单中的单个商品项信息
     */
    public record OrderItemDTO(
        String id,
        int amount,
        BigDecimal price,
        ProductItemDTO product
    ) {
        /**
         * 从订单项实体转换为订单项DTO
         * @param item 订单项实体
         * @return 订单项DTO对象
         */
        public static OrderItemDTO from(OrderItem item) {
            return new OrderItemDTO(
                item.getId(),
                item.getAmount(),
                item.getPrice(),
                item.getProduct() != null ? ProductItemDTO.from(item.getProduct()) : null
            );
        }
    }

    /**
     * 订单项中的商品摘要DTO
     * 职责：封装订单项中的商品基本信息
     */
    public record ProductItemDTO(
        String id,
        String name,
        BigDecimal price,
        List<String> imageUrls
    ) {
        /**
         * 从商品实体转换为商品摘要DTO
         * @param product 商品实体
         * @return 商品摘要DTO对象
         */
        public static ProductItemDTO from(me.code.springboot_postgres.models.entities.Product product) {
            return new ProductItemDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrls()
            );
        }
    }
}
