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

    @Transactional
    public ApiResponse<Void> placeOrder(User user, String[] productIds, String address,
                                        Order.DeliveryMethod deliveryMethod, Order.PaymentMethod paymentMethod) {
        List<Product> products = productService.loadProductsById(productIds);
        List<OrderItem> items = orderItemService.generateOrderItems(products);
        List<UnavailableProductDTO> unavailableProducts = productService.findUnavailableProducts(items);

        if (!unavailableProducts.isEmpty()) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not place order",
                    Map.of("unavailableProducts", unavailableProducts));
        }

        productService.updateProductQuantities(items);

        Order order = new Order(user, items, address, deliveryMethod, paymentMethod);
        items.forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        return ApiResponse.ok("The order was placed successfully");
    }

    @Transactional(readOnly = true)
    public OngoingOrderDTO getOngoingOrder(String[] productIds) {
        List<Product> products = productService.loadProductsById(productIds);
        List<OrderItem> items = orderItemService.generateOrderItems(products);
        List<OrderDTO.OrderItemDTO> itemDTOs = items.stream().map(OrderDTO.OrderItemDTO::from).toList();
        BigDecimal totalPrice = orderItemService.getTotalPrice(items);
        return new OngoingOrderDTO(itemDTOs, totalPrice);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(String userId) {
        return orderRepository.findOrdersByUserId(userId).stream()
                .map(OrderDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<Order.DeliveryMethod> getAvailableDeliveryMethods() {
        return Arrays.stream(Order.DeliveryMethod.values()).toList();
    }

    @Transactional(readOnly = true)
    public List<Order.PaymentMethod> getAvailablePaymentMethods() {
        return Arrays.stream(Order.PaymentMethod.values()).toList();
    }
}
