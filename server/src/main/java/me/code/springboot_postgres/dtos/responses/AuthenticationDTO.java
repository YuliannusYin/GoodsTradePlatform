/**
 * @file AuthenticationDTO.java
 * @description 认证响应数据传输对象，返回登录成功后的角色和令牌
 * @input 无（由服务层构造）
 * @output 用户角色列表和JWT令牌
 */
package me.code.springboot_postgres.dtos.responses;

import java.util.List;

/**
 * 认证响应DTO
 * 职责：封装登录成功后返回的用户角色列表和JWT令牌
 */
public record AuthenticationDTO(
    List<String> userRoles,
    String token
) {}
