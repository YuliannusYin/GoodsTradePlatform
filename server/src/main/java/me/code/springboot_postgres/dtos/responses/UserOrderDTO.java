/**
 * @file UserOrderDTO.java
 * @description 用户订单响应数据传输对象（管理员视角），返回包含用户信息的订单详情
 * @input 无（由实体转换构造）
 * @output 订单ID、用户邮箱、用户名、价格、状态、支付方式、配送方式、地址、时间和订单项列表
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Order;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户订单响应DTO（管理员视角）
 * 职责：封装管理员查看订单时返回的订单及用户信息
 */
public record UserOrderDTO(
    String id,
    String userEmail,
    String username,
    java.math.BigDecimal price,
    String status,
    String paymentMethod,
    String deliveryMethod,
    String address,
    LocalDateTime received,
    LocalDateTime expectedDelivery,
    List<OrderDTO.OrderItemDTO> items
) {
    /**
     * 从订单实体转换为用户订单DTO
     * @param order 订单实体
     * @return 用户订单DTO对象
     */
    public static UserOrderDTO from(Order order) {
        return new UserOrderDTO(
            order.getId(),
            // 空值安全处理用户信息
            order.getUser() != null ? order.getUser().getEmail() : null,
            order.getUser() != null ? order.getUser().getUsername() : null,
            order.getPrice(),
            order.getStatus() != null ? order.getStatus().name() : null,
            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
            order.getDeliveryMethod() != null ? order.getDeliveryMethod().name() : null,
            order.getAddress(),
            order.getReceived(),
            order.getExpectedDelivery(),
            order.getItems() != null ? order.getItems().stream().map(OrderDTO.OrderItemDTO::from).toList() : List.of()
        );
    }
}
