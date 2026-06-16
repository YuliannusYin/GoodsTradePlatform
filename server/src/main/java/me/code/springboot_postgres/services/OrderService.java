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
import me.code.springboot_postgres.models.entities.CommissionConfig;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.OrderItem;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.CommissionConfigRepository;
import me.code.springboot_postgres.repositories.OrderRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final UserRepository userRepository;
    private final CartService cartService;
    private final CommissionConfigRepository commissionConfigRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductService productService,
                        OrderItemService orderItemService, UserRepository userRepository,
                        CartService cartService, CommissionConfigRepository commissionConfigRepository) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.commissionConfigRepository = commissionConfigRepository;
    }

    /**
     * 提交订单：验证库存、校验余额、扣减余额、商户结算、扣减库存、创建订单、清空购物车
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
    @SuppressWarnings("null")
    public ApiResponse<Void> placeOrder(User user, String[] productIds, String receiverName,
                                        String receiverPhone, String region, String detailAddress,
                                        Order.DeliveryMethod deliveryMethod) {
        // 重新从数据库加载用户，获取当前事务中的托管实体（避免脱管实体导致持久化异常和余额变更丢失）
        User managedUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "用户不存在"));

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

        // 扣减用户余额（包含余额不足校验），并持久化余额变更
        managedUser.deductBalance(orderPrice);
        userRepository.save(managedUser);

        // 商户结算：按卖家分组，将货款（扣除佣金后）入账到各卖家余额
        settleSellers(items);

        // 扣减商品库存
        productService.updateProductQuantities(items);

        // 创建订单并关联订单项（使用托管用户实体）
        Order order = new Order(managedUser, items, receiverName, receiverPhone, region, detailAddress, deliveryMethod);
        items.forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        // 清空用户购物车（后端侧同步清空，防止重复下单）
        cartService.clearCart(managedUser.getId());

        return ApiResponse.ok("The order was placed successfully");
    }

    /**
     * 商户结算：按卖家分组计算各卖家应收金额，扣除平台佣金后入账
     * 佣金配置从数据库读取，若无配置则默认5%百分比佣金
     * @param items 订单项列表
     */
    @SuppressWarnings("null")
    private void settleSellers(List<OrderItem> items) {
        // 加载佣金配置，若无则使用默认5%百分比佣金
        CommissionConfig config = commissionConfigRepository.findById("1")
                .orElseGet(() -> {
                    // 首次使用时创建默认配置（5%百分比佣金）
                    CommissionConfig defaultConfig = new CommissionConfig();
                    return commissionConfigRepository.save(defaultConfig);
                });

        // 按卖家分组统计各卖家的应收货款
        Map<User, BigDecimal> sellerAmounts = new java.util.HashMap<>();
        for (OrderItem item : items) {
            User seller = item.getProduct().getSeller();
            if (seller != null) {
                // 累加该卖家的订单项金额
                sellerAmounts.merge(seller, item.getPrice(), BigDecimal::add);
            }
        }

        // 逐个卖家结算：扣除佣金后入账
        for (Map.Entry<User, BigDecimal> entry : sellerAmounts.entrySet()) {
            User seller = entry.getKey();
            BigDecimal sellerRevenue = entry.getValue();

            // 计算平台佣金
            BigDecimal commission = config.calculateCommission(sellerRevenue)
                    .setScale(2, RoundingMode.HALF_UP);
            // 卖家实收金额 = 货款 - 佣金
            BigDecimal sellerIncome = sellerRevenue.subtract(commission)
                    .setScale(2, RoundingMode.HALF_UP);

            // 确保卖家实收金额不为负
            if (sellerIncome.compareTo(BigDecimal.ZERO) > 0) {
                // 重新加载卖家实体获取托管对象，避免脱管实体问题
                User managedSeller = userRepository.findById(seller.getId()).orElse(null);
                if (managedSeller != null) {
                    managedSeller.addBalance(sellerIncome);
                    userRepository.save(managedSeller);
                }
            }
        }
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
