/**
 * @file SecurityConfig.java
 * @description Spring Security安全配置类，定义URL访问权限、认证提供者和密码编码器
 * @input HttpSecurity、UserAccountService、JwtTokenUtil
 * @output 安全过滤链、认证提供者和密码编码器Bean
 */
package me.code.springboot_postgres.security;

import me.code.springboot_postgres.services.UserAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.http.HttpMethod;

/**
 * Spring Security安全配置类
 * 职责：配置URL访问权限规则、JWT过滤器、认证入口和异常处理器
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // API路径常量
    private static final String API_PATH = "/api";
    private static final String ACCOUNT_PATH = API_PATH + "/account";
    private static final String PRODUCTS_PATH = API_PATH + "/products";
    private static final String ORDERS_PATH = API_PATH + "/orders";
    private static final String REVIEWS_PATH = API_PATH + "/reviews";

    // 无需认证即可访问的公开URL
    private static final String[] PUBLIC_URLS = {
            ACCOUNT_PATH + "/register",
            ACCOUNT_PATH + "/login",
            PRODUCTS_PATH + "/all",
            PRODUCTS_PATH + "/featured",
            PRODUCTS_PATH + "/{productId}",
            PRODUCTS_PATH + "/search/**",
            PRODUCTS_PATH + "/category/**",
            PRODUCTS_PATH + "/categories",
            PRODUCTS_PATH + "/conditions",
            ORDERS_PATH + "/ongoing",
            ORDERS_PATH + "/delivery/methods",
            ORDERS_PATH + "/payment/methods",
            REVIEWS_PATH + "/product/**",
            REVIEWS_PATH + "/product/*/rating",
    };

    // 仅超级管理员可访问的URL
    private static final String[] SUPER_ADMIN_URLS = {
            API_PATH + "/admin/users/**"
    };

    // 超级管理员和管理员可访问的URL
    private static final String[] ADMIN_URLS = {
            API_PATH + "/admin_tools/**"
    };

    // 商户、管理员和超级管理员可访问的URL
    private static final String[] MERCHANT_URLS = {
            API_PATH + "/user_products/**"
    };

    /**
     * 配置安全过滤链，定义CORS、CSRF、JWT过滤器和URL权限规则
     * @param security HttpSecurity对象
     * @param userAccountService 用户账户服务
     * @param jwtTokenUtil JWT工具类
     * @return 构建完成的安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security, UserAccountService userAccountService, JwtTokenUtil jwtTokenUtil) throws Exception {
        security.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                // 在用户名密码认证过滤器之后添加JWT验证过滤器
                .addFilterAfter(new JwtValidationFilter(jwtTokenUtil, userAccountService), UsernamePasswordAuthenticationFilter.class)
                // 配置认证入口和访问拒绝处理器
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"success\":false,\"status\":401,\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"success\":false,\"status\":403,\"message\":\"Access denied\"}");
                        })
                )
                // 配置URL访问权限
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // 佣金配置修改接口仅超级管理员可访问
                        .requestMatchers(HttpMethod.PUT, API_PATH + "/admin_tools/commission").hasRole("SUPER_ADMIN")
                        .requestMatchers(SUPER_ADMIN_URLS).hasRole("SUPER_ADMIN")
                        .requestMatchers(ADMIN_URLS).hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(MERCHANT_URLS).hasAnyRole("MERCHANT", "SUPER_ADMIN", "ADMIN")
                        .anyRequest().authenticated());
        return security.build();
    }

    /**
     * 创建DAO认证提供者，使用自定义的用户详情服务和密码编码器
     * @param userAccountService 用户账户服务
     * @param encoder 密码编码器
     * @return 认证提供者Bean
     */
    @Bean
    public AuthenticationProvider authProvider(UserAccountService userAccountService, PasswordEncoder encoder) {
        var dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(userAccountService);
        dao.setPasswordEncoder(encoder);
        return dao;
    }

    /**
     * 创建委托式密码编码器，支持多种编码格式（如bcrypt、sha256等）
     * @return 密码编码器Bean
     */
    @Bean
    public PasswordEncoder encoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
