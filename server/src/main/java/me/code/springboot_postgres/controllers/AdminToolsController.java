package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.OrderDeliveryDTO;
import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.entities.UserOrderDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.services.AdminToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin_tools")
public class AdminToolsController {
    private final AdminToolsService adminToolsService;

    @Autowired
    public AdminToolsController(AdminToolsService adminToolsService) {
        this.adminToolsService = adminToolsService;
    }

    // ==================== Product Management ====================

    @PostMapping("/product/add")
    public ResponseEntity<Product> addProduct(@RequestBody ProductDTO dto) {
        var result = adminToolsService.addProduct(dto);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/product/edit/{productId}")
    public ResponseEntity<Success> editProduct(@PathVariable String productId, @RequestBody ProductDTO dto) {
        var result = adminToolsService.editProduct(productId, dto);
        return result.toResponseEntity();
    }

    @DeleteMapping("/product/delete/{productId}")
    public ResponseEntity<Success> deleteProduct(@PathVariable String productId) {
        var result = adminToolsService.deleteProduct(productId);
        return result.toResponseEntity();
    }

    // ==================== Product Review ====================

    @GetMapping("/product/pending")
    public ResponseEntity<List<Product>> getPendingProducts() {
        return ResponseEntity.ok(adminToolsService.getPendingProducts());
    }

    @GetMapping("/product/status/{status}")
    public ResponseEntity<List<Product>> getProductsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(adminToolsService.getProductsByStatus(status));
    }

    @PatchMapping("/product/approve/{productId}")
    public ResponseEntity<Success> approveProduct(@PathVariable String productId) {
        return adminToolsService.approveProduct(productId).toResponseEntity();
    }

    @PatchMapping("/product/reject/{productId}")
    public ResponseEntity<Success> rejectProduct(@PathVariable String productId, @RequestParam String rejectReason) {
        return adminToolsService.rejectProduct(productId, rejectReason).toResponseEntity();
    }

    @PatchMapping("/product/disable/{productId}")
    public ResponseEntity<Success> disableProduct(@PathVariable String productId) {
        return adminToolsService.disableProduct(productId).toResponseEntity();
    }

    @PatchMapping("/product/enable/{productId}")
    public ResponseEntity<Success> enableProduct(@PathVariable String productId) {
        return adminToolsService.enableProduct(productId).toResponseEntity();
    }

    // ==================== Order Management ====================

    @GetMapping("/order/all")
    public ResponseEntity<List<UserOrderDTO>> getAllUsersOrders() {
        var result = adminToolsService.getAllUsersOrders();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/order/all/{status}")
    public ResponseEntity<List<UserOrderDTO>> getAllUsersOrders(@PathVariable String status) {
        var result = adminToolsService.getAllUsersOrders(status);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/order/send")
    public ResponseEntity<Success> sendOrder(@RequestBody OrderDeliveryDTO dto) {
        var result = adminToolsService.sendOrder(dto.orderId(), dto.expectedDelivery());
        return result.toResponseEntity();
    }

    @PatchMapping("/order/expected_delivery")
    public ResponseEntity<Success> changeExpectedDelivery(@RequestBody OrderDeliveryDTO dto) {
        var result = adminToolsService.changeExpectedDelivery(dto.orderId(), dto.expectedDelivery());
        return result.toResponseEntity();
    }
}
