/**
 * @file UserAccountService.java
 * @description 用户账户服务类，提供注册、查询详情、修改信息、删除账户和用户加载的业务逻辑
 * @input 用户实体、各类修改DTO、注册DTO、邮箱、用户名
 * @output 操作结果、用户详情DTO或用户实体
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.ChangeEmailDTO;
import me.code.springboot_postgres.dtos.requests.ChangePasswordDTO;
import me.code.springboot_postgres.dtos.requests.ChangeUsernameDTO;
import me.code.springboot_postgres.dtos.requests.CreateUserDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDetailsDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户账户服务类
 * 职责：处理用户注册、账户信息查询与修改、账户删除，以及Spring Security用户加载
 */
@Service
public class UserAccountService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 提交用户注册
     * @param dto 注册请求数据
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> submitRegistration(CreateUserDTO dto) {
        // 检查邮箱和用户名是否唯一
        checkUniqueValues(dto.email(), dto.username());
        // 加密密码后创建用户
        String encryptedPassword = passwordEncoder.encode(dto.password());
        User newUser = new User(dto.email(), dto.username(), encryptedPassword, User.Role.USER);
        userRepository.save(newUser);
        return ApiResponse.created("Successfully registered a new account", null);
    }

    /**
     * 检查邮箱和用户名是否已被占用
     * @param email 邮箱
     * @param username 用户名
     */
    private void checkUniqueValues(String email, String username) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen username already exists");
        }
    }

    /**
     * 获取当前用户的账户详情
     * 重新从数据库加载用户数据，避免脱管实体返回过时信息（如余额已被扣减但未更新）
     * @param user 当前用户（脱管实体，仅使用其ID）
     * @return 用户详情DTO
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public ApiResponse<UserDetailsDTO> getUserDetails(User user) {
        // 重新加载托管实体，确保数据是最新的
        User managedUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "用户不存在"));
        UserDetailsDTO dto = new UserDetailsDTO(
                managedUser.getEmail(), managedUser.getUsername(), managedUser.getBalance(), managedUser.isProtected(), managedUser.getRole().name());
        return ApiResponse.ok("User details were successfully retrieved", dto);
    }

    /**
     * 修改用户名
     * 重新从数据库加载托管用户实体，避免脱管实体 save() 导致乐观锁冲突或数据覆盖
     * @param user 当前用户（脱管实体，仅使用其ID）
     * @param dto 修改用户名请求数据
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> changeUsername(User user, ChangeUsernameDTO dto) {
        // 受保护账号不可修改
        checkNotProtected(user, "change username");
        // 检查新用户名是否已被占用
        if (userRepository.existsByUsername(dto.newUsername())) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen username already exists");
        }
        // 重新加载托管实体，避免脱管实体的过时 version 字段导致乐观锁冲突
        User managedUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "用户不存在"));
        managedUser.setUsername(dto.newUsername());
        userRepository.save(managedUser);
        return ApiResponse.ok("The username was successfully changed");
    }

    /**
     * 修改邮箱
     * 重新从数据库加载托管用户实体，避免脱管实体 save() 导致乐观锁冲突或数据覆盖
     * @param user 当前用户（脱管实体，仅使用其ID）
     * @param dto 修改邮箱请求数据
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> changeEmail(User user, ChangeEmailDTO dto) {
        checkNotProtected(user, "change email");
        // 检查新邮箱是否已被占用
        if (userRepository.existsByEmail(dto.newEmail())) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen email already exists");
        }
        // 重新加载托管实体，避免脱管实体的过时 version 字段导致乐观锁冲突
        User managedUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "用户不存在"));
        managedUser.setEmail(dto.newEmail());
        userRepository.save(managedUser);
        return ApiResponse.ok("The email was successfully changed");
    }

    /**
     * 修改密码
     * 重新从数据库加载托管用户实体，避免脱管实体 save() 导致乐观锁冲突或数据覆盖
     * @param user 当前用户（脱管实体，仅使用其ID）
     * @param dto 修改密码请求数据
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> changePassword(User user, ChangePasswordDTO dto) {
        checkNotProtected(user, "change password");
        // 重新加载托管实体，避免脱管实体的过时 version 字段导致乐观锁冲突
        User managedUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "用户不存在"));
        // 验证当前密码是否正确
        if (!passwordEncoder.matches(dto.currentPassword(), managedUser.getPassword())) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        managedUser.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(managedUser);
        return ApiResponse.ok("The password was successfully changed");
    }

    /**
     * 删除用户账户
     * 注意：此方法仅删除用户记录，不清理关联的购物车项、收藏和评价（与管理员删除不同）
     * @param user 当前用户
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> deleteAccount(User user) {
        checkNotProtected(user, "delete account");
        userRepository.deleteById(user.getId());
        return ApiResponse.ok("The account was successfully deleted");
    }

    /**
     * 验证用户凭据是否有效
     * @param email 邮箱
     * @param password 密码
     * @return 凭据是否有效
     */
    @Transactional(readOnly = true)
    public boolean isValidUserCredentials(String email, String password) {
        User user = loadUserByEmail(email);
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 根据用户ID加载用户
     * @param userId 用户ID
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public User loadUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with id: " + userId));
    }

    /**
     * 根据邮箱加载用户
     * @param email 邮箱
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with email: " + email));
    }

    /**
     * Spring Security接口实现：根据用户名加载用户
     * @param username 用户名
     * @return 用户实体
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Could not find user with username: " + username));
    }

    /**
     * 检查用户是否为受保护的系统账号
     * @param user 用户
     * @param action 尝试执行的操作描述
     */
    private void checkNotProtected(User user, String action) {
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "This is a system account. You cannot " + action + ".");
        }
    }
}
