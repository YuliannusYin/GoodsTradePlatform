/**
 * @file FavoriteService.java
 * @description 收藏服务类，提供添加收藏、移除收藏、查询收藏列表和检查收藏状态的业务逻辑
 * @input 用户实体、商品ID、用户ID
 * @output 操作结果或收藏DTO列表
 */
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

/**
 * 收藏服务类
 * 职责：处理用户收藏商品的添加、移除、查询和状态检查等业务逻辑
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductService productService;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository, ProductService productService) {
        this.favoriteRepository = favoriteRepository;
        this.productService = productService;
    }

    /**
     * 添加商品到收藏夹
     * @param user 当前用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> addFavorite(User user, String productId) {
        Product product = productService.loadProductById(productId);
        // 检查是否已收藏，避免重复收藏
        if (favoriteRepository.findByUserIdAndProductId(user.getId(), productId).isPresent()) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Product is already in favorites");
        }
        favoriteRepository.save(new Favorite(user, product));
        return ApiResponse.ok("Product added to favorites");
    }

    /**
     * 从收藏夹中移除指定商品
     * @param user 当前用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> removeFavorite(User user, String productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Favorite not found"));
        favoriteRepository.deleteById(favorite.getId());
        return ApiResponse.ok("Product removed from favorites");
    }

    /**
     * 获取指定用户的收藏列表
     * @param userId 用户ID
     * @return 收藏DTO列表
     */
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getUserFavorites(String userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(FavoriteDTO::from).toList();
    }

    /**
     * 检查指定商品是否已被用户收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(String userId, String productId) {
        return favoriteRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    /**
     * 获取指定商品的收藏数量
     * @param productId 商品ID
     * @return 收藏数量
     */
    @Transactional(readOnly = true)
    public int getFavoriteCountByProductId(String productId) {
        return favoriteRepository.countByProductId(productId);
    }
}
