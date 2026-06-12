/**
 * @file UserProductController.java
 * @description 商户商品控制器，提供商户添加商品、查看自己的商品、编辑和删除自己商品的接口
 * @input 认证用户信息、商品DTO、商品ID
 * @output 统一API响应包装的商品数据或操作结果
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.ProductService;
import me.code.springboot_postgres.services.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户商品控制器
 * 职责：处理商户对自己商品的添加、查看、编辑和删除操作
 */
@RestController
@RequestMapping("api/user_products")
public class UserProductController {

    private final ProductService productService;
    private final UserProductService userProductService;

    @Autowired
    public UserProductController(ProductService productService, UserProductService userProductService) {
        this.productService = productService;
        this.userProductService = userProductService;
    }

    /**
     * 商户提交新商品（需管理员审核）
     * @param user 当前认证用户
     * @param dto 商品请求数据
     * @return 提交的商品信息
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<me.code.springboot_postgres.dtos.responses.ProductDTO>> addProduct(@AuthenticationPrincipal User user, @RequestBody ProductDTO dto) {
        return ApiResponse.ok("Product submitted for review", userProductService.addProductByUser(user, dto)).toResponseEntity();
    }

    /**
     * 获取当前商户自己的所有商品
     * @param user 当前认证用户
     * @return 商品列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<me.code.springboot_postgres.dtos.responses.ProductDTO>>> getMyProducts(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("My products retrieved", productService.getProductsBySellerId(user.getId())).toResponseEntity();
    }

    /**
     * 编辑自己的商品信息
     * @param user 当前认证用户
     * @param productId 商品ID
     * @param dto 商品更新数据
     * @return 操作结果
     */
    @PutMapping("/edit/{productId}")
    public ResponseEntity<ApiResponse<Void>> editMyProduct(@AuthenticationPrincipal User user, @PathVariable String productId, @RequestBody ProductDTO dto) {
        return userProductService.editOwnProduct(user, productId, dto).toResponseEntity();
    }

    /**
     * 删除自己的商品
     * @param user 当前认证用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyProduct(@AuthenticationPrincipal User user, @PathVariable String productId) {
        return userProductService.deleteOwnProduct(user, productId).toResponseEntity();
    }
}
