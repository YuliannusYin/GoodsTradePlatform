package me.code.springboot_postgres.services;

import jakarta.transaction.Transactional;
import me.code.springboot_postgres.dtos.requests.AddProductDTO;
import me.code.springboot_postgres.dtos.requests.EditedProductDTO;
import me.code.springboot_postgres.dtos.responses.entities.UserOrderDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.Order;
import me.code.springboot_postgres.models.entities.Product;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.OrderRepository;
import me.code.springboot_postgres.repositories.ProductRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AdminToolsService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public AdminToolsService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public List<UserOrderDTO> getAllUsersOrders() {
        return findAllUsersOrders().stream()
                .map(order -> new UserOrderDTO(order, order.getUser().getEmail()))
                .toList();
    }

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
        List<Order> orders = orderRepository.findAllUsersOrders();
        if (orders.isEmpty()) {
            throw new CustomRuntimeException(
                    HttpStatus.NOT_FOUND,
                    "Could not find all users orders");
        }
        return orders;
    }

    public List<Order> findAllUsersOrders(String status) {
        Order.Status statusEnum = Order.Status.valueOf(status.toUpperCase());
        List<Order> orders = orderRepository.findAllUsersOrdersByStatus(statusEnum);
        if (orders.isEmpty()) {
            throw new CustomRuntimeException(
                    HttpStatus.NOT_FOUND,
                    "Could not find all users orders");
        }
        return orders;
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
                () -> new CustomRuntimeException(
                        HttpStatus.NOT_FOUND,
                        "Could not find order with id: " + orderId));
    }

    public Product addProduct(AddProductDTO dto) {
        try {
            Product.Condition condition = dto.condition() != null
                    ? Product.Condition.valueOf(dto.condition())
                    : Product.Condition.NEW;
            Product.Category category = Product.Category.valueOf(dto.category());

            Product product = new Product(
                    dto.name(),
                    dto.description(),
                    dto.imageUrls(),
                    dto.price(),
                    dto.quantity(),
                    category,
                    condition,
                    dto.source() != null ? dto.source() : "PLATFORM");
            return productRepository.save(product);
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not create product");
        }
    }

    public Product addProductByUser(User user, AddProductDTO dto) {
        try {
            Product.Condition condition = dto.condition() != null
                    ? Product.Condition.valueOf(dto.condition())
                    : Product.Condition.NEW;
            Product.Category category = Product.Category.valueOf(dto.category());

            Product product = new Product(
                    dto.name(),
                    dto.description(),
                    dto.imageUrls(),
                    dto.price(),
                    dto.quantity(),
                    category,
                    condition,
                    "USER");

            product.setSeller(user);
            return productRepository.save(product);
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not create product");
        }
    }

    public Success deleteProduct(String productId) {
        if (isValidProductID(productId)) {
            productRepository.deleteById(productId);
            return new Success(HttpStatus.OK, "The product was deleted successfully");
        } else {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not delete product");
        }
    }

    private boolean isValidProductID(String product_id) {
        return productRepository.existsById(product_id);
    }

    public Success editProduct(String productId, EditedProductDTO dto) {
        if (isValidProductID(productId)) {
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

    public Product loadProductById(String productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Product with id: " + productId + " not found"));
    }

}
