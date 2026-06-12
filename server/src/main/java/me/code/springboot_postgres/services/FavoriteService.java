package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.FavoriteDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Favorite;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ApiResponse<Void> addFavorite(User user, String productId) {
        Product product = productService.loadProductById(productId);
        if (favoriteRepository.findByUserIdAndProductId(user.getId(), productId).isPresent()) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Product is already in favorites");
        }
        favoriteRepository.save(new Favorite(user, product));
        return ApiResponse.ok("Product added to favorites");
    }

    @Transactional
    public ApiResponse<Void> removeFavorite(User user, String productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Favorite not found"));
        favoriteRepository.deleteById(favorite.getId());
        return ApiResponse.ok("Product removed from favorites");
    }

    @Transactional(readOnly = true)
    public List<FavoriteDTO> getUserFavorites(String userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(FavoriteDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String userId, String productId) {
        return favoriteRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    @Transactional(readOnly = true)
    public int getFavoriteCountByProductId(String productId) {
        return favoriteRepository.countByProductId(productId);
    }
}
