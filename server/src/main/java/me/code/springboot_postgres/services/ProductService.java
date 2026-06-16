/**
 * @file ProductService.java
 * @description 商品服务类，提供商品查询、搜索、库存检查和库存更新的业务逻辑
 * @input 商品ID、搜索关键词、筛选条件、分类、订单项列表
 * @output 商品DTO列表、不可用商品列表或操作结果
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.ProductDTO;
import me.code.springboot_postgres.dtos.responses.UnavailableProductDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.OrderItem;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.repositories.ProductRepository;
import me.code.springboot_postgres.repositories.ProductSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 商品服务类
 * 职责：处理商品查询、搜索筛选、库存检查和库存扣减等核心业务逻辑
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 获取所有已审核通过的商品列表
     * @return 商品DTO列表
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<ProductDTO> getProducts() {
        return productRepository.findAll(ProductSpecifications.hasStatus(Product.Status.APPROVED))
                .stream().map(ProductDTO::from).toList();
    }

    /**
     * 根据商品ID获取单个商品详情（仅返回已审核通过的商品）
     * @param productId 商品ID
     * @return 商品DTO
     */
    @Transactional(readOnly = true)
    public ProductDTO getProduct(String productId) {
        Product product = loadProductById(productId);
        // 非已审核通过的商品对普通用户不可见
        if (product.getStatus() != Product.Status.APPROVED) {
            throw new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find requested product");
        }
        return ProductDTO.from(product);
    }

    /**
     * 获取精选商品列表（按库存降序取前4个已审核通过的商品）
     * @return 精选商品DTO列表
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getFeaturedProducts() {
        Specification<Product> spec = ProductSpecifications.hasStatus(Product.Status.APPROVED)
                .and(ProductSpecifications.orderByQuantityDesc());
        return productRepository.findAll(spec, PageRequest.of(0, 4))
                .stream().map(ProductDTO::from).toList();
    }

    /**
     * 搜索商品（不含分类过滤）
     * @param query 搜索关键词
     * @param filter 排序条件
     * @return 搜索结果商品DTO列表
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getSearchedProducts(String query, String filter) {
        return getSearchedProducts(query, filter, null);
    }

    /**
     * 搜索商品（含分类过滤），支持关键词搜索、分类过滤和价格排序
     * @param query 搜索关键词
     * @param filter 排序条件（lowest_price/highest_price）
     * @param category 商品分类（可选）
     * @return 搜索结果商品DTO列表
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<ProductDTO> getSearchedProducts(String query, String filter, String category) {
        // 基础条件：已审核通过
        Specification<Product> spec = ProductSpecifications.hasStatus(Product.Status.APPROVED);

        // 添加分类过滤条件
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
            try {
                spec = spec.and(ProductSpecifications.hasCategory(Product.Category.valueOf(category)));
            } catch (IllegalArgumentException e) {
                throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "无效的商品分类: " + category);
            }
        }

        // 添加名称搜索条件
        if (query != null && !query.isBlank()) {
            spec = spec.and(ProductSpecifications.nameContains(query));
        }

        // 应用排序条件
        spec = applySort(spec, filter);

        return productRepository.findAll(spec).stream().map(ProductDTO::from).toList();
    }

    /**
     * 根据筛选条件应用排序
     * @param spec 当前查询条件
     * @param filter 排序条件
     * @return 添加排序后的查询条件
     */
    @SuppressWarnings("null")
    private Specification<Product> applySort(Specification<Product> spec, String filter) {
        return switch (filter) {
            case "lowest_price" -> spec.and(ProductSpecifications.orderByPriceAsc());
            case "highest_price" -> spec.and(ProductSpecifications.orderByPriceDesc());
            default -> spec;
        };
    }

    /**
     * 根据分类获取商品列表
     * @param categoryStr 分类名称字符串
     * @return 指定分类的商品DTO列表
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String categoryStr) {
        Product.Category category = Product.Category.valueOf(categoryStr);
        Specification<Product> spec = ProductSpecifications.hasStatus(Product.Status.APPROVED)
                .and(ProductSpecifications.hasCategory(category));
        return productRepository.findAll(spec).stream().map(ProductDTO::from).toList();
    }

    /**
     * 根据卖家ID获取商品列表
     * @param userId 卖家用户ID
     * @return 该卖家的商品DTO列表
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsBySellerId(String userId) {
        return productRepository.findBySellerId(userId).stream().map(ProductDTO::from).toList();
    }

    /**
     * 检查订单项中的商品库存是否充足
     * @param items 订单项列表
     * @return 不可用商品列表（库存不足或商品不存在）
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<UnavailableProductDTO> findUnavailableProducts(List<OrderItem> items) {
        return items.stream()
                .map(item -> {
                    Product p = productRepository.findById(item.getProduct().getId()).orElse(null);
                    // 商品不存在
                    if (p == null) {
                        return new UnavailableProductDTO("Product not found", item.getProduct().getId(), item.getAmount(), 0);
                    }
                    // 库存不足
                    if (p.getQuantity() < item.getAmount()) {
                        return new UnavailableProductDTO("Requested amount not available", p.getId(), item.getAmount(), p.getQuantity());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 扣减订单项中商品的库存数量
     * @param items 订单项列表
     */
    @Transactional
    public void updateProductQuantities(List<OrderItem> items) {
        for (var item : items) {
            Product product = loadProductById(item.getProduct().getId());
            // 扣减库存
            product.setQuantity(product.getQuantity() - item.getAmount());
            productRepository.save(product);
        }
    }

    /**
     * 根据ID加载商品，不存在则抛出异常
     * @param productId 商品ID
     * @return 商品实体
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Product loadProductById(String productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product with id: " + productId + " not found"));
    }

    /**
     * 根据ID数组批量加载商品（保留重复ID以支持数量信息）
     * findAllById内部执行SELECT WHERE id IN(...)会自动去重，
     * 因此需要手动按原始ID顺序构建结果列表，保留重复ID对应的商品实例
     * @param productIds 商品ID数组（可包含重复ID，重复次数代表购买数量）
     * @return 商品实体列表（保留重复ID的顺序和数量）
     */
    @Transactional(readOnly = true)
    public List<Product> loadProductsById(String[] productIds) {
        // 先批量查询去重后的商品
        List<Product> uniqueProducts = productRepository.findAllById(Arrays.asList(productIds));
        // 构建ID到商品的映射，用于快速查找
        Map<String, Product> productMap = uniqueProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        // 按原始ID数组顺序返回商品列表（保留重复ID，以支持数量信息）
        return Arrays.stream(productIds)
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
