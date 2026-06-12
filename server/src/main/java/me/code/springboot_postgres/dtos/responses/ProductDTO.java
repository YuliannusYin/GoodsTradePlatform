/**
 * @file ProductDTO.java
 * @description 商品响应数据传输对象，返回商品的完整信息
 * @input 无（由实体转换构造）
 * @output 商品ID、名称、描述、图片、价格、数量、分类、成色、来源、状态、驳回原因和卖家信息
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品响应DTO
 * 职责：封装返回给前端的商品完整信息，包含卖家摘要
 */
public record ProductDTO(
    String id,
    String name,
    String description,
    List<String> imageUrls,
    BigDecimal price,
    int quantity,
    String category,
    String condition,
    String source,
    String status,
    String rejectReason,
    SellerDTO seller
) {
    /**
     * 从商品实体转换为商品DTO
     * @param product 商品实体
     * @return 商品DTO对象
     */
    public static ProductDTO from(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getImageUrls(),
            product.getPrice(),
            product.getQuantity(),
            // 枚举转字符串，空值安全处理
            product.getCategory() != null ? product.getCategory().name() : null,
            product.getCondition() != null ? product.getCondition().name() : null,
            product.getSource(),
            product.getStatus() != null ? product.getStatus().name() : null,
            product.getRejectReason(),
            product.getSeller() != null ? SellerDTO.from(product.getSeller()) : null
        );
    }

    /**
     * 卖家摘要DTO
     * 职责：封装商品卖家基本信息
     */
    public record SellerDTO(String id, String username) {
        /**
         * 从用户实体转换为卖家摘要DTO
         * @param user 用户实体
         * @return 卖家摘要DTO对象
         */
        public static SellerDTO from(User user) {
            return new SellerDTO(user.getId(), user.getUsername());
        }
    }
}
