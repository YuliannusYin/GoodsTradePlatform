/**
 * @file UserRepository.java
 * @description 用户数据访问接口，提供用户的查询和存在性检查方法
 * @input 邮箱、用户名
 * @output 用户实体或存在性布尔值
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 职责：提供对用户表的基本CRUD操作及按邮箱、用户名查询的自定义方法
 */
public interface UserRepository extends JpaRepository<User, String> {
    // 根据邮箱查询用户
    Optional<User> findByEmail(String email);
    // 根据用户名查询用户
    Optional<User> findByUsername(String username);
    // 检查邮箱是否已存在
    boolean existsByEmail(String email);
    // 检查用户名是否已存在
    boolean existsByUsername(String username);
}
