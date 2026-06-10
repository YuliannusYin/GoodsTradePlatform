package me.code.springboot_neo4j.services;

import me.code.springboot_neo4j.dtos.responses.entities.UnavailableProductDTO;
import me.code.springboot_neo4j.exceptions.types.CustomRuntimeException;
import me.code.springboot_neo4j.models.nodes.OrderItem;
import me.code.springboot_neo4j.models.nodes.Product;
import me.code.springboot_neo4j.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(String productId) {
        try {
            return loadProductById(productId);
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find requested product");
        }
    }

    public List<Product> getFeaturedProducts() {
        int productAmount = 4;
        return productRepository.findProductsWithBiggestQuantity(productAmount);
    }

    public List<Product> getSearchedProducts(String query, String filter) {
        return getSearchedProducts(query, filter, null);
    }

    public List<Product> getSearchedProducts(String query, String filter, String category) {
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
            return getSearchedProductsWithCategory(query, filter, category);
        }
        if (isBlankSearchQuery(query)) {
            return loadAllProductsWithFilter(filter);
        } else {
            return loadSearchedProductsWithFilter(query, filter);
        }
    }

    private List<Product> getSearchedProductsWithCategory(String query, String filter, String category) {
        if (isBlankSearchQuery(query)) {
            return loadAllProductsByCategoryWithFilter(category, filter);
        } else {
            return loadSearchedProductsByCategoryWithFilter(query, category, filter);
        }
    }

    private boolean isBlankSearchQuery(String query) {
        return query.isBlank();
    }

    private List<Product> loadAllProductsWithFilter(String filter) {
        return switch (filter) {
            case "lowest_price" -> productRepository.findAllProductsOrderedByLowestPrice();
            case "highest_price" -> productRepository.findAllProductsOrderedByHighestPrice();
            default -> productRepository.findAll();
        };
    }

    private List<Product> loadSearchedProductsWithFilter(String query, String filter) {
        return switch (filter) {
            case "lowest_price" -> productRepository.findSearchedProductsOrderedByLowestPrice(query);
            case "highest_price" -> productRepository.findSearchedProductsOrderedByHighestPrice(query);
            default -> productRepository.findProductsBySearch(query);
        };
    }

    private List<Product> loadAllProductsByCategoryWithFilter(String category, String filter) {
        return switch (filter) {
            case "lowest_price" -> productRepository.findByCategoryOrderedByLowestPrice(category);
            case "highest_price" -> productRepository.findByCategoryOrderedByHighestPrice(category);
            default -> productRepository.findByCategory(category);
        };
    }

    private List<Product> loadSearchedProductsByCategoryWithFilter(String query, String category, String filter) {
        return switch (filter) {
            case "lowest_price" -> productRepository.findSearchedProductsByCategoryOrderedByLowestPrice(query, category);
            case "highest_price" -> productRepository.findSearchedProductsByCategoryOrderedByHighestPrice(query, category);
            default -> productRepository.findSearchedProductsByCategory(query, category);
        };
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsBySellerId(String userId) {
        return productRepository.findBySellerId(userId);
    }

    public List<UnavailableProductDTO> findUnavailableProducts(List<OrderItem> items) {
        List<UnavailableProductDTO> unavailableProducts = new ArrayList<>();

        for (var item : items) {
            Product targetProduct = item.getProduct();
            int requestedAmount = item.getAmount();
            int availableAmount = loadProductById(targetProduct.getId()).getQuantity();

            if (isUnavailableProduct(availableAmount, requestedAmount)) {
                unavailableProducts.add(
                        new UnavailableProductDTO(
                                "Requested amount not available",
                                targetProduct.getId(),
                                requestedAmount,
                                availableAmount));
            }
        }

        return unavailableProducts;
    }

    private boolean isUnavailableProduct(int availableAmount, int requestedAmount) {
        return (availableAmount - requestedAmount) < 0;
    }

    public void updateProductQuantities(List<OrderItem> items) {
        for (var item : items) {
            var targetProduct = item.getProduct();
            targetProduct.setQuantity(targetProduct.getQuantity() - item.getAmount());

            productRepository.save(targetProduct);
        }

    }

    public Product loadProductById(String productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product with id: " + productId + " not found"));
    }

    public List<Product> loadProductsById(String[] productIds) {
        return Stream.of(productIds)
                .map(this::loadProductById)
                .toList();
    }

}
