/**
 * @file ProductRepository.java
 * @description 商品数据访问接口，提供商品的查询方法，支持JPA动态条件查询
 * @input 卖家ID、商品状态、搜索关键词
 * @output 商品实体列表
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 商品数据访问接口
 * 职责：提供对商品表的基本CRUD操作、动态条件查询和按名称搜索的自定义方法
 */
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    // 根据卖家ID查询商品列表
    List<Product> findBySellerId(String sellerId);

    // 根据审核状态查询商品列表
    List<Product> findByStatus(Product.Status status);

    // 根据审核状态和卖家ID查询商品列表
    List<Product> findByStatusAndSellerId(Product.Status status, String sellerId);

    /**
     * 按商品名称模糊搜索，前缀匹配优先排序
     * @param query 搜索关键词
     * @return 匹配的商品列表（前缀匹配排在前面）
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY CASE WHEN LOWER(p.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END")
    List<Product> searchByName(@Param("query") String query);

    /**
     * 随机获取指定数量的已审核通过商品
     * 使用PostgreSQL的RANDOM()函数实现随机排序，取前count条
     * @param status 商品审核状态（APPROVED）
     * @param count 需要获取的商品数量
     * @return 随机排序后的商品列表
     */
    @Query(value = "SELECT * FROM products WHERE status = :status ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Product> findRandomByStatus(@Param("status") String status, @Param("count") int count);
}
