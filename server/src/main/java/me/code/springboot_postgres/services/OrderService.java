/**
 * @file OrderService.java
 * @description 订单服务类，提供下单、查询订单、获取进行中订单和配送/支付方式的业务逻辑
 * @input 用户实体、商品ID数组、收货人信息、配送方式
 * @output 操作结果、订单DTO列表或进行中订单DTO
 */
package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.OngoingOrderDTO;
import me.code.springboot_postgres.dtos.responses.OrderDTO;
import me.code.springboot_postgres.dtos.responses.UnavailableProductDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.OrderItem;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 订单服务类
 * 职责：处理用户下单、查询订单、获取进行中订单预览以及配送和支付方式列表等业务逻辑
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductService productService, OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    /**
     * 提交订单：验证库存、校验余额、扣减余额、扣减库存、创建订单
     * 支付方式固定为余额支付
     * @param user 下单用户
     * @param productIds 商品ID数组
     * @param receiverName 收货人姓名
     * @param receiverPhone 联系电话
     * @param region 省/市/区
     * @param detailAddress 详细地址
     * @param deliveryMethod 配送方式
     * @return 操作结果
     */
    @Transactional
    public ApiResponse<Void> placeOrder(User user, String[] productIds, String receiverName,
                                        String receiverPhone, String region, String detailAddress,
                                        Order.DeliveryMethod deliveryMethod) {
        // 加载商品并生成订单项
        List<Product> products = productService.loadProductsById(productIds);
        List<OrderItem> items = orderItemService.generateOrderItems(products);
        // 检查库存是否充足
        List<UnavailableProductDTO> unavailableProducts = productService.findUnavailableProducts(items);

        if (!unavailableProducts.isEmpty()) {
            // 存在不可用商品，抛出异常并附带详情
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not place order",
                    Map.of("unavailableProducts", unavailableProducts));
        }

        // 计算订单总价
        BigDecimal orderPrice = items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 扣减用户余额（包含余额不足校验）
        user.deductBalance(orderPrice);

        // 扣减商品库存
        productService.updateProductQuantities(items);

        // 创建订单并关联订单项
        Order order = new Order(user, items, receiverName, receiverPhone, region, detailAddress, deliveryMethod);
        items.forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        return ApiResponse.ok("The order was placed successfully");
    }

    /**
     * 获取进行中订单预览（未提交前的订单详情和总价）
     * @param productIds 商品ID数组
     * @return 进行中订单数据
     */
    @Transactional(readOnly = true)
    public OngoingOrderDTO getOngoingOrder(String[] productIds) {
        List<Product> products = productService.loadProductsById(productIds);
        List<OrderItem> items = orderItemService.generateOrderItems(products);
        List<OrderDTO.OrderItemDTO> itemDTOs = items.stream().map(OrderDTO.OrderItemDTO::from).toList();
        BigDecimal totalPrice = orderItemService.getTotalPrice(items);
        return new OngoingOrderDTO(itemDTOs, totalPrice);
    }

    /**
     * 获取指定用户的所有订单
     * @param userId 用户ID
     * @return 订单DTO列表
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(String userId) {
        return orderRepository.findOrdersByUserId(userId).stream()
                .map(OrderDTO::from).toList();
    }

    /**
     * 获取可用的配送方式列表
     * @return 配送方式枚举列表
     */
    @Transactional(readOnly = true)
    public List<Order.DeliveryMethod> getAvailableDeliveryMethods() {
        return Arrays.stream(Order.DeliveryMethod.values()).toList();
    }

    /**
     * 获取可用的支付方式列表
     * @return 支付方式枚举列表
     */
    @Transactional(readOnly = true)
    public List<Order.PaymentMethod> getAvailablePaymentMethods() {
        return Arrays.stream(Order.PaymentMethod.values()).toList();
    }
}
