package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Role;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.RoleRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserManagementService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
    }

    public Success assignRoles(String userId, Set<String> roleIds) {
        User user = getUserById(userId);

        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "Cannot modify roles of a system account");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleId : roleIds) {
            Role role = roleRepository.findById(roleId).orElseThrow(
                    () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Role not found with id: " + roleId));
            roles.add(role);
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new Success(HttpStatus.OK, "Roles assigned successfully");
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
