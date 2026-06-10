package me.code.springboot_neo4j.controllers;

import me.code.springboot_neo4j.dtos.requests.CreateReviewDTO;
import me.code.springboot_neo4j.dtos.responses.entities.ProductRatingDTO;
import me.code.springboot_neo4j.dtos.responses.entities.ReviewDTO;
import me.code.springboot_neo4j.models.nodes.User;
import me.code.springboot_neo4j.services.ReviewService;
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
    public ResponseEntity<ReviewDTO> addReview(@AuthenticationPrincipal User user, @RequestBody CreateReviewDTO dto) {
        var review = reviewService.createReview(user, dto);
        var reviewDTO = new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt().toString(),
                review.getUser().getUsername(),
                review.getProduct().getId());
        return ResponseEntity.ok(reviewDTO);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable String productId) {
        var result = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ProductRatingDTO> getProductRating(@PathVariable String productId) {
        var result = reviewService.getProductRating(productId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getUserReviews(@PathVariable String userId) {
        var result = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }
}
