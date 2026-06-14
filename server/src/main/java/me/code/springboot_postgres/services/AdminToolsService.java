/**
 * @file AdminToolsService.java
 * @description 管理员工具服务类，提供商品管理（增删改查、审核）和订单管理（查看、发货）的业务逻辑
 * @input 商品DTO、订单ID、状态等参数
 * @output 商品DTO列表、用户订单DTO列表或操作结果
 */
package me.code.springboot_postgres.services;

import org.springframework.transaction.annotation.Transactional;
import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserOrderDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.repositories.CartItemRepository;
import me.code.springboot_postgres.repositories.FavoriteRepository;
import me.code.springboot_postgres.repositories.OrderRepository;
import me.code.springboot_postgres.repositories.ProductRepository;
import me.code.springboot_postgres.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 管理员工具服务类
 * 职责：处理管理员的商品管理操作（增删改查、审核、启用/禁用）和订单管理操作（查看、发货、修改送达时间）
 */
@Service
public class AdminToolsService {

    // 日期时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public AdminToolsService(OrderRepository orderRepository, ProductRepository productRepository,
                             ProductService productService, CartItemRepository cartItemRepository,
                             FavoriteRepository favoriteRepository, ReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.cartItemRepository = cartItemRepository;
        this.favoriteRepository = favoriteRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * 获取所有用户的所有订单
     * @return 用户订单DTO列表
     */
    @Transactional(readOnly = true)
    public List<UserOrderDTO> getAllUsersOrders() {
        return orderRepository.findAllUsersOrders().stream()
                .map(UserOrderDTO::from).toList();
    }

    /**
     * 根据状态获取所有用户的订单
     * @param status 订单状态字符串
     * @return 指定状态的用户订单DTO列表
     */
    @Transactional(readOnly = true)
    public List<UserOrderDTO> getAllUsersOrders(String status) {
        // 验证订单状态是否合法
        if (!isValidOrderStatus(status)) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, status + " is invalid");
        }
        Order.Status statusEnum = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findAllUsersOrdersByStatus(statusEnum).stream()
                .map(UserOrderDTO::from).toList();
    }

    /**
     * 验证订单状态字符串是否合法
     * @param status 状态字符串
     * @return 是否合法
     */
    public boolean isValidOrderStatus(String status) {
        try {
            Order.Status.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 发货并设置预计送达时间
     * @param orderId 订单ID
     * @param dateAndTime 预计送达时间字符串
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> sendOrder(String orderId, String dateAndTime) {
        Order order = findOrder(orderId);
        // 解析日期时间字符串
        LocalDateTime expectedDelivery = LocalDateTime.parse(dateAndTime, DATE_TIME_FORMATTER);
        // 更新订单状态为已发货
        order.setStatus(Order.Status.SHIPPED);
        order.setExpectedDelivery(expectedDelivery);
        orderRepository.save(order);
        return ApiResponse.ok("Order was successfully sent");
    }

    /**
     * 修改订单的预计送达时间
     * @param orderId 订单ID
     * @param newDateAndTime 新的预计送达时间字符串
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> changeExpectedDelivery(String orderId, String newDateAndTime) {
        Order order = findOrder(orderId);
        LocalDateTime newExpectedDelivery = LocalDateTime.parse(newDateAndTime, DATE_TIME_FORMATTER);
        order.setExpectedDelivery(newExpectedDelivery);
        orderRepository.save(order);
        return ApiResponse.ok("Successfully updated expected delivery");
    }

    /**
     * 根据ID查找订单，不存在则抛出异常
     * @param orderId 订单ID
     * @return 订单实体
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Order findOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find order with id: " + orderId));
    }

    /**
     * 管理员添加新商品（状态直接为已审核）
     * @param dto 商品请求数据
     * @return 创建的商品DTO
     */
    @Transactional
    public me.code.springboot_postgres.dtos.responses.ProductDTO addProduct(ProductDTO dto) {
        // 成色为空时默认为全新
        Product.Condition condition = dto.condition() != null
                ? Product.Condition.valueOf(dto.condition())
                : Product.Condition.NEW;
        Product.Category category = Product.Category.valueOf(dto.category());

        Product product = new Product(
                dto.name(), dto.description(), dto.imageUrls(),
                dto.price(), dto.quantity(), category, condition,
                // 来源为空时默认为平台商品
                dto.source() != null ? dto.source() : "PLATFORM");
        // 管理员添加的商品直接审核通过
        product.setStatus(Product.Status.APPROVED);
        return me.code.springboot_postgres.dtos.responses.ProductDTO.from(productRepository.save(product));
    }

    /**
     * 删除指定商品
     * 删除前清理该商品关联的购物车项、收藏记录和评价记录，订单项保留用于业务记录
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId);
        }
        // 清理商品关联的购物车项，避免外键约束冲突
        cartItemRepository.deleteByProductId(productId);
        // 清理商品关联的收藏记录，避免外键约束冲突
        favoriteRepository.deleteByProductId(productId);
        // 清理商品关联的评价记录，避免外键约束冲突
        reviewRepository.deleteByProductId(productId);
        // 注意：不删除订单项记录，保留用于业务记录
        productRepository.deleteById(productId);
        return ApiResponse.ok("The product was deleted successfully");
    }

    /**
     * 编辑指定商品信息
     * @param productId 商品ID
     * @param dto 商品更新数据
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> editProduct(String productId, ProductDTO dto) {
        Product product = productService.loadProductById(productId);
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setImageUrls(dto.imageUrls());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());
        product.setCategory(Product.Category.valueOf(dto.category()));
        // 成色非空时才更新
        if (dto.condition() != null) {
            product.setCondition(Product.Condition.valueOf(dto.condition()));
        }
        product.setSource(dto.source());
        productRepository.save(product);
        return ApiResponse.ok("The product was edited successfully");
    }

    /**
     * 获取待审核商品列表
     * @return 待审核商品DTO列表
     */
    @Transactional(readOnly = true)
    public List<me.code.springboot_postgres.dtos.responses.ProductDTO> getPendingProducts() {
        return productRepository.findByStatus(Product.Status.PENDING).stream()
                .map(me.code.springboot_postgres.dtos.responses.ProductDTO::from).toList();
    }

    /**
     * 根据状态获取商品列表
     * @param status 商品状态字符串
     * @return 指定状态的商品DTO列表
     */
    @Transactional(readOnly = true)
    public List<me.code.springboot_postgres.dtos.responses.ProductDTO> getProductsByStatus(String status) {
        Product.Status statusEnum;
        try {
            statusEnum = Product.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "无效的商品状态: " + status);
        }
        return productRepository.findByStatus(statusEnum).stream()
                .map(me.code.springboot_postgres.dtos.responses.ProductDTO::from).toList();
    }

    /**
     * 审核通过指定商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> approveProduct(String productId) {
        Product product = productService.loadProductById(productId);
        // 只有待审核的商品才能通过
        if (product.getStatus() != Product.Status.PENDING) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only pending products can be approved");
        }
        product.setStatus(Product.Status.APPROVED);
        product.setRejectReason(null);
        productRepository.save(product);
        return ApiResponse.ok("Product approved successfully");
    }

    /**
     * 驳回指定商品并填写驳回原因
     * @param productId 商品ID
     * @param rejectReason 驳回原因
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> rejectProduct(String productId, String rejectReason) {
        Product product = productService.loadProductById(productId);
        // 只有待审核的商品才能驳回
        if (product.getStatus() != Product.Status.PENDING) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only pending products can be rejected");
        }
        product.setStatus(Product.Status.REJECTED);
        product.setRejectReason(rejectReason);
        productRepository.save(product);
        return ApiResponse.ok("Product rejected successfully");
    }

    /**
     * 禁用指定商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> disableProduct(String productId) {
        Product product = productService.loadProductById(productId);
        product.setStatus(Product.Status.DISABLED);
        productRepository.save(product);
        return ApiResponse.ok("Product disabled successfully");
    }

    /**
     * 重新启用被禁用的商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> enableProduct(String productId) {
        Product product = productService.loadProductById(productId);
        // 只有被禁用的商品才能重新启用
        if (product.getStatus() == Product.Status.DISABLED) {
            product.setStatus(Product.Status.APPROVED);
            productRepository.save(product);
            return ApiResponse.ok("Product re-enabled successfully");
        }
        throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only disabled products can be re-enabled");
    }

}
