/**
 * @file ReviewController.java
 * @description 评价控制器，提供添加评价、查询商品评价、查询用户评价、获取评分统计和删除评价的接口
 * @input 认证用户信息、创建评价DTO、商品ID、用户ID、评价ID
 * @output 统一API响应包装的评价数据
 */
package me.code.springboot_postgres.controllers;

import jakarta.validation.Valid;
import me.code.springboot_postgres.dtos.requests.CreateReviewDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.ProductRatingDTO;
import me.code.springboot_postgres.dtos.responses.ReviewDTO;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价控制器
 * 职责：处理商品评价的创建、查询、评分统计和删除操作
 */
@RestController
@RequestMapping("api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 添加商品评价
     * @param user 当前认证用户
     * @param dto 评价请求数据
     * @return 创建的评价信息
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(@AuthenticationPrincipal User user, @Valid @RequestBody CreateReviewDTO dto) {
        return ApiResponse.ok("评价添加成功", reviewService.createReview(user, dto)).toResponseEntity();
    }

    /**
     * 获取指定商品的所有评价
     * @param productId 商品ID
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getProductReviews(@PathVariable String productId) {
        return ApiResponse.ok("Reviews retrieved", reviewService.getReviewsByProductId(productId)).toResponseEntity();
    }

    /**
     * 获取指定商品的评分统计（平均评分和评价数量）
     * @param productId 商品ID
     * @return 评分统计数据
     */
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ApiResponse<ProductRatingDTO>> getProductRating(@PathVariable String productId) {
        return ApiResponse.ok("Rating retrieved", reviewService.getProductRating(productId)).toResponseEntity();
    }

    /**
     * 获取指定用户的所有评价
     * @param userId 用户ID
     * @return 评价列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getUserReviews(@PathVariable String userId) {
        return ApiResponse.ok("用户评价获取成功", reviewService.getReviewsByUserId(userId)).toResponseEntity();
    }

    /**
     * 删除评价（仅评价所有者或管理员可操作）
     * @param user 当前认证用户
     * @param reviewId 评价ID
     * @return 操作结果
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@AuthenticationPrincipal User user, @PathVariable String reviewId) {
        return reviewService.deleteReview(user, reviewId).toResponseEntity();
    }
}
