package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.AddProductDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.AdminToolsService;
import me.code.springboot_postgres.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user_products")
public class UserProductController {

    private final AdminToolsService adminToolsService;
    private final ProductService productService;

    @Autowired
    public UserProductController(AdminToolsService adminToolsService, ProductService productService) {
        this.adminToolsService = adminToolsService;
        this.productService = productService;
    }

    @PostMapping("/add")
    public ResponseEntity<Product> addProduct(@AuthenticationPrincipal User user, @RequestBody AddProductDTO dto) {
        var result = adminToolsService.addProductByUser(user, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Product>> getMyProducts(@AuthenticationPrincipal User user) {
        var result = productService.getProductsBySellerId(user.getId());
        return ResponseEntity.ok(result);
    }
}
