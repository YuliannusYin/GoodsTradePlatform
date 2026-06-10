package me.code.springboot_neo4j.services;

import me.code.springboot_neo4j.dtos.responses.entities.FavoriteDTO;
import me.code.springboot_neo4j.dtos.responses.success.Success;
import me.code.springboot_neo4j.exceptions.types.CustomRuntimeException;
import me.code.springboot_neo4j.models.nodes.Favorite;
import me.code.springboot_neo4j.models.nodes.Product;
import me.code.springboot_neo4j.models.nodes.User;
import me.code.springboot_neo4j.repositories.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductService productService;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository, ProductService productService) {
        this.favoriteRepository = favoriteRepository;
        this.productService = productService;
    }

    public Success addFavorite(User user, String productId) {
        Product product = productService.loadProductById(productId);

        Favorite existing = favoriteRepository.findByUserIdAndProductId(user.getId(), productId).orElse(null);
        if (existing != null) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Product is already in favorites");
        }

        Favorite favorite = new Favorite(user, product);
        favoriteRepository.save(favorite);

        return new Success(HttpStatus.OK, "Product added to favorites");
    }

    public Success removeFavorite(User user, String productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Favorite not found"));

        favoriteRepository.deleteById(favorite.getId());

        return new Success(HttpStatus.OK, "Product removed from favorites");
    }

    public List<FavoriteDTO> getUserFavorites(String userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return favorites.stream()
                .map(fav -> {
                    Product p = fav.getProduct();
                    String imageUrl = (p.getImageUrls() != null && !p.getImageUrls().isEmpty())
                            ? p.getImageUrls().get(0) : "";
                    return new FavoriteDTO(
                            fav.getId(),
                            p.getId(),
                            p.getName(),
                            imageUrl,
                            p.getPrice(),
                            fav.getCreatedAt().format(formatter));
                })
                .toList();
    }

    public boolean isFavorite(String userId, String productId) {
        return favoriteRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    public int getFavoriteCountByProductId(String productId) {
        return favoriteRepository.countByProductId(productId);
    }
}
