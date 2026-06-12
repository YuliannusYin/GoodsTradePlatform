package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();

    @Query("SELECT p FROM Product p ORDER BY p.quantity DESC")
    List<Product> findTopByQuantityDesc(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY CASE WHEN LOWER(p.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END")
    List<Product> findProductsBySearch(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.price ASC")
    List<Product> findSearchedProductsOrderedByLowestPrice(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.price DESC")
    List<Product> findSearchedProductsOrderedByHighestPrice(@Param("query") String query);

    List<Product> findByCategory(Product.Category category);

    List<Product> findByCategoryOrderByPriceAsc(Product.Category category);

    List<Product> findByCategoryOrderByPriceDesc(Product.Category category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.category = :category")
    List<Product> findSearchedProductsByCategory(@Param("query") String query, @Param("category") Product.Category category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.category = :category ORDER BY p.price ASC")
    List<Product> findSearchedProductsByCategoryOrderedByLowestPrice(@Param("query") String query, @Param("category") Product.Category category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.category = :category ORDER BY p.price DESC")
    List<Product> findSearchedProductsByCategoryOrderedByHighestPrice(@Param("query") String query, @Param("category") Product.Category category);

    List<Product> findBySellerId(String sellerId);

    List<Product> findByStatus(Product.Status status);

    List<Product> findByStatusAndSellerId(Product.Status status, String sellerId);
}
