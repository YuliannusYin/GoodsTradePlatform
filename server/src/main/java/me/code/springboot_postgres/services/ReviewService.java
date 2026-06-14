/**
 * @file ReviewService.java
 * @description 评价服务类，提供创建评价、查询评价、获取评分统计和删除评价的业务逻辑
 * @input 用户实体、创建评价DTO、商品ID、用户ID、评价ID
 * @output 评价DTO、评分统计DTO或操作结果
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.CreateReviewDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.ProductRatingDTO;
import me.code.springboot_postgres.dtos.responses.ReviewDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.Review;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价服务类
 * 职责：处理商品评价的创建、查询、评分统计和删除等业务逻辑
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    /**
     * 创建商品评价
     * @param user 评价用户
     * @param dto 评价请求数据
     * @return 创建的评价DTO
     */
    @Transactional
    public ReviewDTO createReview(User user, CreateReviewDTO dto) {
        // 验证评分范围
        if (dto.rating() < 1 || dto.rating() > 5) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        Product product = productService.loadProductById(dto.productId());
        // 检查是否已评价过该商品
        if (reviewRepository.findByUserIdAndProductId(user.getId(), dto.productId()).isPresent()) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "You have already reviewed this product");
        }
        Review review = new Review(dto.rating(), dto.comment(), user, product);
        return ReviewDTO.from(reviewRepository.save(review));
    }

    /**
     * 获取指定商品的所有评价
     * @param productId 商品ID
     * @return 评价DTO列表
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewDTO::from).toList();
    }

    /**
     * 获取指定商品的评分统计（平均评分和评价数量）
     * @param productId 商品ID
     * @return 评分统计DTO
     */
    @Transactional(readOnly = true)
    public ProductRatingDTO getProductRating(String productId) {
        // 获取平均评分，无评价时默认为0.0
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId).orElse(0.0);
        int reviewCount = reviewRepository.countByProductId(productId);
        // 平均评分保留一位小数
        return new ProductRatingDTO(Math.round(avgRating * 10.0) / 10.0, reviewCount);
    }

    /**
     * 获取指定用户的所有评价
     * @param userId 用户ID
     * @return 评价DTO列表
     */
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewDTO::from).toList();
    }

    /**
     * 删除评价（仅评价所有者或管理员可操作）
     * @param user 当前用户
     * @param reviewId 评价ID
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> deleteReview(User user, String reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Review not found with id: " + reviewId));
        // 检查是否为评价所有者或管理员
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "You can only delete your own reviews");
        }
        reviewRepository.delete(review);
        return ApiResponse.ok("Review deleted successfully");
    }
}
