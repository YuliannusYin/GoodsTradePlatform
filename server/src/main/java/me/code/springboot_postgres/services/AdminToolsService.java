package me.code.springboot_postgres.services;

import jakarta.transaction.Transactional;
import me.code.springboot_postgres.dtos.requests.ProductDTO;
import me.code.springboot_postgres.dtos.responses.entities.UserOrderDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
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

    @Transactional
    public List<UserOrderDTO> getAllUsersOrders() {
        return findAllUsersOrders().stream()
                .map(order -> new UserOrderDTO(order, order.getUser().getEmail()))
                .toList();
    }

    @Transactional
    public List<UserOrderDTO> getAllUsersOrders(String status) {
        if (isValidOrderStatus(status)) {
            return findAllUsersOrders(status).stream()
                    .map(order -> new UserOrderDTO(order, order.getUser().getEmail()))
                    .toList();
        } else throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, status + " is invalid");
    }

    public boolean isValidOrderStatus(String status) {
        return status.equalsIgnoreCase(Order.Status.PENDING.toString()) ||
                status.equalsIgnoreCase(Order.Status.SHIPPED.toString()) ||
                status.equalsIgnoreCase(Order.Status.DELIVERED.toString());
    }

    public List<Order> findAllUsersOrders() {
        return orderRepository.findAllUsersOrders();
    }

    public List<Order> findAllUsersOrders(String status) {
        Order.Status statusEnum = Order.Status.valueOf(status.toUpperCase());
        return orderRepository.findAllUsersOrdersByStatus(statusEnum);
    }

    @Transactional
    public Success sendOrder(String orderId, String dateAndTime) {
        try {
            Order order = findOrder(orderId);
            LocalDateTime expectedDelivery = generateLocalDateTime(dateAndTime);
            order.setStatus(Order.Status.SHIPPED);
            order.setExpectedDelivery(expectedDelivery);
            orderRepository.save(order);
            return new Success(HttpStatus.OK, "Order was successfully sent");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    private LocalDateTime generateLocalDateTime(String dateAndTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        return LocalDateTime.parse(dateAndTime, dateTimeFormatter);
    }

    @Transactional
    public Success changeExpectedDelivery(String orderId, String newDateAndTime) {
        try {
            Order order = findOrder(orderId);
            LocalDateTime newExpectedDelivery = generateLocalDateTime(newDateAndTime);
            order.setExpectedDelivery(newExpectedDelivery);
            orderRepository.save(order);
            return new Success(HttpStatus.OK, "Successfully updated expected delivery");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    public Order findOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find order with id: " + orderId));
    }

    public Product addProduct(ProductDTO dto) {
        try {
            Product.Condition condition = dto.condition() != null
                    ? Product.Condition.valueOf(dto.condition())
                    : Product.Condition.NEW;
            Product.Category category = Product.Category.valueOf(dto.category());

            Product product = new Product(
                    dto.name(), dto.description(), dto.imageUrls(),
                    dto.price(), dto.quantity(), category, condition,
                    dto.source() != null ? dto.source() : "PLATFORM");
            product.setStatus(Product.Status.APPROVED);
            return productRepository.save(product);
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not create product");
        }
    }

    public Success deleteProduct(String productId) {
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            return new Success(HttpStatus.OK, "The product was deleted successfully");
        } else {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not delete product");
        }
    }

    public Success editProduct(String productId, ProductDTO dto) {
        if (productRepository.existsById(productId)) {
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
            return new Success(HttpStatus.OK, "The product was edited successfully");
        } else throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Failed to edit the product");
    }

    // ==================== Product Review ====================

    public List<Product> getPendingProducts() {
        return productRepository.findByStatus(Product.Status.PENDING);
    }

    public List<Product> getProductsByStatus(String status) {
        Product.Status statusEnum = Product.Status.valueOf(status.toUpperCase());
        return productRepository.findByStatus(statusEnum);
    }

    @Transactional
    public Success approveProduct(String productId) {
        Product product = loadProductById(productId);
        if (product.getStatus() != Product.Status.PENDING) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only pending products can be approved");
        }
        product.setStatus(Product.Status.APPROVED);
        product.setRejectReason(null);
        productRepository.save(product);
        return new Success(HttpStatus.OK, "Product approved successfully");
    }

    @Transactional
    public Success rejectProduct(String productId, String rejectReason) {
        Product product = loadProductById(productId);
        if (product.getStatus() != Product.Status.PENDING) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only pending products can be rejected");
        }
        product.setStatus(Product.Status.REJECTED);
        product.setRejectReason(rejectReason);
        productRepository.save(product);
        return new Success(HttpStatus.OK, "Product rejected successfully");
    }

    @Transactional
    public Success disableProduct(String productId) {
        Product product = loadProductById(productId);
        product.setStatus(Product.Status.DISABLED);
        productRepository.save(product);
        return new Success(HttpStatus.OK, "Product disabled successfully");
    }

    @Transactional
    public Success enableProduct(String productId) {
        Product product = loadProductById(productId);
        if (product.getStatus() == Product.Status.DISABLED) {
            product.setStatus(Product.Status.APPROVED);
            productRepository.save(product);
            return new Success(HttpStatus.OK, "Product re-enabled successfully");
        }
        throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Only disabled products can be re-enabled");
    }

    public Product loadProductById(String productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product with id: " + productId + " not found"));
    }
}
