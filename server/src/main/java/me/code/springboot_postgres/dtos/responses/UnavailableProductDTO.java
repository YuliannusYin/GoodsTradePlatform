/**
 * @file UnavailableProductDTO.java
 * @description 不可用商品响应数据传输对象，返回下单时库存不足或商品不存在的提示信息
 * @input 无（由服务层构造）
 * @output 提示消息、商品ID、请求数量和可用数量
 */
package me.code.springboot_postgres.dtos.responses;

/**
 * 不可用商品响应DTO
 * 职责：封装下单时库存不足或商品不存在时的错误提示信息
 */
public record UnavailableProductDTO(
    String message,
    String productId,
    int requestedAmount,
    int availableAmount
) {}
