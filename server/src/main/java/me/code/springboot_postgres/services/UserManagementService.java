/**
 * @file UserManagementService.java
 * @description 用户管理服务类（超级管理员），提供查询用户、分配角色、启用/禁用、删除用户和调整余额的业务逻辑
 * @input 用户ID、角色名称、调整金额
 * @output 用户DTO列表、用户DTO或操作结果
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.CartItemRepository;
import me.code.springboot_postgres.repositories.FavoriteRepository;
import me.code.springboot_postgres.repositories.ReviewRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户管理服务类
 * 职责：处理超级管理员对用户的查询、角色分配、启用/禁用、删除和余额调整等管理操作
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public UserManagementService(UserRepository userRepository,
                                 CartItemRepository cartItemRepository,
                                 FavoriteRepository favoriteRepository,
                                 ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.favoriteRepository = favoriteRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * 获取所有用户列表
     * @return 用户DTO列表
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::from).toList();
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户DTO
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public UserDTO getUserById(String userId) {
        return UserDTO.from(userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId)));
    }

    /**
     * 为指定用户分配角色
     * @param userId 用户ID
     * @param roleName 角色名称字符串
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> assignRole(String userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        // 受保护账号不可修改角色
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot modify roles of a system account");
        }
        try {
            user.setRole(User.Role.valueOf(roleName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 无效的角色名称
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }
        userRepository.save(user);
        return ApiResponse.ok("Role assigned successfully");
    }

    /**
     * 切换用户的启用/禁用状态
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> toggleUserEnabled(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        // 受保护账号不可禁用
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot disable a system account");
        }
        // 切换启用状态
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ApiResponse.ok("User " + (user.isEnabled() ? "enabled" : "disabled") + " successfully");
    }

    /**
     * 删除指定用户
     * 删除前清理该用户的购物车项、收藏记录和评价记录，订单保留用于业务记录
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        // 受保护账号不可删除
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot delete a system account");
        }
        // 清理用户关联的购物车项，避免外键约束冲突
        cartItemRepository.deleteByUserId(userId);
        // 清理用户关联的收藏记录，避免外键约束冲突
        favoriteRepository.deleteByUserId(userId);
        // 清理用户关联的评价记录，避免外键约束冲突
        reviewRepository.deleteByUserId(userId);
        // 注意：不删除订单记录，保留用于业务记录
        userRepository.delete(user);
        return ApiResponse.ok("User deleted successfully");
    }

    /**
     * 调整指定用户的余额
     * @param userId 用户ID
     * @param amount 调整金额（正数为充值，负数为扣减）
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> adjustBalance(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            // 充值：直接增加余额
            user.setBalance(user.getBalance().add(amount));
        } else {
            // 扣减：通过 deductBalance 统一进行负数校验
            user.deductBalance(amount.negate());
        }
        userRepository.save(user);
        return ApiResponse.ok("余额调整成功，当前余额：" + user.getBalance());
    }
}
