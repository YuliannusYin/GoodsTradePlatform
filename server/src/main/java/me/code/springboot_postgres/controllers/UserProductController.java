package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.models.entities.Product;
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
    public ResponseEntity<Product> addProduct(@AuthenticationPrincipal User user, @RequestBody ProductDTO dto) {
        var result = userProductService.addProductByUser(user, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Product>> getMyProducts(@AuthenticationPrincipal User user) {
        var result = productService.getProductsBySellerId(user.getId());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/edit/{productId}")
    public ResponseEntity<Success> editMyProduct(@AuthenticationPrincipal User user, @PathVariable String productId, @RequestBody ProductDTO dto) {
        var result = userProductService.editOwnProduct(user, productId, dto);
        return result.toResponseEntity();
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Success> deleteMyProduct(@AuthenticationPrincipal User user, @PathVariable String productId) {
        var result = userProductService.deleteOwnProduct(user, productId);
        return result.toResponseEntity();
    }
}
