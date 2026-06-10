package me.code.springboot_neo4j.repositories;

import me.code.springboot_neo4j.models.nodes.Favorite;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends Neo4jRepository<Favorite, String> {

    @Query("MATCH (f:Favorite)-[:FAVORITED_BY]->(u:User {id: $userId}) RETURN f")
    @NotNull List<Favorite> findByUserId(@NotNull String userId);

    @Query("MATCH (f:Favorite)-[:FAVORITED_BY]->(u:User {id: $userId})-[:FAVORITE_PRODUCT]->(p:Product {id: $productId}) RETURN f")
    Optional<Favorite> findByUserIdAndProductId(@NotNull String userId, @NotNull String productId);

    @Query("MATCH (f:Favorite)-[:FAVORITE_PRODUCT]->(p:Product {id: $productId}) RETURN COUNT(f)")
    int countByProductId(@NotNull String productId);

    @Query("MATCH (f:Favorite)-[:FAVORITED_BY]->(u:User {id: $userId}) RETURN COUNT(f)")
    int countByUserId(@NotNull String userId);
}
