/**
 * @file AdminToolsController.java
 * @description 管理员工具控制器，提供商品管理（增删改查、审核）和订单管理（查看、发货）的接口
 * @input 商品DTO、订单发货DTO、路径参数等
 * @output 统一API响应包装的结果
 */
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

/**
 * 管理员工具控制器
 * 职责：处理管理员对商品的增删改查、审核操作，以及对订单的查看和发货操作
 */
@RestController
@RequestMapping("api/admin_tools")
public class AdminToolsController {
    private final AdminToolsService adminToolsService;

    @Autowired
    public AdminToolsController(AdminToolsService adminToolsService) {
        this.adminToolsService = adminToolsService;
    }

    /**
     * 添加新商品（管理员直接添加，状态为已审核）
     * @param dto 商品请求数据
     * @return 创建的商品信息
     */
    @PostMapping("/product/add")
    public ResponseEntity<ApiResponse<me.code.springboot_postgres.dtos.responses.ProductDTO>> addProduct(@RequestBody ProductDTO dto) {
        return ApiResponse.ok("Product added", adminToolsService.addProduct(dto)).toResponseEntity();
    }

    /**
     * 编辑指定商品信息
     * @param productId 商品ID
     * @param dto 商品更新数据
     * @return 操作结果
     */
    @PutMapping("/product/edit/{productId}")
    public ResponseEntity<ApiResponse<Void>> editProduct(@PathVariable String productId, @RequestBody ProductDTO dto) {
        return adminToolsService.editProduct(productId, dto).toResponseEntity();
    }

    /**
     * 删除指定商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/product/delete/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productId) {
        return adminToolsService.deleteProduct(productId).toResponseEntity();
    }

    /**
     * 获取待审核商品列表
     * @return 待审核商品列表
     */
    @GetMapping("/product/pending")
    public ResponseEntity<ApiResponse<List<me.code.springboot_postgres.dtos.responses.ProductDTO>>> getPendingProducts() {
        return ApiResponse.ok("Pending products retrieved", adminToolsService.getPendingProducts()).toResponseEntity();
    }

    /**
     * 根据状态获取商品列表
     * @param status 商品状态
     * @return 指定状态的商品列表
     */
    @GetMapping("/product/status/{status}")
    public ResponseEntity<ApiResponse<List<me.code.springboot_postgres.dtos.responses.ProductDTO>>> getProductsByStatus(@PathVariable String status) {
        return ApiResponse.ok("Products by status retrieved", adminToolsService.getProductsByStatus(status)).toResponseEntity();
    }

    /**
     * 审核通过指定商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @PatchMapping("/product/approve/{productId}")
    public ResponseEntity<ApiResponse<Void>> approveProduct(@PathVariable String productId) {
        return adminToolsService.approveProduct(productId).toResponseEntity();
    }

    /**
     * 驳回指定商品并填写驳回原因
     * @param productId 商品ID
     * @param rejectReason 驳回原因
     * @return 操作结果
     */
    @PatchMapping("/product/reject/{productId}")
    public ResponseEntity<ApiResponse<Void>> rejectProduct(@PathVariable String productId, @RequestParam String rejectReason) {
        return adminToolsService.rejectProduct(productId, rejectReason).toResponseEntity();
    }

    /**
     * 禁用指定商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @PatchMapping("/product/disable/{productId}")
    public ResponseEntity<ApiResponse<Void>> disableProduct(@PathVariable String productId) {
        return adminToolsService.disableProduct(productId).toResponseEntity();
    }

    /**
     * 重新启用被禁用的商品
     * @param productId 商品ID
     * @return 操作结果
     */
    @PatchMapping("/product/enable/{productId}")
    public ResponseEntity<ApiResponse<Void>> enableProduct(@PathVariable String productId) {
        return adminToolsService.enableProduct(productId).toResponseEntity();
    }

    /**
     * 获取所有用户的所有订单
     * @return 所有用户订单列表
     */
    @GetMapping("/order/all")
    public ResponseEntity<ApiResponse<List<UserOrderDTO>>> getAllUsersOrders() {
        return ApiResponse.ok("All orders retrieved", adminToolsService.getAllUsersOrders()).toResponseEntity();
    }

    /**
     * 根据状态获取所有用户的订单
     * @param status 订单状态
     * @return 指定状态的订单列表
     */
    @GetMapping("/order/all/{status}")
    public ResponseEntity<ApiResponse<List<UserOrderDTO>>> getAllUsersOrders(@PathVariable String status) {
        return ApiResponse.ok("Orders by status retrieved", adminToolsService.getAllUsersOrders(status)).toResponseEntity();
    }

    /**
     * 发货并设置预计送达时间
     * @param dto 包含订单ID和预计送达时间的数据
     * @return 操作结果
     */
    @PatchMapping("/order/send")
    public ResponseEntity<ApiResponse<Void>> sendOrder(@RequestBody OrderDeliveryDTO dto) {
        return adminToolsService.sendOrder(dto.orderId(), dto.expectedDelivery()).toResponseEntity();
    }

    /**
     * 修改订单的预计送达时间
     * @param dto 包含订单ID和新预计送达时间的数据
     * @return 操作结果
     */
    @PatchMapping("/order/expected_delivery")
    public ResponseEntity<ApiResponse<Void>> changeExpectedDelivery(@RequestBody OrderDeliveryDTO dto) {
        return adminToolsService.changeExpectedDelivery(dto.orderId(), dto.expectedDelivery()).toResponseEntity();
    }
}
