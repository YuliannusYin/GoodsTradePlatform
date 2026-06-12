package me.code.springboot_postgres.services;

import jakarta.transaction.Transactional;
import me.code.springboot_postgres.dtos.responses.entities.OngoingOrderDTO;
import me.code.springboot_postgres.dtos.responses.entities.PlacedOrderDTO;
import me.code.springboot_postgres.dtos.responses.entities.UnavailableProductDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.OrderItem;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    public OrderService(
            OrderRepository orderRepository,
            ProductService productService,
            OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    @Transactional
    public Success placeOrder(
            User user,
            String[] productIds,
            String address,
            Order.DeliveryMethod deliveryMethod,
            Order.PaymentMethod paymentMethod) {
        try {
            List<Product> products =
                    productService.loadProductsById(productIds);

            List<OrderItem> items =
                    orderItemService.generateOrderItems(products);

            List<UnavailableProductDTO> unavailableProducts =
                    productService.findUnavailableProducts(items);

            if (hasUnavailableProducts(unavailableProducts)) {
                throw new CustomRuntimeException(
                        HttpStatus.BAD_REQUEST,
                        "Could not place order",
                        Map.of("unavailableProducts", unavailableProducts));
            }

            productService.updateProductQuantities(items);

            Order order = new Order(user, items, address, deliveryMethod, paymentMethod);
            items.forEach(item -> item.setOrder(order));
            orderRepository.save(order);

            return new Success(
                    HttpStatus.OK,
                    "The order was placed successfully");

        } catch (CustomRuntimeException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new CustomRuntimeException(
                    HttpStatus.BAD_REQUEST,
                    "Could not place order");
        }
    }

    private boolean hasUnavailableProducts(List<UnavailableProductDTO> unavailableProductDTOS) {
        return !unavailableProductDTOS.isEmpty();
    }

    public OngoingOrderDTO getOngoingOrder(String[] productIds) {
        try {
            List<Product> products =
                    productService.loadProductsById(productIds);

            List<OrderItem> items =
                    orderItemService.generateOrderItems(products);

            BigDecimal totalPrice =
                    orderItemService.getTotalPrice(items);

            return new OngoingOrderDTO(items, totalPrice);

        } catch (Exception exception) {
            throw new CustomRuntimeException(
                    HttpStatus.BAD_REQUEST,
                    "Could not retrieve ongoing order");
        }
    }

    @Transactional
    public List<PlacedOrderDTO> getUserOrders(String userId) {
        return findOrdersByUserId(userId).stream()
                .map(PlacedOrderDTO::new)
                .toList();
    }

    public List<Order> findOrdersByUserId(String userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    public List<Order.DeliveryMethod> getAvailableDeliveryMethods() {
        return Arrays.stream(Order.DeliveryMethod.values()).toList();
    }

    public List<Order.PaymentMethod> getAvailablePaymentMethods() {
        return Arrays.stream(Order.PaymentMethod.values()).toList();
    }

}
