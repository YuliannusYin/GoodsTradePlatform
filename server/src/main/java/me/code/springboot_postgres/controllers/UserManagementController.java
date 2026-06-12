package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.models.entities.User;
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
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userManagementService.getUserById(userId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Success> assignRole(@PathVariable String userId, @RequestBody Map<String, String> body) {
        return userManagementService.assignRole(userId, body.get("role")).toResponseEntity();
    }

    @PatchMapping("/{userId}/toggle-enabled")
    public ResponseEntity<Success> toggleUserEnabled(@PathVariable String userId) {
        return userManagementService.toggleUserEnabled(userId).toResponseEntity();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Success> deleteUser(@PathVariable String userId) {
        return userManagementService.deleteUser(userId).toResponseEntity();
    }
}
