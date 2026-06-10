package me.code.springboot_neo4j.repositories;

import me.code.springboot_neo4j.models.nodes.Review;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends Neo4jRepository<Review, String> {

    @Query("MATCH (r:Review)-[:ABOUT]->(p:Product {id: $productId}) RETURN r")
    @NotNull List<Review> findByProductId(@NotNull String productId);

    @Query("MATCH (r:Review)-[:WRITTEN_BY]->(u:User {id: $userId}) RETURN r")
    @NotNull List<Review> findByUserId(@NotNull String userId);

    @Query("MATCH (r:Review)-[:ABOUT]->(p:Product {id: $productId}) RETURN AVG(r.rating)")
    Optional<Double> getAverageRatingByProductId(@NotNull String productId);

    @Query("MATCH (r:Review)-[:ABOUT]->(p:Product {id: $productId}) RETURN COUNT(r)")
    int countByProductId(@NotNull String productId);

    @Query("MATCH (r:Review)-[:WRITTEN_BY]->(u:User {id: $userId})-[:ABOUT]->(p:Product {id: $productId}) RETURN r")
    Optional<Review> findByUserIdAndProductId(@NotNull String userId, @NotNull String productId);
}
