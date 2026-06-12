/**
 * @file DataInitializer.java
 * @description 应用启动时自动初始化内置用户数据，确保超级管理员、商户和测试用户存在且字段正确
 * @input 无（通过构造注入获取仓库和编码器）
 * @output 数据库中确保存在的内置用户记录
 */
package me.code.springboot_postgres.config;

import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 数据初始化器
 * 职责：在应用启动时自动检查并创建内置用户（超级管理员、商户、测试用户），并修复异常数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // 超级管理员账号配置
    private static final String SUPER_ADMIN_EMAIL = "admin@merchandise.com";
    private static final String SUPER_ADMIN_PASSWORD = "Admin@2024";

    // 商户账号配置
    private static final String MERCHANT_EMAIL = "merchant@merchandise.com";
    private static final String MERCHANT_PASSWORD = "Merchant@2024";

    // 测试用户账号配置
    private static final String TEST_USER_EMAIL = "testuser@merchandise.com";
    private static final String TEST_USER_PASSWORD = "Test@2024";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 应用启动时自动执行，确保三个内置用户存在且数据正确
     * @param args 启动参数
     */
    @Override
    public void run(String... args) {
        // 确保超级管理员用户存在
        ensureUser(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD, User.Role.SUPER_ADMIN, BigDecimal.ZERO, true);
        // 确保商户用户存在
        ensureUser(MERCHANT_EMAIL, MERCHANT_PASSWORD, User.Role.MERCHANT, BigDecimal.ZERO, true);
        // 确保测试用户存在，初始余额为一千万
        ensureUser(TEST_USER_EMAIL, TEST_USER_PASSWORD, User.Role.USER, new BigDecimal("10000000.00"), true);
    }

    /**
     * 确保指定用户存在，若不存在则创建，若存在则修复密码和校验字段
     * @param email 用户邮箱
     * @param rawPassword 明文密码
     * @param role 用户角色
     * @param balance 账户余额
     * @param isProtected 是否为受保护的系统账号
     */
    private void ensureUser(String email, String rawPassword, User.Role role, BigDecimal balance, boolean isProtected) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isEmpty()) {
            // 用户不存在，创建新用户
            String encoded = passwordEncoder.encode(rawPassword);
            // 使用邮箱@前缀作为用户名
            User user = new User(email, email.split("@")[0], encoded, role, balance, isProtected);
            userRepository.save(user);
            log.info("Created built-in user: {} ({})", email, role);
            return;
        }

        // 用户已存在，修复密码和校验字段
        User user = existing.get();
        fixPlaceholderPassword(user, rawPassword);
        ensureFields(user, role, balance, isProtected);
    }

    /**
     * 修复占位符密码或损坏的密码哈希
     * @param user 用户实体
     * @param rawPassword 正确的明文密码
     */
    private void fixPlaceholderPassword(User user, String rawPassword) {
        String stored = user.getPassword();
        // 检查是否为占位符密码
        if (stored.contains("placeholder")) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            log.info("Fixed placeholder password for: {}", user.getEmail());
            return;
        }
        try {
            // 尝试验证密码格式是否合法
            passwordEncoder.matches(rawPassword, stored);
        } catch (IllegalArgumentException e) {
            // 密码哈希损坏，重新编码保存
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            log.warn("Fixed corrupted password hash for: {}", user.getEmail());
        }
    }

    /**
     * 确保内置用户的关键字段与预期一致，不一致则更新
     * @param user 用户实体
     * @param role 期望的角色
     * @param balance 期望的余额
     * @param isProtected 期望的保护状态
     */
    private void ensureFields(User user, User.Role role, BigDecimal balance, boolean isProtected) {
        boolean needsUpdate = false;

        // 角色不一致则修正
        if (user.getRole() != role) {
            user.setRole(role);
            needsUpdate = true;
        }
        // 保护状态不一致则修正
        if (user.isProtected() != isProtected) {
            user.setProtected(isProtected);
            needsUpdate = true;
        }
        // 余额大于零且不一致则修正
        if (balance.compareTo(BigDecimal.ZERO) > 0 && user.getBalance().compareTo(balance) != 0) {
            user.setBalance(balance);
            needsUpdate = true;
        }

        // 有字段需要更新时才执行保存
        if (needsUpdate) {
            userRepository.save(user);
            log.info("Updated built-in user fields: {}", user.getEmail());
        }
    }
}
