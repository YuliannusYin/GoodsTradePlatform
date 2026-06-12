/**
 * @file ReviewDTO.java
 * @description 评价响应数据传输对象，返回评价的详细信息
 * @input 无（由实体转换构造）
 * @output 评价ID、评分、评论内容、创建时间、用户名和商品ID
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Review;
import java.time.LocalDateTime;

/**
 * 评价响应DTO
 * 职责：封装返回给前端的评价详细信息
 */
public record ReviewDTO(
    String id,
    int rating,
    String comment,
    LocalDateTime createdAt,
    String username,
    String productId
) {
    /**
     * 从评价实体转换为评价DTO
     * @param review 评价实体
     * @return 评价DTO对象
     */
    public static ReviewDTO from(Review review) {
        return new ReviewDTO(
            review.getId(),
            review.getRating(),
            review.getComment(),
            review.getCreatedAt(),
            // 空值安全处理用户名
            review.getUser() != null ? review.getUser().getUsername() : null,
            review.getProduct() != null ? review.getProduct().getId() : null
        );
    }
}
