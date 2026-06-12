package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.OrderDeliveryDTO;
import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserOrderDTO;
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

    @PostMapping("/product/add")
    public ResponseEntity<ApiResponse<me.code.springboot_postgres.dtos.responses.ProductDTO>> addProduct(@RequestBody ProductDTO dto) {
        return ApiResponse.ok("Product added", adminToolsService.addProduct(dto)).toResponseEntity();
    }

    @PutMapping("/product/edit/{productId}")
    public ResponseEntity<ApiResponse<Void>> editProduct(@PathVariable String productId, @RequestBody ProductDTO dto) {
        return adminToolsService.editProduct(productId, dto).toResponseEntity();
    }

    @DeleteMapping("/product/delete/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productId) {
        return adminToolsService.deleteProduct(productId).toResponseEntity();
    }

    @GetMapping("/product/pending")
    public ResponseEntity<ApiResponse<List<me.code.springboot_postgres.dtos.responses.ProductDTO>>> getPendingProducts() {
        return ApiResponse.ok("Pending products retrieved", adminToolsService.getPendingProducts()).toResponseEntity();
    }

    @GetMapping("/product/status/{status}")
    public ResponseEntity<ApiResponse<List<me.code.springboot_postgres.dtos.responses.ProductDTO>>> getProductsByStatus(@PathVariable String status) {
        return ApiResponse.ok("Products by status retrieved", adminToolsService.getProductsByStatus(status)).toResponseEntity();
    }

    @PatchMapping("/product/approve/{productId}")
    public ResponseEntity<ApiResponse<Void>> approveProduct(@PathVariable String productId) {
        return adminToolsService.approveProduct(productId).toResponseEntity();
    }

    @PatchMapping("/product/reject/{productId}")
    public ResponseEntity<ApiResponse<Void>> rejectProduct(@PathVariable String productId, @RequestParam String rejectReason) {
        return adminToolsService.rejectProduct(productId, rejectReason).toResponseEntity();
    }

    @PatchMapping("/product/disable/{productId}")
    public ResponseEntity<ApiResponse<Void>> disableProduct(@PathVariable String productId) {
        return adminToolsService.disableProduct(productId).toResponseEntity();
    }

    @PatchMapping("/product/enable/{productId}")
    public ResponseEntity<ApiResponse<Void>> enableProduct(@PathVariable String productId) {
        return adminToolsService.enableProduct(productId).toResponseEntity();
    }

    @GetMapping("/order/all")
    public ResponseEntity<ApiResponse<List<UserOrderDTO>>> getAllUsersOrders() {
        return ApiResponse.ok("All orders retrieved", adminToolsService.getAllUsersOrders()).toResponseEntity();
    }

    @GetMapping("/order/all/{status}")
    public ResponseEntity<ApiResponse<List<UserOrderDTO>>> getAllUsersOrders(@PathVariable String status) {
        return ApiResponse.ok("Orders by status retrieved", adminToolsService.getAllUsersOrders(status)).toResponseEntity();
    }

    @PatchMapping("/order/send")
    public ResponseEntity<ApiResponse<Void>> sendOrder(@RequestBody OrderDeliveryDTO dto) {
        return adminToolsService.sendOrder(dto.orderId(), dto.expectedDelivery()).toResponseEntity();
    }

    @PatchMapping("/order/expected_delivery")
    public ResponseEntity<ApiResponse<Void>> changeExpectedDelivery(@RequestBody OrderDeliveryDTO dto) {
        return adminToolsService.changeExpectedDelivery(dto.orderId(), dto.expectedDelivery()).toResponseEntity();
    }
}
