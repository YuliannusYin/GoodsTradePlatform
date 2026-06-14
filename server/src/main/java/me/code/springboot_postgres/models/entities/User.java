/**
 * @file User.java
 * @description 用户实体类，表示系统中的用户，实现Spring Security的UserDetails接口
 * @input 邮箱、用户名、密码、角色等用户信息
 * @output 持久化的用户记录，可用于Spring Security认证
 */
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 用户实体
 * 职责：映射用户数据并实现Spring Security认证接口，支持角色权限和账户状态管理
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    // 用户唯一标识
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    // 用户邮箱（唯一）
    @Column(nullable = false, unique = true)
    private String email;

    // 用户名（唯一）
    @Column(nullable = false, unique = true)
    private String username;

    // 加密后的密码
    @Column(nullable = false)
    private String password;

    // 用户角色，默认为普通用户
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    // 头像URL
    @Column(length = 500)
    private String avatarUrl;

    // 个人简介
    @Column(length = 500)
    private String bio;

    // 账户余额，默认为零
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // 是否为受保护的系统账号（不可修改、删除）
    @Column(name = "is_protected", nullable = false)
    private boolean isProtected = false;

    // 是否启用，默认启用
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    // 乐观锁版本号
    @Version
    private int version;

    /**
     * 构造用户对象（基本四项）
     * @param email 邮箱
     * @param username 用户名
     * @param password 加密密码
     * @param role 角色
     */
    public User(String email, String username, String password, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * 构造用户对象（含余额和保护状态）
     * @param email 邮箱
     * @param username 用户名
     * @param password 加密密码
     * @param role 角色
     * @param balance 余额
     * @param isProtected 是否受保护
     */
    public User(String email, String username, String password, Role role, BigDecimal balance, boolean isProtected) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.balance = balance;
        this.isProtected = isProtected;
    }

    /**
     * 获取用户权限列表
     * @return 角色对应的权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // 账户未过期，始终返回true
    @Override
    public boolean isAccountNonExpired() { return true; }

    // 账户未锁定，与启用状态一致
    @Override
    public boolean isAccountNonLocked() { return isEnabled; }

    // 凭据未过期，始终返回true
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // 账户是否启用
    @Override
    public boolean isEnabled() { return isEnabled; }

    /**
     * 用户信息字符串表示，用于日志和调试
     * @return 包含id、邮箱、用户名和角色的字符串
     */
    @Override
    public String toString() {
        return "User{id='" + id + "', email='" + email + "', username='" + username + "', role=" + role + "}";
    }

    /**
     * 用户角色枚举
     */
    public enum Role {
        USER, MERCHANT, ADMIN, SUPER_ADMIN;

        /**
         * 返回角色名称字符串
         * @return 角色枚举的name值
         */
        @Override
        public String toString() { return this.name(); }
    }
}
