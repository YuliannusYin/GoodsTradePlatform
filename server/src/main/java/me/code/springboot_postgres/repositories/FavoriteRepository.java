/**
 * @file FavoriteRepository.java
 * @description 收藏数据访问接口，提供收藏记录的查询和统计方法
 * @input 用户ID、商品ID
 * @output 收藏实体列表、单个收藏实体或计数结果
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏数据访问接口
 * 职责：提供对收藏表的基本CRUD操作及按用户、商品查询的自定义方法
 */
public interface FavoriteRepository extends JpaRepository<Favorite, String> {

    // 根据用户ID查询所有收藏记录
    List<Favorite> findByUserId(String userId);

    // 根据用户ID和商品ID查询收藏记录（用于判断是否已收藏）
    Optional<Favorite> findByUserIdAndProductId(String userId, String productId);

    // 统计指定商品的收藏数量
    int countByProductId(String productId);

    // 统计指定用户的收藏数量
    int countByUserId(String userId);
}
