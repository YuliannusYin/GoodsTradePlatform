package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserProductService {

    private final ProductRepository productRepository;

    @Autowired
    public UserProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProductByUser(User user, ProductDTO dto) {
        try {
            Product.Condition condition = dto.condition() != null
                    ? Product.Condition.valueOf(dto.condition())
                    : Product.Condition.NEW;
            Product.Category category = Product.Category.valueOf(dto.category());

            Product product = new Product(
                    dto.name(), dto.description(), dto.imageUrls(),
                    dto.price(), dto.quantity(), category, condition, "USER");

            product.setSeller(user);
            product.setStatus(Product.Status.PENDING);
            return productRepository.save(product);
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not create product");
        }
    }

    public Success editOwnProduct(User user, String productId, ProductDTO dto) {
        Product product = loadProductAndVerifyOwnership(user, productId);

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setImageUrls(dto.imageUrls());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());
        product.setCategory(Product.Category.valueOf(dto.category()));
        if (dto.condition() != null) {
            product.setCondition(Product.Condition.valueOf(dto.condition()));
        }

        if (product.getStatus() == Product.Status.REJECTED) {
            product.setStatus(Product.Status.PENDING);
            product.setRejectReason(null);
        }

        productRepository.save(product);
        return new Success(HttpStatus.OK, "Product updated successfully");
    }

    public Success deleteOwnProduct(User user, String productId) {
        Product product = loadProductAndVerifyOwnership(user, productId);
        productRepository.delete(product);
        return new Success(HttpStatus.OK, "Product deleted successfully");
    }

    private Product loadProductAndVerifyOwnership(User user, String productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId));

        if (product.getSeller() == null || !product.getSeller().getId().equals(user.getId())) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "You can only modify your own products");
        }

        return product;
    }
}
