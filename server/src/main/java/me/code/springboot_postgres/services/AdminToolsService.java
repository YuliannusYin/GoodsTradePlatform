package me.code.springboot_postgres.services;

import org.springframework.transaction.annotation.Transactional;
import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserOrderDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.repositories.OrderRepository;
import me.code.springboot_postgres.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AdminToolsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public AdminToolsService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<UserOrderDTO> getAllUsersOrders() {
        return orderRepository.findAllUsersOrders().stream()
                .map(UserOrderDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserOrderDTO> getAllUsersOrders(String status) {
        if (!isValidOrderStatus(status)) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, status + " is invalid");
        }
        Order.Status statusEnum = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findAllUsersOrdersByStatus(statusEnum).stream()
                .map(UserOrderDTO::from).toList();
    }

    public boolean isValidOrderStatus(String status) {
        try {
            Order.Status.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional
    public ApiResponse<Void> sendOrder(String orderId, String dateAndTime) {
        Order order = findOrder(orderId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        LocalDateTime expectedDelivery = LocalDateTime.parse(dateAndTime, formatter);
        order.setStatus(Order.Status.SHIPPED);
        order.setExpectedDelivery(expectedDelivery);
        orderRepository.save(order);
        return ApiResponse.ok("Order was successfully sent");
    }

    @Transactional
    public ApiResponse<Void> changeExpectedDelivery(String orderId, String newDateAndTime) {
        Order order = findOrder(orderId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        LocalDateTime newExpectedDelivery = LocalDateTime.parse(newDateAndTime, formatter);
        order.setExpectedDelivery(newExpectedDelivery);
        orderRepository.save(order);
        return ApiResponse.ok("Successfully updated expected delivery");
    }

    @Transactional(readOnly = true)
    public Order findOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find order with id: " + orderId));
    }

    @Transactional
    public me.code.springboot_postgres.dtos.responses.ProductDTO addProduct(ProductDTO dto) {
        Product.Condition condition = dto.condition() != null
                ? Product.Condition.valueOf(dto.condition())
                : Product.Condition.NEW;
        Product.Category category = Product.Category.valueOf(dto.category());

        Product product = new Product(
                dto.name(), dto.description(), dto.imageUrls(),
                dto.price(), dto.quantity(), category, condition,
                dto.source() != null ? dto.source() : "PLATFORM");
        product.setStatus(Product.Status.APPROVED);
        return me.code.springboot_postgres.dtos.responses.ProductDTO.from(productRepository.save(product));
    }

    @Transactional
    public ApiResponse<Void> deleteProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
        return ApiResponse.ok("The product was deleted successfully");
    }

    @Transactional
    public ApiResponse<Void> editProduct(String productId, ProductDTO dto) {
        Product product = loadProductById(productId);
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setImageUrls(dto.imageUrls());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());
        product.setCategory(Product.Category.valueOf(dto.category()));
        if (dto.condition() != null) {
            product.setCondition(Product.Condition.valueOf(dto.condition()));
        }
        product.setSource(dto.source());
        productRepository.save(product);
        return ApiResponse.ok("The product was edited successfully");
    }

    @Transactional(readOnly = true)
    public List<me.code.springboot_postgres.dtos.responses.ProductDTO> getPendingProducts() {
        return productRepository.findByStatus(Product.Status.PENDING).stream()
                .map(me.code.springboot_postgres.dtos.responses.ProductDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<me.code.springboot_postgres.dtos.responses.ProductDTO> getProductsByStatus(String status) {
        Product.Status statusEnum = Product.Status.valueOf(status.toUpperCase());
        return productRepository.findByStatus(statusEnum).stream()
                .map(me.code.springboot_postgres.dtos.responses.ProductDTO::from).toList();
    }

    @Transactional
    public ApiResponse<Void> approveProduct(String productId) {
        Product product = loadProductById(productId);
        if (product.getStatus() != Product.Status.PENDING) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only pending products can be approved");
        }
        product.setStatus(Product.Status.APPROVED);
        product.setRejectReason(null);
        productRepository.save(product);
        return ApiResponse.ok("Product approved successfully");
    }

    @Transactional
    public ApiResponse<Void> rejectProduct(String productId, String rejectReason) {
        Product product = loadProductById(productId);
        if (product.getStatus() != Product.Status.PENDING) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only pending products can be rejected");
        }
        product.setStatus(Product.Status.REJECTED);
        product.setRejectReason(rejectReason);
        productRepository.save(product);
        return ApiResponse.ok("Product rejected successfully");
    }

    @Transactional
    public ApiResponse<Void> disableProduct(String productId) {
        Product product = loadProductById(productId);
        product.setStatus(Product.Status.DISABLED);
        productRepository.save(product);
        return ApiResponse.ok("Product disabled successfully");
    }

    @Transactional
    public ApiResponse<Void> enableProduct(String productId) {
        Product product = loadProductById(productId);
        if (product.getStatus() == Product.Status.DISABLED) {
            product.setStatus(Product.Status.APPROVED);
            productRepository.save(product);
            return ApiResponse.ok("Product re-enabled successfully");
        }
        throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only disabled products can be re-enabled");
    }

    @Transactional(readOnly = true)
    public Product loadProductById(String productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product with id: " + productId + " not found"));
    }
}
