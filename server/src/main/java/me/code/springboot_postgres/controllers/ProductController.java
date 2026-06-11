package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getProducts() {
        var result = productService.getProducts();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        var result = productService.getFeaturedProducts();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable String productId) {
        var result = productService.getProduct(productId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam String filter,
            @RequestParam(required = false) String category) {
        var result = productService.getSearchedProducts(query, filter, category);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        var result = productService.getProductsByCategory(category);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categories")
    public ResponseEntity<Product.Category[]> getCategories() {
        return ResponseEntity.ok(Product.Category.values());
    }

    @GetMapping("/conditions")
    public ResponseEntity<Product.Condition[]> getConditions() {
        return ResponseEntity.ok(Product.Condition.values());
    }
}
