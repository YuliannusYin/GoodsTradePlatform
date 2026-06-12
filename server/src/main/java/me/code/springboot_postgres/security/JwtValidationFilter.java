/**
 * @file JwtValidationFilter.java
 * @description JWT令牌验证过滤器，在每次请求中提取并验证JWT令牌，设置安全上下文
 * @input HTTP请求和响应
 * @output 验证通过则设置安全上下文，验证失败则返回401错误响应
 */
package me.code.springboot_postgres.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JWT令牌验证过滤器
 * 职责：拦截每个HTTP请求，提取Authorization头中的JWT令牌并验证，设置Spring Security认证上下文
 */
public class JwtValidationFilter extends OncePerRequestFilter {

    // Authorization请求头名称
    private static final String AUTHORIZATION_HEADER = "Authorization";
    // Bearer令牌前缀
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserAccountService userAccountService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtValidationFilter(JwtTokenUtil jwtTokenUtil, UserAccountService userAccountService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userAccountService = userAccountService;
    }

    /**
     * 每次请求的过滤逻辑：提取令牌、验证令牌、设置认证上下文
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中提取JWT令牌
        String token = extractToken(request);

        // 无令牌则直接放行（由后续安全配置决定是否需要认证）
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 令牌无效则返回401错误
        if (!jwtTokenUtil.isValidToken(token)) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "The provided token is not valid");
            return;
        }

        try {
            // 令牌有效，设置安全认证上下文
            setAuthenticationContext(token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 认证上下文设置失败
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }

    /**
     * 从请求头中提取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌字符串，无令牌则返回null
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        // 无Authorization头则返回null
        if (header == null || header.isBlank()) {
            return null;
        }
        // 去除Bearer前缀提取令牌
        if (header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return header;
    }

    /**
     * 根据令牌信息设置Spring Security认证上下文
     * @param token 有效的JWT令牌
     */
    private void setAuthenticationContext(String token) {
        // 从令牌中获取用户ID
        String userId = jwtTokenUtil.getTokenId(token);
        // 加载用户信息
        User user = userAccountService.loadUserById(userId);
        // 构建认证令牌并设置到安全上下文
        var authToken = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * 发送JSON格式的错误响应
     * @param response HTTP响应
     * @param status HTTP状态码
     * @param message 错误消息
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> errorBody = Map.of(
                "error", true,
                "status", status.value(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );
        objectMapper.writeValue(response.getOutputStream(), errorBody);
    }
}
