/**
 * @file OrderDeliveryDTO.java
 * @description 订单发货请求数据传输对象
 * @input 订单ID和预计送达时间
 * @output 包含订单ID和预计送达时间的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * 订单发货请求DTO
 * 职责：封装管理员发货或修改预计送达时间时提交的订单ID和时间
 */
public record OrderDeliveryDTO(
        @NotBlank String orderId,
        @NotBlank String expectedDelivery) {
}
