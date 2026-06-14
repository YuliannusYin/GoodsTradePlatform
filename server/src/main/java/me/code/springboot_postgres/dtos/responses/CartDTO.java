/**
 * @file CartDTO.java
 * @description 购物车响应数据传输对象，返回购物车的完整信息
 * @input 无（由实体列表转换构造）
 * @output 购物车商品列表，包含商品ID、名称、价格、数量、图片和库存信息
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.CartItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应DTO
 * 职责：封装返回给前端的购物车完整信息
 *
 * @param items 购物车商品条目列表
 */
public record CartDTO(
        List<CartItemDTO> items
) {
    /**
     * 购物车商品条目DTO
     * 职责：封装购物车中单个商品的详细信息
     *
     * @param productId     商品ID
     * @param productName   商品名称
     * @param price         商品单价
     * @param quantity      购物车中的商品数量
     * @param imageUrls     商品图片URL列表
     * @param stockQuantity 商品库存数量
     */
    public record CartItemDTO(
            String productId,
            String productName,
            BigDecimal price,
            int quantity,
            List<String> imageUrls,
            int stockQuantity
    ) {
    }

    /**
     * 从购物车项实体列表转换为购物车DTO
     * @param items 购物车项实体列表
     * @return 购物车DTO对象
     */
    public static CartDTO from(List<CartItem> items) {
        List<CartItemDTO> cartItemDTOs = items.stream()
                .map(item -> {
                    var product = item.getProduct();
                    return new CartItemDTO(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            item.getQuantity(),
                            product.getImageUrls(),
                            product.getQuantity()
                    );
                })
                .toList();
        return new CartDTO(cartItemDTOs);
    }
}
