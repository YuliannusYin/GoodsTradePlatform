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
     * 同时修复已有商品中图片URL为空的数据
     * 测试商品以商户账号为卖家，状态为已审核通过，来源为平台商品
     */
    private void ensureTestProducts() {
        // 检查是否已有已审核通过的商品
        List<Product> existingApproved = productRepository.findByStatus(Product.Status.APPROVED);

        if (!existingApproved.isEmpty()) {
            // 修复已有商品中图片URL为空的数据
            fixEmptyImageUrls(existingApproved);
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

        // 创建测试商品列表，覆盖各主要分类，每个商品配有真实图片
        List<Product> testProducts = List.of(
                // 手办（5个）
                new Product("初音未來 深海少女ver.", "一位幸福少女的故事。出自niconico動畫上的百萬播放人氣曲、由ゆうゆP製作的「深海少女」，「初音未來」1/8比例模型決定再次販售。以はるよ特別繪製的百萬播放數＆EXIT TUNES PRESENTS vocalodream收錄紀念插圖為基礎設計立體化，在深海底下祈禱的初音未來那白皙剔透的肌膚、漆黑裙裝、在水中飄散的秀髮與珊瑚礁等等，盡情欣賞「深海少女」的世界觀吧！", List.of("https://www.goodsmile.com/gsc-webrevo-sdk-storage-prd/product/image/product/20130529/3939/24293/large/d6e3ea77521a4f677438f3413a90439d.jpg"), new BigDecimal("16297.00"), 21, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                new Product("初音未來 米山舞ver.", "「初音未來」——創作的繆思女神。出自《Character Vocal系列01 初音未來》，由繪師米山舞所繪製的「初音未來」化身為1/7比例模型。女神般穿著的初音未來，背景中結合植物等有機圖案，保持插畫的氛圍完整且細緻地製成模型。像是翅膀一樣點綴整個空間的雙馬尾使用透明零件；純白的服裝分別使用珠光塗裝表現出優雅的效果。敬請將充滿慈愛表情且露出微笑的「初音未來」帶回家鑑賞吧！", List.of("https://www.goodsmile.com/gsc-webrevo-sdk-storage-prd/product/image/60248/rUfbpVcekh19w38XMJCiKWZqLDP40tTH.jpg"), new BigDecimal("32800.00"), 13, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                new Product("阿尔及利亚 轻装Ver.", "出自游戏《碧蓝航线》。品牌：ALTER，型号：AL20714，PVC&ABS涂装完成品，1/7比例，全高约250mm，适用年龄18岁以上", List.of("https://www.alter-shanghai.cn/files/images/az_algerie_k_all1.jpg"), new BigDecimal("1599.00"), 217, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                new Product("鸣潮 弗洛洛 手办", "鸣潮动画漫手办雕像模型摆件玩具gk游戏兵人影视弗洛洛美女爆甲。作品名：鸣潮，材质：树脂，比例：1:5，适用年龄：14岁以上", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/444019/29/10054/127176/6a1d348cFad8cc42d/0083438414f1a160.jpg.avif"), new BigDecimal("1140.50"), 586, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                new Product("星铁 卡芙卡 手办", "APEXINNOVATION【APEX/米哈游】崩坏:星穹铁道 卡芙卡1/7手办 周边 miHoYo 现货。品牌：APEXINNOVATION，材质：PVC(聚氯乙烯)，款式：静态", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/423320/9/15914/101694/69eec624Facc96cb2/0083320320818a3b.jpg.avif"), new BigDecimal("838.99"), 79, Product.Category.ANIME_FIGURE, Product.Condition.NEW, "PLATFORM"),
                // 海报（3个）
                new Product("日漫newtype动漫杂志海报", "二次元卧室房间宿舍装饰墙贴壁纸海报。newtype[全款A+B/18张]，尺寸:A4，纸质:铜版纸下单送二次元贴纸", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/276025/13/16166/367845/67f12d93F8e35ffa7/338d253f25fcb0aa.jpg.avif"), new BigDecimal("22.59"), 19527, Product.Category.POSTER, Product.Condition.NEW, "PLATFORM"),
                new Product("久耀 爱弥斯周边鸣潮挂画海报", "动漫游戏二次元美少女卡通客厅装饰画壁画。爱弥斯32 105x70cm油画布挂墙", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/414464/16/4154/102249/69d0e466F5b147f47/008332032060cb34.jpg.avif"), new BigDecimal("117.55"), 2371, Product.Category.POSTER, Product.Condition.NEW, "PLATFORM"),
                new Product("捷霸（JBA）达妮娅挂画海报", "鸣潮动漫游戏周边房间卧室装饰画卷轴可定制礼物。图案9 80*120厘米挂画【超大幅】", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/447180/17/11254/101515/6a28c76eF3ebb9f97/008332032008342e.jpg.avif"), new BigDecimal("98.00"), 2113, Product.Category.POSTER, Product.Condition.NEW, "PLATFORM"),
                // 钥匙扣（3个）
                new Product("纳西妲周边亚克力钥匙扣", "草神卡通动漫周边书包挂件二次元物。【超值】全套3款，亚克力材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/393727/24/3497/369845/697daf70F02d893ea/093532032046bed8.png.avif"), new BigDecimal("6.30"), 13997, Product.Category.KEYCHAIN, Product.Condition.NEW, "PLATFORM"),
                new Product("原神周边钥匙扣 水神芙卡洛斯芙宁娜闲云", "游戏同人亚克力书包挂件。哥伦比娅4 6cm（双面覆膜，记得撕掉）-3，亚克力材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/307794/40/20695/108304/68d6654eF5781bb63/c616afb5ec633847.jpg.avif"), new BigDecimal("11.80"), 9823, Product.Category.KEYCHAIN, Product.Condition.NEW, "PLATFORM"),
                new Product("小林家的龙女仆 钥匙扣", "Tohru款，动漫周边钥匙扣", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/158424/18/43952/89766/6607b964Fee90105a/b597e5d71fbd5cb7.jpg.avif"), new BigDecimal("20.00"), 1723, Product.Category.KEYCHAIN, Product.Condition.NEW, "PLATFORM"),
                // 徽章（5个）
                new Product("映棠鬼灭之刃日漫提剑姿态吧唧", "二次元周边吧唧，学生礼物马口铁徽章。喜欢全部16个打包，鬼灭第五弹58mm，金属材质", List.of("https://img13.360buyimg.com/pcpubliccms/jfs/t1/422732/26/17186/293567/69f2d25cF5826fb27/00833203201ef482.jpg.avif"), new BigDecimal("28.90"), 571, Product.Category.BADGE, Product.Condition.NEW, "PLATFORM"),
                new Product("皓昔 夏洛特charlotte吧唧徽章", "友利奈绪周边 乙坂有宇动漫镭射谷子。六 五件任选(款式号发客服备注)，镭射满天星，PVC(聚氯乙烯)材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/421397/26/7788/241524/69e3710bF6b454033/0083320320dc6505.png.avif"), new BigDecimal("63.84"), 371, Product.Category.BADGE, Product.Condition.NEW, "PLATFORM"),
                new Product("带宝乐罗小黑战记周边吧唧", "鹿野吧唧徽章谷礼物卡通动漫周边可爱学生。全部六款【各1个】A 仅以上款式", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/315711/10/21082/286109/6889b918F377684ce/fd3dfd2083f185a6.jpg.avif"), new BigDecimal("20.90"), 677, Product.Category.BADGE, Product.Condition.NEW, "PLATFORM"),
                new Product("异环游戏新款徽章", "薄荷发帝娅周边75mm双闪大徽章谷子吧唧。一套6款送纸袋（颜色随机），75mm双闪大徽章（独立包装）", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/435374/33/2015/182812/6a01b8e9F7b0f56fb/00833203203297c5.jpg.avif"), new BigDecimal("34.30"), 427, Product.Category.BADGE, Product.Condition.NEW, "PLATFORM"),
                new Product("【官谷正版】JOJO的奇妙冒险动漫周边徽章", "吧唧胸针马口铁礼物。JOJO的奇妙冒险【套装十个】75mm双闪大徽章，金属材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/412809/27/10755/715941/69d11b47F06030a57/00834b04b0cd800e.jpg.avif"), new BigDecimal("49.00"), 313, Product.Category.BADGE, Product.Condition.NEW, "PLATFORM"),
                // 抱枕（3个）
                new Product("咕咕嘎嘎企鹅管理员毛绒公仔抱枕", "明日方舟游戏周边送男生。咕嘎企鹅25厘米190克，OPP袋独立包装。面料：绒类，填充材质：聚酯纤维", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/452753/3/9362/64058/6a2be003Fa3ff1bef/00833203203dcfa4.jpg.avif"), new BigDecimal("72.00"), 197, Product.Category.PILLOW, Product.Condition.NEW, "PLATFORM"),
                new Product("可爱途 菲比啾比鸣潮游戏周边", "卡通动漫周边萌萌礼物Q版菲比抱枕自制版。坐姿菲比 50cm，绒类材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/441443/21/3749/2377357/6a155061F1bcf5a48/00834e64e6759abd.png.avif"), new BigDecimal("37.00"), 413, Product.Category.PILLOW, Product.Condition.NEW, "PLATFORM"),
                new Product("崩坏3德莉莎月下誓约予等身抱枕", "二次元动漫长条枕床上靠枕定制。正常款 50*160cm枕套（桃皮绒），使用年龄：18+", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/195359/30/50311/258038/671fabe2F7865281b/72c260d5b516f695.jpg.avif"), new BigDecimal("144.79"), 97, Product.Category.PILLOW, Product.Condition.NEW, "PLATFORM"),
                // 立牌（5个）
                new Product("卡游（Kayou）初音未来立牌乐章版第2弹", "正版初音未来周边谷子亚克力立牌摆件。【第1弹】乐章版-4包，亚克力材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/431001/16/21038/327292/6a0d71afF82ea6721/0083320320a09106.jpg.avif"), new BigDecimal("169.90"), 19, Product.Category.STAND, Product.Condition.NEW, "PLATFORM"),
                new Product("遥绾惜百合园圣娅胸针吧唧立牌卡砖", "蔚蓝档案啾啾指挥家系列周边礼物卡通。两套八个，立牌10厘米双插", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/442526/25/12270/130181/6a22db58F97145bdf/0083320320dfa802.jpg.avif"), new BigDecimal("43.80"), 239, Product.Category.STAND, Product.Condition.NEW, "PLATFORM"),
                new Product("富力新 鸣潮游戏周边亚克力立牌", "礼物今汐长离吟霖角色手办摆件。今汐 10cm，亚克力材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/231033/20/23277/114985/669a1c8bF1f24fc3d/79d63a75bc20e546.jpg.avif"), new BigDecimal("13.70"), 2077, Product.Category.STAND, Product.Condition.NEW, "PLATFORM"),
                new Product("绯萤银狼lv999狼尊自制立牌", "崩坏星穹铁道如是众生欢笑不已系列。银狼LV999 10cm立牌，亚克力材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/430720/2/8510/226671/69fd4794F20439140/00835145142e489c.jpg.avif"), new BigDecimal("6.90"), 19273, Product.Category.STAND, Product.Condition.NEW, "PLATFORM"),
                new Product("何佳功 守望先锋黑寡妇手办卡通动漫周边", "二次元源氏安娜半藏立牌生日礼物。455黑寡妇 16色黑色遥控(充电款)，亚克力材质", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/269828/26/10072/85887/6825b7eaFc94b8998/ed8e29fa62829515.jpg.avif"), new BigDecimal("71.49"), 6107, Product.Category.STAND, Product.Condition.NEW, "PLATFORM"),
                // 服饰（3个）
                new Product("崩坏星穹铁道T恤周边痛衣服知更鸟", "流萤黄泉砂金夏装男女学生二次元短袖。深灰色 11知更鸟 11XL 成人L码【建议120-140斤】，夏装 圆领 日常 情侣", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/264530/24/17278/105994/67a5b452Fd41699f1/a3e32ee8bbd0088a.jpg.avif"), new BigDecimal("36.00"), 213, Product.Category.CLOTHING, Product.Condition.NEW, "PLATFORM"),
                new Product("动漫二次元galgame印花周边花衬衫痛衣服", "男女百搭和服羽织gal人物。羽织【可定制图案】3XL，印花 V领 秋季", List.of("https://img13.360buyimg.com/pcpubliccms/jfs/t1/363551/17/5637/172676/692662ceF395e162f/0ffbcdad576fddcd.png.avif"), new BigDecimal("39.00"), 999, Product.Category.CLOTHING, Product.Condition.NEW, "PLATFORM"),
                new Product("奢依君（SHEYIJUN）王者荣耀短袖孙尚香联名t恤", "无心青少年cos痛衣服皮肤宿舍开黑。HZH125异界灵契孙尚香 S(80-95)斤，圆领 印花 夏季 抗起球", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/431410/11/14822/126919/6a043d2dF897189cc/008332032080071c.jpg.avif"), new BigDecimal("72.75"), 137, Product.Category.CLOTHING, Product.Condition.NEW, "PLATFORM"),
                // 专辑（2个）
                new Product("丸子家米哈游/崩坏：星穹铁道知更鸟《空气蛹》实体CD专辑", "miHoYo周边 空气蛹 实体CD专辑", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/178792/26/51352/83262/6725632bFa966dcde/845a5fadd3c15bee.jpg.avif"), new BigDecimal("132.05"), 103, Product.Category.ALBUM, Product.Condition.NEW, "PLATFORM"),
                new Product("正版 灵笼 动漫原声音乐专辑", "纪念编码 2LP黑胶唱片 海报特典周边 第二季 喷溅彩胶。SBN：9787798449945，介质：LP，类别：合集", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/345921/9/21522/86082/6909c938F6cb5222d/071e8d7f51134ce3.png.avif"), new BigDecimal("359.00"), 0, Product.Category.ALBUM, Product.Condition.NEW, "PLATFORM"),
                // 配件（2个）
                new Product("RSRDDYPS5Pro主机Slim散热防尘网保护侧边贴膜", "防灰尘宠物猫毛发周边配件。Slim防尘网赛博朋克女高品质。材质：PU(聚氨酯)，风格：侘寂风，适用对象：记录仪保护套", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/222767/7/43432/103363/67568ee5F18825412/75f34d1d32f8637c.jpg.avif"), new BigDecimal("72.75"), 137, Product.Category.ACCESSORY, Product.Condition.NEW, "PLATFORM"),
                new Product("足卿 鸣潮适用vivos18手机壳", "s18Pro\\s18E爱弥斯防摔磁吸保护套。二次元超火动漫周边男女镭射彩银壳，月光银-爱弥斯A8【强磁吸附】VIVOS18PRO", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/454499/20/6141/130623/6a2adaf5Fd044fe60/00833203209ea05b.jpg.avif"), new BigDecimal("18.20"), 230, Product.Category.ACCESSORY, Product.Condition.NEW, "PLATFORM"),
                // 其他（1个）
                new Product("卡锐鲨三角洲行动威龙C4磁吸模型", "定时炸模型弹玩具cos卡通二次元礼物。三角洲C4磁吸投掷等多种音，游戏道具模型玩家", List.of("https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/399932/21/1834/151704/699fb63aF17998a20/00834b04b0f80f8a.jpg.avif"), new BigDecimal("359.00"), 0, Product.Category.OTHER, Product.Condition.FAIR, "PLATFORM")
        );

        // 逐个保存测试商品并设置卖家和审核状态
        for (Product product : testProducts) {
            product.setSeller(merchant);
            product.setStatus(Product.Status.APPROVED);
            productRepository.save(product);
        }

        log.info("Created {} test products with seller: {}", testProducts.size(), MERCHANT_EMAIL);
    }

    /**
     * 修复已有商品中图片URL为空的数据
     * 根据商品名称关键词匹配对应的图片URL，无法匹配则按分类使用默认图片
     * @param products 已有的商品列表
     */
    private void fixEmptyImageUrls(List<Product> products) {
        // 商品名称关键词到图片URL的映射
        java.util.Map<String, String> imageUrlMap = java.util.Map.ofEntries(
                java.util.Map.entry("深海少女", "https://www.goodsmile.com/gsc-webrevo-sdk-storage-prd/product/image/product/20130529/3939/24293/large/d6e3ea77521a4f677438f3413a90439d.jpg"),
                java.util.Map.entry("米山舞", "https://www.goodsmile.com/gsc-webrevo-sdk-storage-prd/product/image/60248/rUfbpVcekh19w38XMJCiKWZqLDP40tTH.jpg"),
                java.util.Map.entry("阿尔及利亚", "https://www.alter-shanghai.cn/files/images/az_algerie_k_all1.jpg"),
                java.util.Map.entry("弗洛洛", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/444019/29/10054/127176/6a1d348cFad8cc42d/0083438414f1a160.jpg.avif"),
                java.util.Map.entry("卡芙卡", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/423320/9/15914/101694/69eec624Facc96cb2/0083320320818a3b.jpg.avif"),
                java.util.Map.entry("newtype", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/276025/13/16166/367845/67f12d93F8e35ffa7/338d253f25fcb0aa.jpg.avif"),
                java.util.Map.entry("爱弥斯", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/414464/16/4154/102249/69d0e466F5b147f47/008332032060cb34.jpg.avif"),
                java.util.Map.entry("达妮娅", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/447180/17/11254/101515/6a28c76eF3ebb9f97/008332032008342e.jpg.avif"),
                java.util.Map.entry("纳西妲", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/393727/24/3497/369845/697daf70F02d893ea/093532032046bed8.png.avif"),
                java.util.Map.entry("芙卡洛斯", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/307794/40/20695/108304/68d6654eF5781bb63/c616afb5ec633847.jpg.avif"),
                java.util.Map.entry("龙女仆", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/158424/18/43952/89766/6607b964Fee90105a/b597e5d71fbd5cb7.jpg.avif"),
                java.util.Map.entry("鬼灭", "https://img13.360buyimg.com/pcpubliccms/jfs/t1/422732/26/17186/293567/69f2d25cF5826fb27/00833203201ef482.jpg.avif"),
                java.util.Map.entry("夏洛特", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/421397/26/7788/241524/69e3710bF6b454033/0083320320dc6505.png.avif"),
                java.util.Map.entry("罗小黑", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/315711/10/21082/286109/6889b918F377684ce/fd3dfd2083f185a6.jpg.avif"),
                java.util.Map.entry("异环", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/435374/33/2015/182812/6a01b8e9F7b0f56fb/00833203203297c5.jpg.avif"),
                java.util.Map.entry("JOJO", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/412809/27/10755/715941/69d11b47F06030a57/00834b04b0cd800e.jpg.avif"),
                java.util.Map.entry("企鹅管理员", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/452753/3/9362/64058/6a2be003Fa3ff1bef/00833203203dcfa4.jpg.avif"),
                java.util.Map.entry("菲比", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/441443/21/3749/2377357/6a155061F1bcf5a48/00834e64e6759abd.png.avif"),
                java.util.Map.entry("德莉莎", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/195359/30/50311/258038/671fabe2F7865281b/72c260d5b516f695.jpg.avif"),
                java.util.Map.entry("乐章版", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/431001/16/21038/327292/6a0d71afF82ea6721/0083320320a09106.jpg.avif"),
                java.util.Map.entry("圣娅", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/442526/25/12270/130181/6a22db58F97145bdf/0083320320dfa802.jpg.avif"),
                java.util.Map.entry("今汐", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/231033/20/23277/114985/669a1c8bF1f24fc3d/79d63a75bc20e546.jpg.avif"),
                java.util.Map.entry("银狼", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/430720/2/8510/226671/69fd4794F20439140/00835145142e489c.jpg.avif"),
                java.util.Map.entry("黑寡妇", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/269828/26/10072/85887/6825b7eaFc94b8998/ed8e29fa62829515.jpg.avif"),
                java.util.Map.entry("知更鸟", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/264530/24/17278/105994/67a5b452Fd41699f1/a3e32ee8bbd0088a.jpg.avif"),
                java.util.Map.entry("galgame", "https://img13.360buyimg.com/pcpubliccms/jfs/t1/363551/17/5637/172676/692662ceF395e162f/0ffbcdad576fddcd.png.avif"),
                java.util.Map.entry("孙尚香", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/431410/11/14822/126919/6a043d2dF897189cc/008332032080071c.jpg.avif"),
                java.util.Map.entry("空气蛹", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/178792/26/51352/83262/6725632bFa966dcde/845a5fadd3c15bee.jpg.avif"),
                java.util.Map.entry("灵笼", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/345921/9/21522/86082/6909c938F6cb5222d/071e8d7f51134ce3.png.avif"),
                java.util.Map.entry("PS5Pro", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/222767/7/43432/103363/67568ee5F18825412/75f34d1d32f8637c.jpg.avif"),
                java.util.Map.entry("手机壳", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/454499/20/6141/130623/6a2adaf5Fd044fe60/00833203209ea05b.jpg.avif"),
                java.util.Map.entry("三角洲", "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/399932/21/1834/151704/699fb63aF17998a20/00834b04b0f80f8a.jpg.avif")
        );

        // 分类到默认图片URL的映射（用于无法按名称匹配的商品）
        java.util.Map<Product.Category, String> categoryDefaultUrls = java.util.Map.ofEntries(
                java.util.Map.entry(Product.Category.ANIME_FIGURE, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/423320/9/15914/101694/69eec624Facc96cb2/0083320320818a3b.jpg.avif"),
                java.util.Map.entry(Product.Category.POSTER, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/276025/13/16166/367845/67f12d93F8e35ffa7/338d253f25fcb0aa.jpg.avif"),
                java.util.Map.entry(Product.Category.KEYCHAIN, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/393727/24/3497/369845/697daf70F02d893ea/093532032046bed8.png.avif"),
                java.util.Map.entry(Product.Category.BADGE, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/422732/26/17186/293567/69f2d25cF5826fb27/00833203201ef482.jpg.avif"),
                java.util.Map.entry(Product.Category.PILLOW, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/452753/3/9362/64058/6a2be003Fa3ff1bef/00833203203dcfa4.jpg.avif"),
                java.util.Map.entry(Product.Category.STAND, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/431001/16/21038/327292/6a0d71afF82ea6721/0083320320a09106.jpg.avif"),
                java.util.Map.entry(Product.Category.CLOTHING, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/264530/24/17278/105994/67a5b452Fd41699f1/a3e32ee8bbd0088a.jpg.avif"),
                java.util.Map.entry(Product.Category.ALBUM, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/178792/26/51352/83262/6725632bFa966dcde/845a5fadd3c15bee.jpg.avif"),
                java.util.Map.entry(Product.Category.ACCESSORY, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/454499/20/6141/130623/6a2adaf5Fd044fe60/00833203209ea05b.jpg.avif"),
                java.util.Map.entry(Product.Category.OTHER, "https://img10.360buyimg.com/pcpubliccms/s1440x1440_jfs/t1/399932/21/1834/151704/699fb63aF17998a20/00834b04b0f80f8a.jpg.avif")
        );

        int fixedCount = 0;
        for (Product product : products) {
            // 跳过已有图片的商品
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                continue;
            }

            // 根据名称关键词匹配图片URL
            String matchedUrl = null;
            for (java.util.Map.Entry<String, String> entry : imageUrlMap.entrySet()) {
                if (product.getName() != null && product.getName().contains(entry.getKey())) {
                    matchedUrl = entry.getValue();
                    break;
                }
            }

            // 名称未匹配时，使用分类默认图片
            if (matchedUrl == null && product.getCategory() != null) {
                matchedUrl = categoryDefaultUrls.get(product.getCategory());
            }

            // 更新图片URL并保存
            if (matchedUrl != null) {
                product.setImageUrls(List.of(matchedUrl));
                productRepository.save(product);
                fixedCount++;
            }
        }

        if (fixedCount > 0) {
            log.info("Fixed empty imageUrls for {} products", fixedCount);
        }
    }
}
