/**
 * @file OrderController.java
 * @description 订单控制器，提供下单、查询订单、获取进行中订单和配送/支付方式等接口
 * @input 认证用户信息、商品ID数组、下单DTO
 * @output 统一API响应包装的订单数据
 */
package me.code.springboot_postgres.controllers;

import jakarta.validation.Valid;
import me.code.springboot_postgres.dtos.requests.PlaceOrderDTO;
import me.code.springboot_postgres.dtos.responses.*;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * 职责：处理用户下单、查询订单、获取进行中订单预览以及配送和支付方式列表
 */
@RestController
@RequestMapping("api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取进行中订单预览（未提交前的订单详情和总价）
     * @param productIds 商品ID数组
     * @return 进行中订单数据
     */
    @PostMapping("/ongoing")
    public ResponseEntity<ApiResponse<OngoingOrderDTO>> getOngoingOrder(@RequestBody String[] productIds) {
        return ApiResponse.ok("订单预览获取成功", orderService.getOngoingOrder(productIds)).toResponseEntity();
    }

    /**
     * 提交订单（下单），使用余额支付
     * @param user 当前认证用户
     * @param dto 下单请求数据
     * @return 操作结果
     */
    @PostMapping("/place")
    public ResponseEntity<ApiResponse<String>> placeOrder(@AuthenticationPrincipal User user, @Valid @RequestBody PlaceOrderDTO dto) {
        return orderService.placeOrder(user, dto.productIds(),
                dto.receiverName(), dto.receiverPhone(), dto.region(), dto.detailAddress(),
                dto.deliveryMethod()).toResponseEntity();
    }

    /**
     * 获取当前用户的所有订单
     * @param user 当前认证用户
     * @return 订单列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Orders retrieved", orderService.getUserOrders(user.getId())).toResponseEntity();
    }

    /**
     * 获取可用的配送方式列表
     * @return 配送方式枚举列表
     */
    @GetMapping("/delivery/methods")
    public ResponseEntity<ApiResponse<List<Order.DeliveryMethod>>> getAvailableDeliveryMethods() {
        return ApiResponse.ok("Delivery methods retrieved", orderService.getAvailableDeliveryMethods()).toResponseEntity();
    }

    /**
     * 获取可用的支付方式列表
     * @return 支付方式枚举列表
     */
    @GetMapping("/payment/methods")
    public ResponseEntity<ApiResponse<List<Order.PaymentMethod>>> getAvailablePaymentMethods() {
        return ApiResponse.ok("支付方式获取成功", orderService.getAvailablePaymentMethods()).toResponseEntity();
    }
}
