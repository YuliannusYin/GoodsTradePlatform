/**
 * @file UserDTO.java
 * @description 用户响应数据传输对象，返回用户的完整信息
 * @input 无（由实体转换构造）
 * @output 用户ID、邮箱、用户名、角色、头像、简介、余额、保护状态和启用状态
 */
package me.code.springboot_postgres.dtos.responses;

import me.code.springboot_postgres.models.entities.User;
import java.math.BigDecimal;

/**
 * 用户响应DTO
 * 职责：封装返回给前端的用户完整信息
 */
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
    /**
     * 从用户实体转换为用户DTO
     * @param user 用户实体
     * @return 用户DTO对象
     */
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
