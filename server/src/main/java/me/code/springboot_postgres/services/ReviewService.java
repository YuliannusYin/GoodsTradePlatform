package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.CreateReviewDTO;
import me.code.springboot_postgres.dtos.responses.entities.ProductRatingDTO;
import me.code.springboot_postgres.dtos.responses.entities.ReviewDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.Review;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final UserAccountService userAccountService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductService productService, UserAccountService userAccountService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.userAccountService = userAccountService;
    }

    public Review createReview(User user, CreateReviewDTO dto) {
        if (dto.rating() < 1 || dto.rating() > 5) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Product product = productService.loadProductById(dto.productId());

        Review existingReview = reviewRepository.findByUserIdAndProductId(user.getId(), dto.productId()).orElse(null);
        if (existingReview != null) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "You have already reviewed this product");
        }

        Review review = new Review(dto.rating(), dto.comment(), user, product);
        return reviewRepository.save(review);
    }

    public List<ReviewDTO> getReviewsByProductId(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return reviews.stream()
                .map(review -> new ReviewDTO(
                        review.getId(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt().format(formatter),
                        review.getUser().getUsername(),
                        review.getProduct().getId()))
                .toList();
    }

    public ProductRatingDTO getProductRating(String productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId).orElse(0.0);
        int reviewCount = reviewRepository.countByProductId(productId);
        return new ProductRatingDTO(Math.round(avgRating * 10.0) / 10.0, reviewCount);
    }

    public List<ReviewDTO> getReviewsByUserId(String userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return reviews.stream()
                .map(review -> new ReviewDTO(
                        review.getId(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt().format(formatter),
                        review.getUser().getUsername(),
                        review.getProduct().getId()))
                .toList();
    }

    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
