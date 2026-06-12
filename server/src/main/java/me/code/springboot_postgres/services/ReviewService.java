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

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    @Transactional
    public ReviewDTO createReview(User user, CreateReviewDTO dto) {
        if (dto.rating() < 1 || dto.rating() > 5) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        Product product = productService.loadProductById(dto.productId());
        if (reviewRepository.findByUserIdAndProductId(user.getId(), dto.productId()).isPresent()) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "You have already reviewed this product");
        }
        Review review = new Review(dto.rating(), dto.comment(), user, product);
        return ReviewDTO.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductRatingDTO getProductRating(String productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId).orElse(0.0);
        int reviewCount = reviewRepository.countByProductId(productId);
        return new ProductRatingDTO(Math.round(avgRating * 10.0) / 10.0, reviewCount);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewDTO::from).toList();
    }

    @Transactional
    public ApiResponse<Void> deleteReview(User user, String reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Review not found with id: " + reviewId));
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "You can only delete your own reviews");
        }
        reviewRepository.delete(review);
        return ApiResponse.ok("Review deleted successfully");
    }
}
