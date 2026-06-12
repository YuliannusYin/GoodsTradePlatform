package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository userRepository;

    @Autowired
    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(String userId) {
        return UserDTO.from(userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId)));
    }

    @Transactional
    public ApiResponse<Void> assignRole(String userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot modify roles of a system account");
        }
        try {
            user.setRole(User.Role.valueOf(roleName.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }
        userRepository.save(user);
        return ApiResponse.ok("Role assigned successfully");
    }

    @Transactional
    public ApiResponse<Void> toggleUserEnabled(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot disable a system account");
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ApiResponse.ok("User " + (user.isEnabled() ? "enabled" : "disabled") + " successfully");
    }

    @Transactional
    public ApiResponse<Void> deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot delete a system account");
        }
        userRepository.delete(user);
        return ApiResponse.ok("User deleted successfully");
    }
}
