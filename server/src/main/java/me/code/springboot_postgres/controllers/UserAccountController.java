/**
 * @file UserAccountController.java
 * @description 用户账户控制器，提供注册、获取账户详情、修改用户名/邮箱/密码、删除账户和验证凭据的接口
 * @input 认证用户信息、各类修改DTO、注册DTO
 * @output 统一API响应包装的用户详情或操作结果
 */
package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.*;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDetailsDTO;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户账户控制器
 * 职责：处理用户注册、账户信息查询、账户信息修改和账户删除等操作
 */
@RestController
@RequestMapping("api/account")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Autowired
    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * 用户注册接口
     * @param dto 注册请求数据
     * @return 操作结果
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody CreateUserDTO dto) {
        return userAccountService.submitRegistration(dto).toResponseEntity();
    }

    /**
     * 获取当前用户的账户详情
     * @param user 当前认证用户
     * @return 用户详情数据
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> getAccountDetails(@AuthenticationPrincipal User user) {
        return userAccountService.getUserDetails(user).toResponseEntity();
    }

    /**
     * 修改用户名
     * @param user 当前认证用户
     * @param dto 修改用户名请求数据
     * @return 操作结果
     */
    @PutMapping("/username")
    public ResponseEntity<ApiResponse<Void>> changeUsername(@AuthenticationPrincipal User user, @RequestBody ChangeUsernameDTO dto) {
        return userAccountService.changeUsername(user, dto).toResponseEntity();
    }

    /**
     * 修改邮箱
     * @param user 当前认证用户
     * @param dto 修改邮箱请求数据
     * @return 操作结果
     */
    @PutMapping("/email")
    public ResponseEntity<ApiResponse<Void>> changeEmail(@AuthenticationPrincipal User user, @RequestBody ChangeEmailDTO dto) {
        return userAccountService.changeEmail(user, dto).toResponseEntity();
    }

    /**
     * 修改密码
     * @param user 当前认证用户
     * @param dto 修改密码请求数据
     * @return 操作结果
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal User user, @RequestBody ChangePasswordDTO dto) {
        return userAccountService.changePassword(user, dto).toResponseEntity();
    }

    /**
     * 删除当前用户账户
     * @param user 当前认证用户
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        return userAccountService.deleteAccount(user).toResponseEntity();
    }

    /**
     * 验证用户凭据是否有效
     * @param dto 登录请求数据
     * @return 凭据是否有效
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Boolean>> isValidCredentials(@RequestBody UserLoginDTO dto) {
        boolean valid = userAccountService.isValidUserCredentials(dto.email(), dto.password());
        return ApiResponse.ok("Credentials validated", valid).toResponseEntity();
    }
}
