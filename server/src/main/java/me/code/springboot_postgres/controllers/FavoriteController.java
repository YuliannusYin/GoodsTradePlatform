/**
 * @file FavoriteController.java
 * @description 用户收藏控制器，提供添加收藏、取消收藏、查询收藏列表和检查收藏状态的接口
 * @input 认证用户信息、商品ID
 * @output 统一API响应包装的收藏数据
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.FavoriteDTO;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收藏控制器
 * 职责：处理用户对商品的收藏操作，包括添加、移除、查询和状态检查
 */
@RestController
@RequestMapping("api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 添加商品到收藏夹
     * @param user 当前认证用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addFavorite(@AuthenticationPrincipal User user, @RequestParam String productId) {
        return favoriteService.addFavorite(user, productId).toResponseEntity();
    }

    /**
     * 从收藏夹中移除指定商品
     * @param user 当前认证用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@AuthenticationPrincipal User user, @PathVariable String productId) {
        return favoriteService.removeFavorite(user, productId).toResponseEntity();
    }

    /**
     * 获取当前用户的收藏列表
     * @param user 当前认证用户
     * @return 收藏商品列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getUserFavorites(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Favorites retrieved", favoriteService.getUserFavorites(user.getId())).toResponseEntity();
    }

    /**
     * 检查指定商品是否已被当前用户收藏
     * @param user 当前认证用户
     * @param productId 商品ID
     * @return 是否已收藏
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(@AuthenticationPrincipal User user, @PathVariable String productId) {
        return ApiResponse.ok("Favorite status checked", favoriteService.isFavorite(user.getId(), productId)).toResponseEntity();
    }
}
