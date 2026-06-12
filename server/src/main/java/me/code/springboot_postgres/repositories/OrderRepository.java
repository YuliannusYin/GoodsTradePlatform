/**
 * @file OrderRepository.java
 * @description 订单数据访问接口，提供订单的查询方法，包含关联查询优化
 * @input 用户ID、订单状态
 * @output 订单实体列表
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 订单数据访问接口
 * 职责：提供对订单表的基本CRUD操作及带关联抓取的自定义查询方法
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * 根据用户ID查询订单列表（关联抓取用户、订单项和商品）
     * @param userId 用户ID
     * @return 该用户的所有订单（含关联数据）
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product " +
           "WHERE u.id = :userId")
    List<Order> findOrdersByUserId(@Param("userId") String userId);

    /**
     * 查询所有用户的所有订单（关联抓取用户、订单项和商品）
     * @return 所有订单（含关联数据）
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product")
    List<Order> findAllUsersOrders();

    /**
     * 根据订单状态查询所有用户的订单（关联抓取用户、订单项和商品）
     * @param status 订单状态
     * @return 指定状态的订单列表
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product " +
           "WHERE o.status = :status")
    List<Order> findAllUsersOrdersByStatus(@Param("status") Order.Status status);
}
