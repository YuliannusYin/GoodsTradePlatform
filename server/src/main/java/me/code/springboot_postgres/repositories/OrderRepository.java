package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product " +
           "WHERE u.id = :userId")
    List<Order> findOrdersByUserId(@Param("userId") String userId);

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product")
    List<Order> findAllUsersOrders();

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product " +
           "WHERE o.status = :status")
    List<Order> findAllUsersOrdersByStatus(@Param("status") Order.Status status);
}
