/**
 * @file DataInitializer.java
 * @description 应用启动时自动初始化内置用户和测试商品数据，确保超级管理员、商户、测试用户存在且字段正确，并插入测试商品
 * @input 无（通过构造注入获取仓库和编码器）
 * @output 数据库中确保存在的内置用户记录和测试商品记录
 */
package me.code.springboot_postgres.config;

import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.ProductRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 数据初始化器
 * 职责：在应用启动时自动检查并创建内置用户（超级管理员、商户、测试用户）和测试商品，并修复异常数据
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
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 应用启动时自动执行，确保内置用户和测试商品存在且数据正确
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

        // 确保测试商品存在
        ensureTestProducts();
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

    /**
     * 确保测试商品存在，若数据库中无已审核通过的商品则插入测试商品数据
     * 测试商品以商户账号为卖家，状态为已审核通过，来源为平台商品
     */
    private void ensureTestProducts() {
        // 检查是否已有已审核通过的商品，有则跳过初始化
        List<Product> existingApproved = productRepository.findByStatus(Product.Status.APPROVED);
        if (!existingApproved.isEmpty()) {
            log.info("Test products already exist ({} approved products found), skipping initialization", existingApproved.size());
            return;
        }

        // 获取商户用户作为测试商品的卖家
        Optional<User> merchantOpt = userRepository.findByEmail(MERCHANT_EMAIL);
        if (merchantOpt.isEmpty()) {
            log.warn("Merchant user not found, cannot create test products");
            return;
        }
        User merchant = merchantOpt.get();

        // 创建测试商品列表，覆盖各主要分类
        List<Product> testProducts = List.of(
                new Product("初音未来 手办 Ver.1", "经典初音未来1/8比例手办，高品质PVC材质，精细涂装", List.of(), new BigDecimal("299.00"), 50, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                new Product("蕾姆 手办 和服Ver.", "Re:Zero 蕾姆和服版1/7比例手办，优雅和服造型", List.of(), new BigDecimal("459.00"), 30, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                new Product("咒术回战 五条悟 海报", "咒术回战五条悟官方授权海报，A2尺寸，高清印刷", List.of(), new BigDecimal("39.90"), 200, Product.Category.POSTER, Product.Condition.NEW, "PLATFORM"),
                new Product("进击的巨人 调查兵团钥匙扣", "进击的巨人调查兵团徽章造型钥匙扣，金属材质", List.of(), new BigDecimal("29.90"), 150, Product.Category.KEYCHAIN, Product.Condition.NEW, "PLATFORM"),
                new Product("动漫角色徽章套装", "精选热门动漫角色徽章6枚套装，金属烤漆工艺", List.of(), new BigDecimal("49.90"), 100, Product.Category.BADGE, Product.Condition.NEW, "PLATFORM"),
                new Product("动漫角色抱枕 二次元周边", "高品质二次元角色抱枕，亲肤面料，精美印花", List.of(), new BigDecimal("89.00"), 80, Product.Category.PILLOW, Product.Condition.NEW, "PLATFORM"),
                new Product("鬼灭之炭治郎 亚克力立牌", "鬼灭之刃炭治郎全呼吸亚克力立牌，高清双面印刷", List.of(), new BigDecimal("35.00"), 120, Product.Category.STAND, Product.Condition.NEW, "PLATFORM"),
                new Product("动漫联名T恤 限定款", "热门动漫联名限定款T恤，纯棉面料，多尺码可选", List.of(), new BigDecimal("129.00"), 60, Product.Category.CLOTHING, Product.Condition.NEW, "PLATFORM"),
                new Product("动漫原声专辑 精选集", "经典动漫原声音乐精选专辑，CD+数字版双格式", List.of(), new BigDecimal("79.00"), 40, Product.Category.ALBUM, Product.Condition.LIKE_NEW, "PLATFORM"),
                new Product("动漫周边配件套装", "包含手机壳、挂绳、贴纸的动漫主题配件套装", List.of(), new BigDecimal("59.90"), 90, Product.Category.ACCESSORY, Product.Condition.NEW, "PLATFORM"),
                new Product("二手手办 良好状态", "九成新动漫手办，轻微展示痕迹，功能完好", List.of(), new BigDecimal("159.00"), 15, Product.Category.ANIME_FIGURE, Product.Condition.GOOD, "PLATFORM"),
                new Product("一般成色动漫海报", "有轻微折痕的动漫海报，内容清晰完整", List.of(), new BigDecimal("15.00"), 25, Product.Category.POSTER, Product.Condition.FAIR, "PLATFORM")
        );

        // 逐个保存测试商品并设置卖家和审核状态
        for (Product product : testProducts) {
            product.setSeller(merchant);
            product.setStatus(Product.Status.APPROVED);
            productRepository.save(product);
        }

        log.info("Created {} test products with seller: {}", testProducts.size(), MERCHANT_EMAIL);
    }
}
