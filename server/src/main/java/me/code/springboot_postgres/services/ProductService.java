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

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProducts() {
        return productRepository.findAll(ProductSpecifications.hasStatus(Product.Status.APPROVED))
                .stream().map(ProductDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO getProduct(String productId) {
        Product product = loadProductById(productId);
        if (product.getStatus() != Product.Status.APPROVED) {
            throw new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find requested product");
        }
        return ProductDTO.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getFeaturedProducts() {
        Specification<Product> spec = ProductSpecifications.hasStatus(Product.Status.APPROVED)
                .and(ProductSpecifications.orderByQuantityDesc());
        return productRepository.findAll(spec, PageRequest.of(0, 4))
                .stream().map(ProductDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getSearchedProducts(String query, String filter) {
        return getSearchedProducts(query, filter, null);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getSearchedProducts(String query, String filter, String category) {
        Specification<Product> spec = ProductSpecifications.hasStatus(Product.Status.APPROVED);

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
            spec = spec.and(ProductSpecifications.hasCategory(Product.Category.valueOf(category)));
        }

        if (query != null && !query.isBlank()) {
            spec = spec.and(ProductSpecifications.nameContains(query));
        }

        spec = applySort(spec, filter);

        return productRepository.findAll(spec).stream().map(ProductDTO::from).toList();
    }

    private Specification<Product> applySort(Specification<Product> spec, String filter) {
        return switch (filter) {
            case "lowest_price" -> spec.and(ProductSpecifications.orderByPriceAsc());
            case "highest_price" -> spec.and(ProductSpecifications.orderByPriceDesc());
            default -> spec;
        };
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String categoryStr) {
        Product.Category category = Product.Category.valueOf(categoryStr);
        Specification<Product> spec = ProductSpecifications.hasStatus(Product.Status.APPROVED)
                .and(ProductSpecifications.hasCategory(category));
        return productRepository.findAll(spec).stream().map(ProductDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsBySellerId(String userId) {
        return productRepository.findBySellerId(userId).stream().map(ProductDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UnavailableProductDTO> findUnavailableProducts(List<OrderItem> items) {
        return items.stream()
                .map(item -> {
                    Product p = productRepository.findById(item.getProduct().getId()).orElse(null);
                    if (p == null) {
                        return new UnavailableProductDTO("Product not found", item.getProduct().getId(), item.getAmount(), 0);
                    }
                    if (p.getQuantity() < item.getAmount()) {
                        return new UnavailableProductDTO("Requested amount not available", p.getId(), item.getAmount(), p.getQuantity());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public void updateProductQuantities(List<OrderItem> items) {
        for (var item : items) {
            Product product = loadProductById(item.getProduct().getId());
            product.setQuantity(product.getQuantity() - item.getAmount());
            productRepository.save(product);
        }
    }

    @Transactional(readOnly = true)
    public Product loadProductById(String productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product with id: " + productId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Product> loadProductsById(String[] productIds) {
        return Stream.of(productIds).map(this::loadProductById).toList();
    }
}
