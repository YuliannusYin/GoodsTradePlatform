/**
 * @file CartItemRepository.java
 * @description 购物车项数据访问接口，提供购物车项的查询、删除方法
 * @input 用户ID、商品ID
 * @output 购物车项实体列表、单个购物车项实体或删除操作
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 购物车项数据访问接口
 * 职责：提供对购物车项表的基本CRUD操作及按用户、商品查询和删除的自定义方法
 */
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    // 根据用户ID查询该用户的所有购物车项
    List<CartItem> findByUserId(String userId);

    // 根据用户ID和商品ID查询购物车项（用于判断商品是否已在购物车中）
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    // 根据用户ID删除该用户的所有购物车项（清空购物车）
    @Transactional
    void deleteByUserId(String userId);

    // 根据用户ID和商品ID删除指定购物车项（移除单个商品）
    @Transactional
    void deleteByUserIdAndProductId(String userId, String productId);

    // 根据商品ID删除所有关联的购物车项（商品删除前清理）
    @Transactional
    void deleteByProductId(String productId);
}
