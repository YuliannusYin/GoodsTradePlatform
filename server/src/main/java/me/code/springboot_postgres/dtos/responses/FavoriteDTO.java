package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.Favorite;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FavoriteDTO(
    String id,
    String productId,
    String productName,
    String imageUrl,
    BigDecimal price,
    LocalDateTime createdAt
) {
    public static FavoriteDTO from(Favorite favorite) {
        var p = favorite.getProduct();
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
