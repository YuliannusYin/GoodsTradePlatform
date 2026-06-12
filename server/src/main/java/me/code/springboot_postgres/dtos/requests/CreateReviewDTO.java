/**
 * @file CreateReviewDTO.java
 * @description 创建评价请求数据传输对象
 * @input 评分、评论内容、商品ID
 * @output 包含评价信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建评价请求DTO
 * 职责：封装用户提交商品评价时的评分、评论和商品ID
 */
public record CreateReviewDTO(
        @Min(1) @Max(5) int rating,
        String comment,
        @NotBlank String productId) {
}
