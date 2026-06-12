/**
 * @file LoginController.java
 * @description 登录控制器，提供用户登录认证和JWT令牌生成的接口
 * @input 用户登录DTO（邮箱和密码）
 * @output 统一API响应包装的认证信息（角色列表和JWT令牌）
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.UserLoginDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.AuthenticationDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.security.JwtTokenUtil;
import me.code.springboot_postgres.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录控制器
 * 职责：处理用户登录请求，验证凭据并返回JWT令牌
 */
@RestController
@RequestMapping("api/account")
public class LoginController {

    private final AuthenticationProvider authenticationProvider;
    private final UserAccountService userAccountService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public LoginController(AuthenticationProvider authenticationProvider,
                           UserAccountService userAccountService,
                           JwtTokenUtil jwtTokenUtil) {
        this.authenticationProvider = authenticationProvider;
        this.userAccountService = userAccountService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 用户登录接口，验证凭据并返回JWT令牌
     * @param dto 登录请求数据（邮箱和密码）
     * @return 认证信息（角色列表和JWT令牌）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationDTO>> login(@RequestBody UserLoginDTO dto) {
        User user = authenticateUser(dto);
        // 生成JWT令牌
        String token = jwtTokenUtil.generateToken(user);
        // 构造认证响应数据
        AuthenticationDTO data = new AuthenticationDTO(List.of(user.getRole().name()), token);
        return ApiResponse.ok("Login successful", data).toResponseEntity();
    }

    /**
     * 认证用户凭据，验证邮箱和密码是否正确
     * @param dto 登录请求数据
     * @return 认证成功的用户实体
     * @throws CustomRuntimeException 认证失败时抛出
     */
    private User authenticateUser(UserLoginDTO dto) {
        User user;
        try {
            // 根据邮箱加载用户
            user = userAccountService.loadUserByEmail(dto.email());
        } catch (CustomRuntimeException e) {
            // 邮箱不存在，返回统一错误提示
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // 构建认证令牌
        var authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.password());
        Authentication result;
        try {
            // 执行Spring Security认证
            result = authenticationProvider.authenticate(authToken);
        } catch (BadCredentialsException e) {
            // 密码错误
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        } catch (InternalAuthenticationServiceException e) {
            // 内部认证服务异常
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        } catch (Exception e) {
            // 其他认证异常
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        // 认证结果未通过
        if (!result.isAuthenticated()) {
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return user;
    }
}
