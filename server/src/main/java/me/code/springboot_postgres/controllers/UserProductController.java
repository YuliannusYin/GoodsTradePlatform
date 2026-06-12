package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.ProductService;
import me.code.springboot_postgres.services.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user_products")
public class UserProductController {

    private final ProductService productService;
    private final UserProductService userProductService;

    @Autowired
    public UserProductController(ProductService productService, UserProductService userProductService) {
        this.productService = productService;
        this.userProductService = userProductService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<me.code.springboot_postgres.dtos.responses.ProductDTO>> addProduct(@AuthenticationPrincipal User user, @RequestBody ProductDTO dto) {
        return ApiResponse.ok("Product submitted for review", userProductService.addProductByUser(user, dto)).toResponseEntity();
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<me.code.springboot_postgres.dtos.responses.ProductDTO>>> getMyProducts(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("My products retrieved", productService.getProductsBySellerId(user.getId())).toResponseEntity();
    }

    @PutMapping("/edit/{productId}")
    public ResponseEntity<ApiResponse<Void>> editMyProduct(@AuthenticationPrincipal User user, @PathVariable String productId, @RequestBody ProductDTO dto) {
        return userProductService.editOwnProduct(user, productId, dto).toResponseEntity();
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyProduct(@AuthenticationPrincipal User user, @PathVariable String productId) {
        return userProductService.deleteOwnProduct(user, productId).toResponseEntity();
    }
}
