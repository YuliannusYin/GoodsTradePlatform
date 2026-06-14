/**
 * @file UserManagementController.java
 * @description 用户管理控制器（超级管理员），提供查询用户、分配角色、启用/禁用用户和删除用户的接口
 * @input 用户ID、角色名称
 * @output 统一API响应包装的用户数据或操作结果
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.AdjustBalanceDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDTO;
import me.code.springboot_postgres.services.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 职责：提供超级管理员对用户的查询、角色分配、启用/禁用和删除操作
 */
@RestController
@RequestMapping("api/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Autowired
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ApiResponse.ok("Users retrieved", userManagementService.getAllUsers()).toResponseEntity();
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable String userId) {
        return ApiResponse.ok("User retrieved", userManagementService.getUserById(userId)).toResponseEntity();
    }

    /**
     * 为指定用户分配角色
     * @param userId 用户ID
     * @param body 请求体，包含role字段
     * @return 操作结果
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> assignRole(@PathVariable String userId, @RequestBody Map<String, String> body) {
        return userManagementService.assignRole(userId, body.get("role")).toResponseEntity();
    }

    /**
     * 切换用户的启用/禁用状态
     * @param userId 用户ID
     * @return 操作结果
     */
    @PatchMapping("/{userId}/toggle-enabled")
    public ResponseEntity<ApiResponse<Void>> toggleUserEnabled(@PathVariable String userId) {
        return userManagementService.toggleUserEnabled(userId).toResponseEntity();
    }

    /**
     * 删除指定用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        return userManagementService.deleteUser(userId).toResponseEntity();
    }

    /**
     * 调整指定用户的余额（管理员操作）
     * @param dto 请求体，包含userId和amount字段
     * @return 操作结果
     */
    @PutMapping("/balance")
    public ResponseEntity<ApiResponse<Void>> adjustBalance(@RequestBody AdjustBalanceDTO dto) {
        return userManagementService.adjustBalance(dto.userId(), dto.amount()).toResponseEntity();
    }
}
