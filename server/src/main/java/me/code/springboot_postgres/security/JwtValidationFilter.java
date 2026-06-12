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

public class JwtValidationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserAccountService userAccountService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtValidationFilter(JwtTokenUtil jwtTokenUtil, UserAccountService userAccountService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userAccountService = userAccountService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtTokenUtil.isValidToken(token)) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "The provided token is not valid");
            return;
        }

        try {
            setAuthenticationContext(token);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || header.isBlank()) {
            return null;
        }
        if (header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return header;
    }

    private void setAuthenticationContext(String token) {
        String userId = jwtTokenUtil.getTokenId(token);
        User user = userAccountService.loadUserById(userId);
        var authToken = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

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
