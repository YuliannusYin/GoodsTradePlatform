/**
 * @file MergeCartDTO.java
 * @description 合并购物车的请求数据传输对象，用于将本地购物车数据合并到后端
 * @input 本地购物车商品条目列表
 * @output 无（仅作为请求体接收数据）
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 合并购物车请求DTO
 * 职责：封装用户登录后将本地购物车数据合并到后端购物车时提交的请求数据
 *
 * @param items 本地购物车商品条目列表
 */
public record MergeCartDTO(
        @NotEmpty List<@Valid CartItemEntry> items
) {
    /**
     * 购物车商品条目
     * 职责：表示本地购物车中的单个商品条目
     *
     * @param productId 商品ID
     * @param quantity  商品数量
     */
    public record CartItemEntry(
            @NotNull String productId,
            @NotNull @jakarta.validation.constraints.Min(1) Integer quantity
    ) {
    }
}
