/**
 * @file CartService.java
 * @description 购物车服务类，提供购物车的增删改查和合并等业务逻辑
 * @input 用户ID、商品ID、数量、本地购物车条目列表
 * @output 购物车DTO或操作结果
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.MergeCartDTO;
import me.code.springboot_postgres.dtos.responses.CartDTO;
import me.code.springboot_postgres.models.entities.CartItem;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.CartItemRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务类
 * 职责：处理购物车的获取、添加商品、更新数量、移除商品、清空和合并等核心业务逻辑
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductService productService, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    /**
     * 获取用户购物车
     * @param userId 用户ID
     * @return 购物车DTO
     */
    @Transactional(readOnly = true)
    public CartDTO getCart(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return CartDTO.from(items);
    }

    /**
     * 添加商品到购物车
     * 如果商品已存在于购物车中，则将数量相加
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  添加数量
     * @return 更新后的购物车DTO
     */
    @Transactional
    public CartDTO addItem(String userId, String productId, int quantity) {
        // 加载商品实体，不存在则抛出异常
        Product product = productService.loadProductById(productId);
        // 加载用户实体
        User user = userRepository.findById(userId).orElseThrow();

        // 检查商品是否已在购物车中
        var existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existingItem.isPresent()) {
            // 商品已存在，数量相加
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // 商品不存在，创建新的购物车项
            CartItem newItem = new CartItem(user, product, quantity);
            cartItemRepository.save(newItem);
        }

        return getCart(userId);
    }

    /**
     * 更新购物车中商品的数量
     * 如果数量小于等于0，则删除该购物车项
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  新数量
     * @return 更新后的购物车DTO
     */
    @Transactional
    public CartDTO updateItemQuantity(String userId, String productId, int quantity) {
        if (quantity <= 0) {
            // 数量小于等于0，直接删除该购物车项
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            // 查找购物车项并更新数量
            CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCart(userId);
    }

    /**
     * 移除购物车中的指定商品
     * @param userId    用户ID
     * @param productId 商品ID
     */
    @Transactional
    public void removeItem(String userId, String productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    /**
     * 清空用户购物车
     * @param userId 用户ID
     */
    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    /**
     * 合并本地购物车到后端购物车
     * 合并策略：如果商品已存在于后端购物车，则数量相加（本地数量 + 后端数量）
     * @param userId 用户ID
     * @param items  本地购物车商品条目列表
     * @return 合并后的购物车DTO
     */
    @Transactional
    public CartDTO mergeCart(String userId, List<MergeCartDTO.CartItemEntry> items) {
        // 加载用户实体
        User user = userRepository.findById(userId).orElseThrow();

        for (MergeCartDTO.CartItemEntry entry : items) {
            // 加载商品实体，不存在则跳过该条目
            Product product;
            try {
                product = productService.loadProductById(entry.productId());
            } catch (RuntimeException e) {
                // 商品不存在时跳过，继续处理其他条目
                continue;
            }

            // 检查商品是否已在后端购物车中
            var existingItem = cartItemRepository.findByUserIdAndProductId(userId, entry.productId());
            if (existingItem.isPresent()) {
                // 商品已存在，数量相加（本地数量 + 后端数量）
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + entry.quantity());
                cartItemRepository.save(item);
            } else {
                // 商品不存在，创建新的购物车项
                CartItem newItem = new CartItem(user, product, entry.quantity());
                cartItemRepository.save(newItem);
            }
        }

        return getCart(userId);
    }
}
