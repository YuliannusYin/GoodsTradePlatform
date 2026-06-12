/**
 * @file FavoriteDTO.java
 * @description 收藏响应数据传输对象，返回收藏记录的详细信息
 * @input 无（由实体转换构造）
 * @output 收藏ID、商品ID、商品名称、图片、价格和创建时间
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Favorite;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏响应DTO
 * 职责：封装返回给前端的收藏商品信息
 */
public record FavoriteDTO(
    String id,
    String productId,
    String productName,
    String imageUrl,
    BigDecimal price,
    LocalDateTime createdAt
) {
    /**
     * 从收藏实体转换为收藏DTO
     * @param favorite 收藏实体
     * @return 收藏DTO对象
     */
    public static FavoriteDTO from(Favorite favorite) {
        var p = favorite.getProduct();
        // 取商品第一张图片作为封面，无图片则返回空字符串
        String imageUrl = (p.getImageUrls() != null && !p.getImageUrls().isEmpty())
                ? p.getImageUrls().get(0) : "";
        return new FavoriteDTO(
            favorite.getId(),
            p.getId(),
            p.getName(),
            imageUrl,
            p.getPrice(),
            favorite.getCreatedAt()
        );
    }
}
