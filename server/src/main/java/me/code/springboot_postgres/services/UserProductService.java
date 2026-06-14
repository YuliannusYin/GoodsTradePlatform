/**
 * @file UserProductService.java
 * @description 商户商品服务类，提供商户添加商品、编辑和删除自己商品的业务逻辑
 * @input 用户实体、商品DTO、商品ID
 * @output 商品DTO或操作结果
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商户商品服务类
 * 职责：处理商户对自己商品的添加（需审核）、编辑和删除操作，包含所有权验证
 */
@Service
public class UserProductService {

    private final ProductRepository productRepository;

    @Autowired
    public UserProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 商户提交新商品（状态为待审核）
     * @param user 商户用户
     * @param dto 商品请求数据
     * @return 创建的商品DTO
     */
    @Transactional
    public me.code.springboot_postgres.dtos.responses.ProductDTO addProductByUser(User user, ProductDTO dto) {
        // 成色为空时默认为全新
        Product.Condition condition = dto.condition() != null
                ? Product.Condition.valueOf(dto.condition())
                : Product.Condition.NEW;
        Product.Category category = Product.Category.valueOf(dto.category());

        Product product = new Product(
                dto.name(), dto.description(), dto.imageUrls(),
                dto.price(), dto.quantity(), category, condition, "USER");
        product.setSeller(user);
        // 商户提交的商品需审核
        product.setStatus(Product.Status.PENDING);
        return me.code.springboot_postgres.dtos.responses.ProductDTO.from(productRepository.save(product));
    }

    /**
     * 编辑自己的商品信息
     * @param user 商户用户
     * @param productId 商品ID
     * @param dto 商品更新数据
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> editOwnProduct(User user, String productId, ProductDTO dto) {
        // 验证商品所有权
        Product product = loadProductAndVerifyOwnership(user, productId);
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
        // 被驳回的商品编辑后重新提交审核
        if (product.getStatus() == Product.Status.REJECTED) {
            product.setStatus(Product.Status.PENDING);
            product.setRejectReason(null);
        }
        productRepository.save(product);
        return ApiResponse.ok("Product updated successfully");
    }

    /**
     * 删除自己的商品
     * @param user 商户用户
     * @param productId 商品ID
     * @return 操作结果
     */
    @Transactional
    @SuppressWarnings("null")
    public ApiResponse<Void> deleteOwnProduct(User user, String productId) {
        Product product = loadProductAndVerifyOwnership(user, productId);
        productRepository.delete(product);
        return ApiResponse.ok("Product deleted successfully");
    }

    /**
     * 加载商品并验证当前用户是否为商品所有者
     * @param user 当前用户
     * @param productId 商品ID
     * @return 商品实体
     */
    @SuppressWarnings("null")
    private Product loadProductAndVerifyOwnership(User user, String productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId));
        // 验证商品卖家是否为当前用户
        if (product.getSeller() == null || !product.getSeller().getId().equals(user.getId())) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "You can only modify your own products");
        }
        return product;
    }
}
