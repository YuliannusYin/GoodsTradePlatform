package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.User;
import java.math.BigDecimal;

public record UserDTO(
    String id,
    String email,
    String username,
    String role,
    String avatarUrl,
    String bio,
    BigDecimal balance,
    boolean isProtected,
    boolean isEnabled
) {
    public static UserDTO from(User user) {
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getRole().name(),
            user.getAvatarUrl(),
            user.getBio(),
            user.getBalance(),
            user.isProtected(),
            user.isEnabled()
        );
    }
}
