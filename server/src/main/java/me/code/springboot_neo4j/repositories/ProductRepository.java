package me.code.springboot_neo4j.repositories;

import me.code.springboot_neo4j.models.nodes.Product;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends Neo4jRepository<Product, String> {

    @Query("MATCH (p:Product) RETURN p")
    @NotNull List<Product> findAll();

    @Query("MATCH (p:Product) WHERE p.id = $id RETURN p")
    @NotNull Optional<Product> findById(@NotNull String id);

    @Query("MATCH (p:Product) RETURN p ORDER BY p.quantity DESC LIMIT $productAmount")
    @NotNull List<Product> findProductsWithBiggestQuantity(int productAmount);

    @Query("MATCH (p:Product) RETURN p ORDER BY p.price ASC")
    @NotNull List<Product> findAllProductsOrderedByLowestPrice();

    @Query("MATCH (p:Product) RETURN p ORDER BY p.price DESC")
    @NotNull List<Product> findAllProductsOrderedByHighestPrice();

    @Query("MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($query) RETURN p " +
            "ORDER BY CASE WHEN toLower(p.name) STARTS WITH toLower(SUBSTRING($query, 0, 1))" +
            " THEN 0 ELSE 1 END")
    @NotNull List<Product> findProductsBySearch(@NotNull String query);

    @Query("MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($query) RETURN p " +
            "ORDER BY p.price ASC")
    @NotNull List<Product> findSearchedProductsOrderedByLowestPrice(@NotNull String query);

    @Query("MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($query) RETURN p " +
            "ORDER BY p.price DESC")
    @NotNull List<Product> findSearchedProductsOrderedByHighestPrice(@NotNull String query);

    @Query("MATCH (p:Product) WHERE p.category = $category RETURN p")
    @NotNull List<Product> findByCategory(@NotNull String category);

    @Query("MATCH (p:Product) WHERE p.category = $category RETURN p ORDER BY p.price ASC")
    @NotNull List<Product> findByCategoryOrderedByLowestPrice(@NotNull String category);

    @Query("MATCH (p:Product) WHERE p.category = $category RETURN p ORDER BY p.price DESC")
    @NotNull List<Product> findByCategoryOrderedByHighestPrice(@NotNull String category);

    @Query("MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($query) AND p.category = $category RETURN p")
    @NotNull List<Product> findSearchedProductsByCategory(@NotNull String query, @NotNull String category);

    @Query("MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($query) AND p.category = $category RETURN p ORDER BY p.price ASC")
    @NotNull List<Product> findSearchedProductsByCategoryOrderedByLowestPrice(@NotNull String query, @NotNull String category);

    @Query("MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($query) AND p.category = $category RETURN p ORDER BY p.price DESC")
    @NotNull List<Product> findSearchedProductsByCategoryOrderedByHighestPrice(@NotNull String query, @NotNull String category);

    @Query("MATCH (p:Product)<-[:SELLS]-(u:User {id: $userId}) RETURN p")
    @NotNull List<Product> findBySellerId(@NotNull String userId);
}
