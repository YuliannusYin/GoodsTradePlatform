/**
 * @file CartController.java
 * @description 购物车控制器，提供购物车的增删改查和合并接口
 * @input 认证用户信息、商品ID、数量、本地购物车数据
 * @output 统一API响应包装的购物车数据
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.AddToCartDTO;
import me.code.springboot_postgres.dtos.requests.MergeCartDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.CartDTO;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 * 职责：处理用户对购物车的操作请求，包括获取、添加、更新、移除、清空和合并购物车
 */
@RestController
@RequestMapping("api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 获取当前用户的购物车
     * @param user 当前认证用户
     * @return 购物车数据
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@AuthenticationPrincipal User user) {
        CartDTO cart = cartService.getCart(user.getId());
        return ApiResponse.ok("购物车获取成功", cart).toResponseEntity();
    }

    /**
     * 添加商品到购物车
     * @param user        当前认证用户
     * @param addToCartDTO 添加购物车请求体（包含商品ID和数量）
     * @return 更新后的购物车数据
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(@AuthenticationPrincipal User user,
                                                        @RequestBody AddToCartDTO addToCartDTO) {
        CartDTO cart = cartService.addItem(user.getId(), addToCartDTO.productId(), addToCartDTO.quantity());
        return ApiResponse.ok("商品已添加到购物车", cart).toResponseEntity();
    }

    /**
     * 更新购物车中指定商品的数量
     * @param user      当前认证用户
     * @param productId 商品ID
     * @param quantity   新数量
     * @return 更新后的购物车数据
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItemQuantity(@AuthenticationPrincipal User user,
                                                                   @PathVariable String productId,
                                                                   @RequestParam int quantity) {
        CartDTO cart = cartService.updateItemQuantity(user.getId(), productId, quantity);
        return ApiResponse.ok("购物车商品数量已更新", cart).toResponseEntity();
    }

    /**
     * 移除购物车中的指定商品
     * @param user      当前认证用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@AuthenticationPrincipal User user,
                                                        @PathVariable String productId) {
        cartService.removeItem(user.getId(), productId);
        // 显式指定泛型类型，避免类型推断为 ApiResponse<Object>
        return ApiResponse.<Void>ok("商品已从购物车移除").toResponseEntity();
    }

    /**
     * 清空当前用户的购物车
     * @param user 当前认证用户
     * @return 操作结果
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        // 显式指定泛型类型，避免类型推断为 ApiResponse<Object>
        return ApiResponse.<Void>ok("购物车已清空").toResponseEntity();
    }

    /**
     * 合并本地购物车到后端购物车
     * @param user         当前认证用户
     * @param mergeCartDTO 合并购物车请求体（包含本地购物车商品条目列表）
     * @return 合并后的购物车数据
     */
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartDTO>> mergeCart(@AuthenticationPrincipal User user,
                                                          @RequestBody MergeCartDTO mergeCartDTO) {
        CartDTO cart = cartService.mergeCart(user.getId(), mergeCartDTO.items());
        return ApiResponse.ok("购物车合并成功", cart).toResponseEntity();
    }
}
