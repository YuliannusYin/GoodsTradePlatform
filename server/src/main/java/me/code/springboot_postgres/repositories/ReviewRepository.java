/**
 * @file ReviewRepository.java
 * @description 评价数据访问接口，提供评价的查询和统计方法
 * @input 商品ID、用户ID
 * @output 评价实体列表、平均评分或计数结果
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 评价数据访问接口
 * 职责：提供对评价表的基本CRUD操作及按商品、用户查询和统计的自定义方法
 */
public interface ReviewRepository extends JpaRepository<Review, String> {

    // 根据商品ID查询所有评价
    List<Review> findByProductId(String productId);

    // 根据用户ID查询所有评价
    List<Review> findByUserId(String userId);

    /**
     * 计算指定商品的平均评分
     * @param productId 商品ID
     * @return 平均评分（可能为空）
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> getAverageRatingByProductId(@Param("productId") String productId);

    // 统计指定商品的评价数量
    int countByProductId(String productId);

    // 根据用户ID和商品ID查询评价（用于判断是否已评价）
    Optional<Review> findByUserIdAndProductId(String userId, String productId);

    // 根据用户ID删除所有评价记录（用户删除前清理）
    @Transactional
    void deleteByUserId(String userId);

    // 根据商品ID删除所有评价记录（商品删除前清理）
    @Transactional
    void deleteByProductId(String productId);
}
