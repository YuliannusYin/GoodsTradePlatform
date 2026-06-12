package me.code.springboot_postgres.controllers;

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

@RestController
@RequestMapping("api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(@AuthenticationPrincipal User user, @RequestBody CreateReviewDTO dto) {
        return ApiResponse.ok("Review added", reviewService.createReview(user, dto)).toResponseEntity();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getProductReviews(@PathVariable String productId) {
        return ApiResponse.ok("Reviews retrieved", reviewService.getReviewsByProductId(productId)).toResponseEntity();
    }

    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ApiResponse<ProductRatingDTO>> getProductRating(@PathVariable String productId) {
        return ApiResponse.ok("Rating retrieved", reviewService.getProductRating(productId)).toResponseEntity();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getUserReviews(@PathVariable String userId) {
        return ApiResponse.ok("User reviews retrieved", reviewService.getReviewsByUserId(userId)).toResponseEntity();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@AuthenticationPrincipal User user, @PathVariable String reviewId) {
        return reviewService.deleteReview(user, reviewId).toResponseEntity();
    }
}
