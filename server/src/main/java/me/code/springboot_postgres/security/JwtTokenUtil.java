/**
 * @file JwtTokenUtil.java
 * @description JWT令牌工具类，负责令牌的生成、验证和声明提取
 * @input 用户实体、JWT密钥和过期时间配置
 * @output JWT令牌字符串、令牌验证结果、令牌中的声明数据
 */
package me.code.springboot_postgres.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.code.springboot_postgres.models.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT令牌工具类
 * 职责：提供JWT令牌的生成、验证和声明信息提取功能
 */
@Component
public class JwtTokenUtil {

    // 令牌中用户ID的声明键名
    private static final String CLAIM_ID = "id";
    // 令牌中用户角色的声明键名
    private static final String CLAIM_ROLE = "role";

    // HMAC签名密钥
    private final Key key;
    // 令牌过期时间（毫秒）
    private final long expirationMs;

    /**
     * 构造JWT工具类，从配置中读取密钥和过期时间
     * @param secret JWT密钥字符串
     * @param expirationMs 令牌过期时间（毫秒），默认1小时
     */
    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        // 密钥不能为空
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured via JWT_SECRET environment variable");
        }
        // 密钥长度至少32字符以保证安全性
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * 为指定用户生成JWT令牌
     * @param user 用户实体
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(User user) {
        Date now = new Date();
        // 计算过期时间
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(user.getId())
                .claim(CLAIM_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 验证令牌是否有效
     * @param token JWT令牌
     * @return 令牌是否有效
     */
    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException jwtException) {
            return false;
        }
    }

    /**
     * 从令牌中提取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public String getTokenId(String token) {
        return getTokenClaim(token, CLAIM_ID);
    }

    /**
     * 从令牌中提取用户角色
     * @param token JWT令牌
     * @return 角色名称
     */
    public String getTokenRole(String token) {
        return getTokenClaim(token, CLAIM_ROLE);
    }

    /**
     * 从令牌中提取指定声明值
     * @param token JWT令牌
     * @param claimName 声明键名
     * @return 声明值
     */
    private String getTokenClaim(String token, String claimName) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get(claimName, String.class);
    }
}
