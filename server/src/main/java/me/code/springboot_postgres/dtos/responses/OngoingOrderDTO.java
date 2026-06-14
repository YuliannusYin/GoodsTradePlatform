/**
 * @file OngoingOrderDTO.java
 * @description 进行中订单响应数据传输对象，返回未提交订单的预览信息
 * @input 无（由服务层构造）
 * @output 订单项列表和总价
 */
package me.code.springboot_postgres.dtos.responses;

import java.math.BigDecimal;
import java.util.List;

/**
 * 进行中订单响应DTO
 * 职责：封装用户下单前的订单预览数据，包含商品项和总价
 */
public record OngoingOrderDTO(
    List<OrderDTO.OrderItemDTO> items,
    BigDecimal totalPrice
) {}
