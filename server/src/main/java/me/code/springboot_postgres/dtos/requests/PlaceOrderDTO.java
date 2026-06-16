/**
 * @file PlaceOrderDTO.java
 * @description 下单请求数据传输对象
 * @input 商品ID数组、收货人信息、配送方式
 * @output 包含下单信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.code.springboot_postgres.models.entities.Order;

/**
 * 下单请求DTO
 * 职责：封装用户下单时提交的商品列表、收货人信息和配送方式
 */
public record PlaceOrderDTO(
        /** 商品ID数组 */
        @NotEmpty String[] productIds,
        /** 收货人姓名 */
        @NotBlank String receiverName,
        /** 联系电话 */
        @NotBlank String receiverPhone,
        /** 省/市/区 */
        @NotBlank String region,
        /** 详细地址 */
        @NotBlank String detailAddress,
        /** 配送方式（枚举类型必须用 @NotNull，@NotBlank 仅适用于 String 类型） */
        @NotNull Order.DeliveryMethod deliveryMethod) {
}
