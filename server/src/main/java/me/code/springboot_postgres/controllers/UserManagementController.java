package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDTO;
import me.code.springboot_postgres.services.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Autowired
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ApiResponse.ok("Users retrieved", userManagementService.getAllUsers()).toResponseEntity();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable String userId) {
        return ApiResponse.ok("User retrieved", userManagementService.getUserById(userId)).toResponseEntity();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> assignRole(@PathVariable String userId, @RequestBody Map<String, String> body) {
        return userManagementService.assignRole(userId, body.get("role")).toResponseEntity();
    }

    @PatchMapping("/{userId}/toggle-enabled")
    public ResponseEntity<ApiResponse<Void>> toggleUserEnabled(@PathVariable String userId) {
        return userManagementService.toggleUserEnabled(userId).toResponseEntity();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        return userManagementService.deleteUser(userId).toResponseEntity();
    }
}
