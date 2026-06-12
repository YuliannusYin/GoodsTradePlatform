/**
 * @file PlaceOrderDTO.java
 * @description 下单请求数据传输对象
 * @input 商品ID数组、收货地址、配送方式、支付方式
 * @output 包含下单信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import me.code.springboot_postgres.models.entities.Order;

/**
 * 下单请求DTO
 * 职责：封装用户下单时提交的商品列表、地址、配送方式和支付方式
 */
public record PlaceOrderDTO(
        String[] productIds,
        String address,
        Order.DeliveryMethod deliveryMethod,
        Order.PaymentMethod paymentMethod) {
}
