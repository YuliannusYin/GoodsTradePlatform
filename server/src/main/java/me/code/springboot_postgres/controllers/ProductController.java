package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.ProductDTO;
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
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProducts() {
        return ApiResponse.ok("Products retrieved", productService.getProducts()).toResponseEntity();
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFeaturedProducts() {
        return ApiResponse.ok("Featured products retrieved", productService.getFeaturedProducts()).toResponseEntity();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable String productId) {
        return ApiResponse.ok("Product retrieved", productService.getProduct(productId)).toResponseEntity();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam String filter,
            @RequestParam(required = false) String category) {
        return ApiResponse.ok("Search results retrieved", productService.getSearchedProducts(query, filter, category)).toResponseEntity();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        return ApiResponse.ok("Products by category retrieved", productService.getProductsByCategory(category)).toResponseEntity();
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Product.Category[]>> getCategories() {
        return ApiResponse.ok("Categories retrieved", Product.Category.values()).toResponseEntity();
    }

    @GetMapping("/conditions")
    public ResponseEntity<ApiResponse<Product.Condition[]>> getConditions() {
        return ApiResponse.ok("Conditions retrieved", Product.Condition.values()).toResponseEntity();
    }
}
