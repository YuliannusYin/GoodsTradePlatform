/**
 * @file ProductController.java
 * @description 商品控制器，提供商品浏览、搜索、分类查询和获取商品/分类/成色枚举的公开接口
 * @input 商品ID、搜索关键词、筛选条件、分类名称
 * @output 统一API响应包装的商品数据或枚举列表
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.ProductDTO;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * 职责：提供商品浏览、搜索、分类查询等公开接口，无需认证即可访问
 */
@RestController
@RequestMapping("api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取所有已审核通过的商品列表
     * @return 商品列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts() {
        return ApiResponse.ok("Products retrieved", productService.getProducts()).toResponseEntity();
    }

    /**
     * 获取精选商品列表（按库存降序取前4个）
     * @return 精选商品列表
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFeaturedProducts() {
        return ApiResponse.ok("Featured products retrieved", productService.getFeaturedProducts()).toResponseEntity();
    }

    /**
     * 随机获取推荐商品列表
     * @param count 获取数量，默认8个（2行×4列）
     * @return 随机排序的商品列表
     */
    @GetMapping("/random")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRandomProducts(
            @RequestParam(defaultValue = "8") int count) {
        return ApiResponse.ok("随机推荐商品获取成功", productService.getRandomProducts(count)).toResponseEntity();
    }

    /**
     * 根据商品ID获取单个商品详情
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable String productId) {
        return ApiResponse.ok("Product retrieved", productService.getProduct(productId)).toResponseEntity();
    }

    /**
     * 搜索商品，支持关键词、筛选条件和分类过滤
     * @param query 搜索关键词
     * @param filter 排序筛选条件（lowest_price/highest_price）
     * @param category 商品分类（可选）
     * @return 搜索结果商品列表
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam String filter,
            @RequestParam(required = false) String category) {
        return ApiResponse.ok("Search results retrieved", productService.getSearchedProducts(query, filter, category)).toResponseEntity();
    }

    /**
     * 根据分类获取商品列表
     * @param category 分类名称
     * @return 指定分类的商品列表
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        return ApiResponse.ok("Products by category retrieved", productService.getProductsByCategory(category)).toResponseEntity();
    }

    /**
     * 获取所有商品分类枚举
     * @return 分类枚举数组
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Product.Category[]>> getCategories() {
        return ApiResponse.ok("Categories retrieved", Product.Category.values()).toResponseEntity();
    }

    /**
     * 获取所有商品成色枚举
     * @return 成色枚举数组
     */
    @GetMapping("/conditions")
    public ResponseEntity<ApiResponse<Product.Condition[]>> getConditions() {
        return ApiResponse.ok("Conditions retrieved", Product.Condition.values()).toResponseEntity();
    }
}
