package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByProductId(String productId);

    List<Review> findByUserId(String userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> getAverageRatingByProductId(@Param("productId") String productId);

    int countByProductId(String productId);

    Optional<Review> findByUserIdAndProductId(String userId, String productId);
}
