/**
 * @file AddToCartDTO.java
 * @description 添加商品到购物车的请求数据传输对象
 * @input 商品ID和数量
 * @output 无（仅作为请求体接收数据）
 */
package me.code.springboot_postgres.dtos.requests;

/**
 * 添加购物车请求DTO
 * 职责：封装添加商品到购物车时前端提交的请求数据
 *
 * @param productId 商品ID
 * @param quantity  商品数量
 */
public record AddToCartDTO(
        String productId,
        int quantity
) {
}
