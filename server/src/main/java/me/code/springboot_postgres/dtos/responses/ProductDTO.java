package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import java.math.BigDecimal;
import java.util.List;

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
    public static ProductDTO from(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getImageUrls(),
            product.getPrice(),
            product.getQuantity(),
            product.getCategory() != null ? product.getCategory().name() : null,
            product.getCondition() != null ? product.getCondition().name() : null,
            product.getSource(),
            product.getStatus() != null ? product.getStatus().name() : null,
            product.getRejectReason(),
            product.getSeller() != null ? SellerDTO.from(product.getSeller()) : null
        );
    }

    public record SellerDTO(String id, String username) {
        public static SellerDTO from(User user) {
            return new SellerDTO(user.getId(), user.getUsername());
        }
    }
}
