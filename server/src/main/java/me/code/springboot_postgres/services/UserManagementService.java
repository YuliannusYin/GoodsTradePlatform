package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository userRepository;

    @Autowired
    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
    }

    public Success assignRole(String userId, String roleName) {
        User user = getUserById(userId);
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot modify roles of a system account");
        }
        try {
            user.setRole(User.Role.valueOf(roleName.toUpperCase()));
            userRepository.save(user);
            return new Success(HttpStatus.OK, "Role assigned successfully");
        } catch (IllegalArgumentException e) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }
    }

    public Success toggleUserEnabled(String userId) {
        User user = getUserById(userId);
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot disable a system account");
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        String status = user.isEnabled() ? "enabled" : "disabled";
        return new Success(HttpStatus.OK, "User " + status + " successfully");
    }

    public Success deleteUser(String userId) {
        User user = getUserById(userId);
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot delete a system account");
        }
        userRepository.delete(user);
        return new Success(HttpStatus.OK, "User deleted successfully");
    }
}
