package me.code.springboot_neo4j.config.neo4j;

import me.code.springboot_neo4j.models.nodes.Product;
import me.code.springboot_neo4j.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockProductsConfig {

    private final ProductRepository productRepository;

    private static final List<Product> MOCK_PRODUCTS = List.of(
            new Product(
                    "初音未来 手办 Ver.",
                    "初音未来经典造型手办，高精度涂装，约23cm高，附带底座。",
                    List.of("https://m.media-amazon.com/images/I/71K3JnFqJWL._AC_SL1500_.jpg"),
                    299.00,
                    10,
                    "ANIME_FIGURE",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "进击的巨人 海报套装",
                    "进击的巨人动画经典场景海报5张套装，A3尺寸，高清印刷。",
                    List.of("https://m.media-amazon.com/images/I/81Jqd2BxDNL._AC_SL1500_.jpg"),
                    49.90,
                    20,
                    "POSTER",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "鬼灭之善逸 钥匙扣",
                    "我妻善逸Q版造型钥匙扣，合金材质，精美电镀工艺。",
                    List.of("https://m.media-amazon.com/images/I/61C5XwJqYqL._AC_SL1500_.jpg"),
                    19.90,
                    50,
                    "KEYCHAIN",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "Fate/stay night 徽章套装",
                    "Saber、Archer、Rider等角色金属徽章6枚套装，直径5.8cm。",
                    List.of("https://m.media-amazon.com/images/I/71qw3P3mYPL._AC_SL1500_.jpg"),
                    35.00,
                    30,
                    "BADGE",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "Re:Zero 蕾姆抱枕",
                    "蕾姆48cm×150cm长款抱枕，双面不同图案，柔软亲肤面料。",
                    List.of("https://m.media-amazon.com/images/I/61ZLq3M5RjL._AC_SL1500_.jpg"),
                    89.00,
                    15,
                    "PILLOW",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "咒术回战 五条悟亚克力立牌",
                    "五条悟Q版亚克力立牌，高约15cm，附透明支架。",
                    List.of("https://m.media-amazon.com/images/I/51C8K5V5qQL._AC_SL1000_.jpg"),
                    29.90,
                    25,
                    "STAND",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "JoJo的奇妙冒险 T恤",
                    "JoJo经典姿势印花T恤，纯棉面料，L码。",
                    List.of("https://m.media-amazon.com/images/I/71YJZ3T9K6L._AC_SL1500_.jpg"),
                    79.00,
                    20,
                    "CLOTHING",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "YOASOBI 原声专辑",
                    "YOASOBI精选专辑CD，收录《夜に駆ける》《群青》等热门曲目。",
                    List.of("https://m.media-amazon.com/images/I/71qVJQK5JfL._AC_SL1500_.jpg"),
                    128.00,
                    12,
                    "ALBUM",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "宝可梦 皮卡丘手机壳",
                    "皮卡丘可爱造型手机壳，适配iPhone 15，TPU软壳防摔。",
                    List.of("https://m.media-amazon.com/images/I/61U+J3Q3qJL._AC_SL1500_.jpg"),
                    39.90,
                    40,
                    "ACCESSORY",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "海贼王 路飞手办 二手",
                    "路飞四档手办，轻微展示痕迹，整体品相良好，约18cm。",
                    List.of("https://m.media-amazon.com/images/I/71R5F3JvWmL._AC_SL1500_.jpg"),
                    159.00,
                    5,
                    "ANIME_FIGURE",
                    Product.Condition.GOOD,
                    "USER"),
            new Product(
                    "原神 钟离亚克力立牌",
                    "钟离角色立牌，精美印刷，高约12cm。",
                    List.of("https://m.media-amazon.com/images/I/51C8K5V5qQL._AC_SL1000_.jpg"),
                    25.00,
                    30,
                    "STAND",
                    Product.Condition.NEW,
                    "PLATFORM"),
            new Product(
                    "间谍过家家 阿尼亚钥匙扣",
                    "阿尼亚wakuwaku造型钥匙扣，亚克力材质，双面印刷。",
                    List.of("https://m.media-amazon.com/images/I/61C5XwJqYqL._AC_SL1500_.jpg"),
                    15.90,
                    60,
                    "KEYCHAIN",
                    Product.Condition.NEW,
                    "PLATFORM"));

    @Autowired
    public MockProductsConfig(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void createDefaultProducts() {

        if (isProductRepositoryEmpty()) {
            MOCK_PRODUCTS.forEach(this::createProduct);
        }
    }

    public boolean isProductRepositoryEmpty() {
        return productRepository.count() == 0;
    }

    private void createProduct(Product product) {
        productRepository.save(product);
        System.out.println("MockProductsConfig created a new product: " + product.getName());
    }
}
