package me.code.springboot_postgres.controllers;

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

@RestController
@RequestMapping("api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/ongoing")
    public ResponseEntity<ApiResponse<OngoingOrderDTO>> getOngoingOrder(@RequestBody String[] productIds) {
        return ApiResponse.ok("Ongoing order retrieved", orderService.getOngoingOrder(productIds)).toResponseEntity();
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<Void>> placeOrder(@AuthenticationPrincipal User user, @RequestBody PlaceOrderDTO dto) {
        return orderService.placeOrder(user, dto.productIds(), dto.address(), dto.deliveryMethod(), dto.paymentMethod()).toResponseEntity();
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Orders retrieved", orderService.getUserOrders(user.getId())).toResponseEntity();
    }

    @GetMapping("/delivery/methods")
    public ResponseEntity<ApiResponse<List<Order.DeliveryMethod>>> getAvailableDeliveryMethods() {
        return ApiResponse.ok("Delivery methods retrieved", orderService.getAvailableDeliveryMethods()).toResponseEntity();
    }

    @GetMapping("/payment/methods")
    public ResponseEntity<ApiResponse<List<Order.PaymentMethod>>> getAvailablePaymentMethods() {
        return ApiResponse.ok("Payment methods retrieved", orderService.getAvailablePaymentMethods()).toResponseEntity();
    }
}
