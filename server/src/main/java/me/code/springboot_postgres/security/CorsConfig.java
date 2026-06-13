/**
 * @file CorsConfig.java
 * @description 跨域资源共享（CORS）配置类，配置允许的来源、方法和头部
 * @input 配置文件中的允许来源列表
 * @output CORS配置Bean和WebMvc配置Bean
 */
package me.code.springboot_postgres.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS跨域配置类
 * 职责：配置Spring Security和Spring MVC层面的跨域访问策略
 */
@Configuration
public class CorsConfig {

    // 从配置文件读取允许的来源列表，默认为localhost
    @Value("${cors.allowed-origins:http://localhost,http://localhost:5173}")
    private String allowedOrigins;

    /**
     * 创建Spring Security使用的CORS配置源
     * @return CORS配置源Bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 设置允许的来源模式，split后trim去除空格，避免逗号后带空格导致匹配失败
        config.setAllowedOriginPatterns(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toList()
        );
        // 设置允许的HTTP方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 允许所有请求头
        config.setAllowedHeaders(List.of("*"));
        // 允许携带凭据（Cookie等）
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 创建Spring MVC使用的CORS配置
     * @return WebMvcConfigurer Bean
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                Arrays.stream(allowedOrigins.split(","))
                                        .map(String::trim)
                                        .toArray(String[]::new)
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
