# Neo4j → PostgreSQL 数据库迁移实施方案

> 项目：eCommerce-Project（动漫周边电商平台）
> 源数据库：Neo4j 5 Community（图数据库）
> 目标数据库：PostgreSQL 16
> ORM 迁移：Spring Data Neo4j → Spring Data JPA + Hibernate
> 迁移工具：Flyway
> 查询方式：混合方式（方法命名 + @Query JPQL）
> 编写日期：2026-06-11

---

## 目录

1. [迁移决策摘要](#1-迁移决策摘要)
2. [迁移前准备](#2-迁移前准备)
3. [阶段一：基础设施与依赖替换](#3-阶段一基础设施与依赖替换)
4. [阶段二：数据模型重构](#4-阶段二数据模型重构)
5. [阶段三：Repository 层重写](#5-阶段三repository-层重写)
6. [阶段四：Service 层适配](#6-阶段四service-层适配)
7. [阶段五：配置与启动类修改](#7-阶段五配置与启动类修改)
8. [阶段六：数据库初始化与种子数据](#8-阶段六数据库初始化与种子数据)
9. [阶段七：Docker 与部署配置](#9-阶段七docker-与部署配置)
10. [阶段八：测试验证](#10-阶段八测试验证)
11. [迁移检查清单](#11-迁移检查清单)
12. [风险与回退策略](#12-风险与回退策略)

---

## 1. 迁移决策摘要

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 目标数据库 | PostgreSQL 16 | 功能强大，原生 JSONB，全文搜索扩展丰富 |
| imageUrls 存储 | JSON 列（JSONB） | 保持灵活性，无需额外表，与原 Neo4j 数组类似 |
| 查询方式 | 混合方式 | 简单查询用方法命名，复杂查询用 @Query JPQL |
| 迁移管理工具 | Flyway | SQL 脚本版本管理，与 Spring Boot 集成好 |
| 包名重命名 | `springboot_neo4j` → `springboot_postgres` | 消除 Neo4j 命名痕迹，反映新架构 |
| 迁移策略 | 整体替换 | Neo4j 与 JPA 注解体系不兼容，无法渐进式迁移 |

---

## 2. 迁移前准备

### 2.1 分支策略

```
main
  └── feature/migrate-to-postgresql    ← 在此分支完成所有迁移工作
```

### 2.2 备份清单

- [ ] 备份 Neo4j 数据（如有生产数据，使用 `neo4j-admin dump`）
- [ ] 记录当前所有 API 端点及其响应格式（确保前端兼容性不变）
- [ ] 导出当前 Postman/测试用例

### 2.3 文件变更范围总览

| 类别 | 文件数 | 变更类型 |
|------|--------|----------|
| Maven 依赖 | 1 | 修改 |
| 配置文件 | 2 | 修改 |
| 启动类 | 1 | 修改 |
| 模型层 | 6 | 重写 |
| Repository 层 | 5 | 重写 |
| Service 层 | ~10 | 适配 |
| 配置/种子数据 | 3 | 重写 |
| Docker 文件 | 2 | 修改 |
| Flyway 迁移脚本 | 1（新增） | 新增 |
| **合计** | **~31** | |

---

## 3. 阶段一：基础设施与依赖替换

### 3.1 修改 pom.xml

**文件**：`server/pom.xml`

**移除**：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-neo4j</artifactId>
</dependency>
```

**新增**：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

**修改 artifactId**：
```xml
<artifactId>springboot_postgres</artifactId>
<name>springboot_postgres</name>
<description>springboot_postgres</description>
```

### 3.2 修改 application.yml

**文件**：`server/src/main/resources/application.yml`

**替换为**：
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/merchandise}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: ${SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: ${JWT_SECRET:dev-only-secret-key-change-in-production-min-32-chars}
  expiration-ms: ${JWT_EXPIRATION_MS:3600000}
```

**关键说明**：
- `ddl-auto: validate`：由 Flyway 管理 Schema，Hibernate 仅做校验
- `flyway.baseline-on-migrate: true`：首次运行时自动创建基线
- 所有敏感配置支持环境变量覆盖

---

## 4. 阶段二：数据模型重构

### 4.1 包名重命名

```
me.code.springboot_neo4j.models.nodes  →  me.code.springboot_postgres.models.entities
```

所有模型类从 `models/nodes/` 目录移至 `models/entities/` 目录。

### 4.2 User 实体

**文件**：`server/src/main/java/me/code/springboot_postgres/models/entities/User.java`

```java
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> sellingProducts;

    public User(String email, String username, String password, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.toString()));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String toString() {
        return "User{id='" + id + "', email='" + email + "', username='" + username + "', role=" + role + '}';
    }

    public enum Role {
        USER, ADMIN;

        @Override
        public String toString() { return this.name(); }
    }
}
```

**变更要点**：
| 原注解 | 新注解 | 说明 |
|--------|--------|------|
| `@Node("User")` | `@Entity` + `@Table(name = "users")` | 表名用 `users` 避免与 PostgreSQL 保留字冲突 |
| `@Id` + `@GeneratedValue(UUIDStringGenerator.class)` | `@Id` + `@GeneratedValue(AUTO)` + `@UuidGenerator` | JPA 标准 UUID 生成 |
| `@Relationship(type = "SELLS", direction = OUTGOING)` | `@OneToMany(mappedBy = "seller")` | JPA 一对多关联 |

### 4.3 Product 实体

**文件**：`server/src/main/java/me/code/springboot_postgres/models/entities/Product.java`

```java
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> imageUrls;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, length = 30)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Condition condition;

    @Column(length = 20)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    public Product(String name, String description, List<String> imageUrls, double price, int quantity, String category, Condition condition, String source) {
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.condition = condition;
        this.source = source;
    }

    public enum Condition {
        NEW, LIKE_NEW, GOOD, FAIR
    }

    public enum Category {
        ANIME_FIGURE, POSTER, KEYCHAIN, BADGE, PILLOW, STAND, CLOTHING, ALBUM, ACCESSORY, OTHER
    }
}
```

**变更要点**：
| 原注解 | 新注解 | 说明 |
|--------|--------|------|
| `@Node("Product")` | `@Entity` + `@Table(name = "products")` | |
| `List<String> imageUrls` | `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")` | PostgreSQL JSONB 存储 |
| `@Relationship(type = "SOLD_BY", direction = INCOMING)` | `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "seller_id")` | 外键关联 |
| `private String category` | `@Column(length = 30)` | 原为枚举映射为字符串，保持字符串存储兼容前端 |

### 4.4 Order 实体

**文件**：`server/src/main/java/me/code/springboot_postgres/models/entities/Order.java`

```java
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryMethod deliveryMethod;

    private LocalDateTime received;

    @Column(name = "expected_delivery")
    private LocalDateTime expectedDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    public Order(User user, List<OrderItem> items, String address, DeliveryMethod deliveryMethod, PaymentMethod paymentMethod) {
        this.status = Status.PENDING;
        this.user = user;
        this.items = items;
        this.price = getTotalPrice();
        this.address = address;
        this.deliveryMethod = deliveryMethod;
        this.paymentMethod = paymentMethod;
        this.received = LocalDateTime.now();
        this.expectedDelivery = null;
    }

    public double getTotalPrice() {
        return formatPrice(items.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum());
    }

    private double formatPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }

    public enum Status { PENDING, SHIPPED, DELIVERED }
    public enum DeliveryMethod { STANDARD_DELIVERY, EXPRESS_DELIVERY }
    public enum PaymentMethod { ALIPAY, WECHAT_PAY, CASH_ON_DELIVERY }
}
```

**变更要点**：
| 原注解 | 新注解 | 说明 |
|--------|--------|------|
| `@Relationship(type = "PLACED_BY")` | `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "user_id")` | 外键关联 |
| `@Relationship(type = "INCLUDES")` | `@OneToMany(mappedBy = "order", cascade = ALL)` | 级联保存订单项 |
| `expectedDelivery` | `@Column(name = "expected_delivery")` | 显式指定蛇形命名列名 |

### 4.5 OrderItem 实体

**文件**：`server/src/main/java/me/code/springboot_postgres/models/entities/OrderItem.java`

```java
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public OrderItem(Product product, int amount) {
        this.product = product;
        this.amount = amount;
        this.price = product.getPrice() * amount;
    }
}
```

**变更要点**：
| 原注解 | 新注解 | 说明 |
|--------|--------|------|
| `@Relationship(value = "REFERS_TO", direction = OUTGOING)` | `@ManyToOne` + `@JoinColumn(name = "product_id")` | 外键关联 |
| 无 | `@ManyToOne` + `@JoinColumn(name = "order_id")` | **新增**：OrderItem 需要显式持有 Order 引用（JPA 双向关联） |

### 4.6 Favorite 实体

**文件**：`server/src/main/java/me/code/springboot_postgres/models/entities/Favorite.java`

```java
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Favorite(User user, Product product) {
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
```

**变更要点**：
- 新增 `@UniqueConstraint(columnNames = {"user_id", "product_id"})`：防止同一用户重复收藏同一商品（原 Neo4j 通过代码逻辑保证）
- Order 的构造函数中需要为每个 OrderItem 设置 `item.setOrder(this)`

### 4.7 Review 实体

**文件**：`server/src/main/java/me/code/springboot_postgres/models/entities/Review.java`

```java
package me.code.springboot_postgres.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Review(int rating, String comment, User user, Product product) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.product = product;
        this.createdAt = LocalDateTime.now();
    }
}
```

### 4.8 模型层变更汇总表

| 原类 | 原注解 | 新注解 | 关键变更 |
|------|--------|--------|----------|
| User | `@Node("User")` | `@Entity` + `@Table(name = "users")` | `@Relationship(SELLS)` → `@OneToMany(mappedBy="seller")` |
| Product | `@Node("Product")` | `@Entity` + `@Table(name = "products")` | `imageUrls` → JSONB 列；`@Relationship(SOLD_BY)` → `@ManyToOne` + `seller_id` |
| Order | `@Node("Order")` | `@Entity` + `@Table(name = "orders")` | `@Relationship(PLACED_BY)` → `@ManyToOne` + `user_id`；`@Relationship(INCLUDES)` → `@OneToMany(mappedBy="order")` |
| OrderItem | `@Node("OrderItem")` | `@Entity` + `@Table(name = "order_items")` | 新增 `@ManyToOne order` 字段（双向关联必须） |
| Favorite | `@Node("Favorite")` | `@Entity` + `@Table(name = "favorites")` | 新增唯一约束 `(user_id, product_id)` |
| Review | `@Node("Review")` | `@Entity` + `@Table(name = "reviews")` | 新增唯一约束 `(user_id, product_id)` |

---

## 5. 阶段三：Repository 层重写

### 5.1 包名重命名

```
me.code.springboot_neo4j.repositories  →  me.code.springboot_postgres.repositories
```

所有 Repository 接口继承 `JpaRepository<T, String>`，不再继承 `Neo4jRepository`。

### 5.2 UserRepository

**文件**：`server/src/main/java/me/code/springboot_postgres/repositories/UserRepository.java`

| 原 Cypher 查询 | 新实现方式 | 方法签名 |
|----------------|-----------|---------|
| `MATCH (u:User) WHERE u.id = $id RETURN u` | 继承自 JpaRepository | `Optional<User> findById(String id)` |
| `MATCH (u:User) WHERE u.email = $email RETURN u` | 方法命名 | `Optional<User> findByEmail(String email)` |
| `MATCH (u:User) WHERE u.username = $username RETURN u` | 方法命名 | `Optional<User> findByUsername(String username)` |
| `MATCH (u:User) WHERE u.email = $email RETURN COUNT(u) > 0` | 方法命名 | `boolean existsByEmail(String email)` |
| `MATCH (u:User) WHERE u.username = $username RETURN COUNT(u) > 0` | 方法命名 | `boolean existsByUsername(String username)` |
| `MATCH (u:User) WHERE u.email = $email RETURN COUNT(u) = 0` | 方法命名 | `boolean notExistsByEmail(String email)` → 使用 `!existsByEmail()` |

```java
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

**Service 层适配**：
- `isExistingEmail(email)` → `existsByEmail(email)`
- `isExistingUsername(username)` → `existsByUsername(username)`
- `isInvalidEmail(email)` → `!existsByEmail(email)`

### 5.3 ProductRepository

**文件**：`server/src/main/java/me/code/springboot_postgres/repositories/ProductRepository.java`

| 原 Cypher 查询 | 新实现方式 | 方法签名 |
|----------------|-----------|---------|
| `MATCH (p:Product) RETURN p` | 继承自 JpaRepository | `List<Product> findAll()` |
| `MATCH (p:Product) WHERE p.id = $id RETURN p` | 继承自 JpaRepository | `Optional<Product> findById(String id)` |
| `MATCH (p:Product) RETURN p ORDER BY p.quantity DESC LIMIT $n` | 方法命名 | `List<Product> findTopNByOrderByQuantityDesc(int n)` → 使用 `Pageable` |
| `MATCH (p:Product) RETURN p ORDER BY p.price ASC` | 方法命名 | `List<Product> findAllByOrderByPriceAsc()` |
| `MATCH (p:Product) RETURN p ORDER BY p.price DESC` | 方法命名 | `List<Product> findAllByOrderByPriceDesc()` |
| 模糊搜索 + 自定义排序 | @Query JPQL | `findProductsBySearch(String query)` |
| 搜索 + 价格排序 | @Query JPQL | `findSearchedProductsOrderedByLowestPrice/HighestPrice` |
| 分类过滤 | 方法命名 | `List<Product> findByCategory(String category)` |
| 分类 + 价格排序 | 方法命名 | `findByCategoryOrderByPriceAsc/Desc` |
| 搜索 + 分类组合 | @Query JPQL | `findSearchedProductsByCategory` |
| `MATCH (p:Product)<-[:SELLS]-(u:User {id: $userId}) RETURN p` | 方法命名 | `List<Product> findBySellerId(String sellerId)` |

```java
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findById(String id);

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();

    @Query("SELECT p FROM Product p ORDER BY p.quantity DESC LIMIT :limit")
    List<Product> findTopByQuantityDesc(@Param("limit") int limit);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY CASE WHEN LOWER(p.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0 ELSE 1 END")
    List<Product> findProductsBySearch(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.price ASC")
    List<Product> findSearchedProductsOrderedByLowestPrice(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.price DESC")
    List<Product> findSearchedProductsOrderedByHighestPrice(@Param("query") String query);

    List<Product> findByCategory(String category);

    List<Product> findByCategoryOrderByPriceAsc(String category);

    List<Product> findByCategoryOrderByPriceDesc(String category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.category = :category")
    List<Product> findSearchedProductsByCategory(@Param("query") String query, @Param("category") String category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.category = :category ORDER BY p.price ASC")
    List<Product> findSearchedProductsByCategoryOrderedByLowestPrice(@Param("query") String query, @Param("category") String category);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.category = :category ORDER BY p.price DESC")
    List<Product> findSearchedProductsByCategoryOrderedByHighestPrice(@Param("query") String query, @Param("category") String category);

    List<Product> findBySellerId(String sellerId);
}
```

**Service 层适配**：
- `findProductsWithBiggestQuantity(n)` → `findTopByQuantityDesc(n)`

### 5.4 OrderRepository

**文件**：`server/src/main/java/me/code/springboot_postgres/repositories/OrderRepository.java`

| 原 Cypher 查询 | 新实现方式 | 方法签名 |
|----------------|-----------|---------|
| `MATCH (o:Order) WHERE o.id = $id RETURN o` | 继承自 JpaRepository | `Optional<Order> findById(String id)` |
| `SET o.expectedDelivery = $val` | 改为 Service 层先查后改 | 移除，改为 `save()` |
| `SET o.status = $s, o.expectedDelivery = $d` | 改为 Service 层先查后改 | 移除，改为 `save()` |
| 多跳关系遍历 + COLLECT | @Query JPQL + JOIN FETCH | `findOrdersByUserId` |
| 全部用户订单 | @Query JPQL + JOIN FETCH | `findAllUsersOrders` |
| 按状态筛选 | @Query JPQL + JOIN FETCH | `findAllUsersOrdersByStatus` |

```java
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product " +
           "WHERE u.id = :userId")
    List<Order> findOrdersByUserId(@Param("userId") String userId);

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product")
    List<Order> findAllUsersOrders();

    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN FETCH o.user u " +
           "JOIN FETCH o.items i " +
           "JOIN FETCH i.product " +
           "WHERE o.status = me.code.springboot_postgres.models.entities.Order$Status.valueOf(:status)")
    List<Order> findAllUsersOrdersByStatus(@Param("status") String status);
}
```

**重要变更说明**：

1. **移除 `setExpectedDelivery` 和 `setOrderToSent`**：Neo4j 的 Cypher `SET` 语句可直接修改属性，JPA 中改为 Service 层先 `findById` 获取实体，修改字段后 `save()`。

2. **返回类型从 `Optional<List<Order>>` 改为 `List<Order>`**：JPA 查询不会返回 null 列表，无需 Optional 包裹。

3. **`findAllUsersOrdersByStatus` 的枚举参数**：JPQL 不支持直接将字符串转为枚举，需在 Service 层先转换，或使用以下替代方案：

```java
@Query("SELECT DISTINCT o FROM Order o " +
       "JOIN FETCH o.user u " +
       "JOIN FETCH o.items i " +
       "JOIN FETCH i.product " +
       "WHERE o.status = :status")
List<Order> findAllUsersOrdersByStatus(@Param("status") Order.Status status);
```

Service 层调用时传入枚举值：
```java
Order.Status statusEnum = Order.Status.valueOf(statusString);
orderRepository.findAllUsersOrdersByStatus(statusEnum);
```

### 5.5 FavoriteRepository

**文件**：`server/src/main/java/me/code/springboot_postgres/repositories/FavoriteRepository.java`

```java
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, String> {

    List<Favorite> findByUserId(String userId);

    Optional<Favorite> findByUserIdAndProductId(String userId, String productId);

    int countByProductId(String productId);

    int countByUserId(String userId);
}
```

**说明**：所有查询均可通过 JPA 方法命名约定自动生成，无需 `@Query`。

### 5.6 ReviewRepository

**文件**：`server/src/main/java/me/code/springboot_postgres/repositories/ReviewRepository.java`

```java
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByProductId(String productId);

    List<Review> findByUserId(String userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> getAverageRatingByProductId(@Param("productId") String productId);

    int countByProductId(String productId);

    Optional<Review> findByUserIdAndProductId(String userId, String productId);
}
```

### 5.7 Repository 变更汇总表

| Repository | 原查询数 | 方法命名 | @Query JPQL | 移除/合并 |
|-----------|---------|---------|-------------|----------|
| UserRepository | 6 | 4 | 0 | 2（`isExistingEmail` → `existsByEmail`，`isInvalidEmail` → `!existsByEmail`） |
| ProductRepository | 15 | 5 | 7 | 3（`findAll`/`findById` 继承自 JpaRepository） |
| OrderRepository | 6 | 0 | 3 | 3（`setExpectedDelivery`、`setOrderToSent` 移至 Service 层；`findById` 继承自 JpaRepository） |
| FavoriteRepository | 4 | 4 | 0 | 0 |
| ReviewRepository | 5 | 3 | 1 | 1（`findByUserIdAndProductId` 改为方法命名） |
| **合计** | **36** | **16** | **11** | **9** |

---

## 6. 阶段四：Service 层适配

### 6.1 包名重命名

```
me.code.springboot_neo4j.services  →  me.code.springboot_postgres.services
me.code.springboot_neo4j.dtos      →  me.code.springboot_postgres.dtos
me.code.springboot_neo4j.exceptions → me.code.springboot_postgres.exceptions
```

### 6.2 全局 import 替换

所有 Service 文件中的 import 需要替换：

```
me.code.springboot_neo4j.models.nodes  →  me.code.springboot_postgres.models.entities
me.code.springboot_neo4j.repositories  →  me.code.springboot_postgres.repositories
me.code.springboot_neo4j.dtos          →  me.code.springboot_postgres.dtos
me.code.springboot_neo4j.exceptions    →  me.code.springboot_postgres.exceptions
```

### 6.3 逐文件适配清单

#### UserAccountService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| import | `models.nodes.User` | `models.entities.User` |
| import | `repositories.UserRepository` | `repositories.UserRepository` |
| 方法调用 | 无变化 | 无变化 |

#### LoginValidationService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| `isInvalidEmail(email)` | `userRepository.isInvalidEmail(email)` | `!userRepository.existsByEmail(email)` |

#### RegistrationValidationService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| `isNonUniqueEmail` | `userRepository.isExistingEmail(email)` | `userRepository.existsByEmail(email)` |
| `isNonUniqueUsername` | `userRepository.isExistingUsername(username)` | `userRepository.existsByUsername(username)` |

#### ProductService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| `findProductsWithBiggestQuantity(n)` | `productRepository.findProductsWithBiggestQuantity(n)` | `productRepository.findTopByQuantityDesc(n)` |

#### OrderService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| `findOrdersByUserId` 返回类型 | `orderRepository.findOrdersByUserId(userId).orElseThrow(...)` | `orderRepository.findOrdersByUserId(userId)` （直接返回 List，空列表时抛异常） |
| `placeOrder` 中 Order 构造 | `new Order(user, items, ...)` | 需要额外设置 `items.forEach(i -> i.setOrder(order))` |

**OrderService.placeOrder 关键修改**：

```java
@Transactional
public Success placeOrder(User user, String[] productIds, String address,
                          Order.DeliveryMethod deliveryMethod, Order.PaymentMethod paymentMethod) {
    try {
        List<Product> products = productService.loadProductsById(productIds);
        List<OrderItem> items = orderItemService.generateOrderItems(products);
        List<UnavailableProductDTO> unavailableProducts = productService.findUnavailableProducts(items);

        if (hasUnavailableProducts(unavailableProducts)) {
            throw new OrderException(HttpStatus.BAD_REQUEST, "Could not place order",
                    new OrderErrorDetail("The order contains unavailable products", unavailableProducts));
        }

        productService.updateProductQuantities(items);

        Order order = new Order(user, items, address, deliveryMethod, paymentMethod);
        items.forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        return new Success(HttpStatus.OK, "The order was placed successfully");
    } catch (OrderException exception) {
        throw exception;
    } catch (Exception exception) {
        throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not place order");
    }
}
```

**OrderService.findOrdersByUserId 关键修改**：

```java
public List<Order> findOrdersByUserId(String userId) {
    List<Order> orders = orderRepository.findOrdersByUserId(userId);
    if (orders.isEmpty()) {
        throw new CustomRuntimeException(HttpStatus.NOT_FOUND,
                "Could not find orders placed by user with id: " + userId);
    }
    return orders;
}
```

#### AdminToolsService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| `sendOrder` | `orderRepository.setOrderToSent(order.getId(), Status.SHIPPED, expectedDelivery)` | `order.setStatus(Status.SHIPPED); order.setExpectedDelivery(expectedDelivery); orderRepository.save(order);` |
| `changeExpectedDelivery` | `orderRepository.setExpectedDelivery(order.getId(), newExpectedDelivery)` | `order.setExpectedDelivery(newExpectedDelivery); orderRepository.save(order);` |
| `findAllUsersOrders` | `orderRepository.findAllUsersOrders().orElseThrow(...)` | `orderRepository.findAllUsersOrders()` |
| `findAllUsersOrders(status)` | `orderRepository.findAllUsersOrdersByStatus(status).orElseThrow(...)` | `Order.Status statusEnum = Order.Status.valueOf(status.toUpperCase()); orderRepository.findAllUsersOrdersByStatus(statusEnum);` |

#### FavoriteService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| import | `models.nodes.*` | `models.entities.*` |
| 方法调用 | 无变化 | 无变化 |

#### ReviewService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| import | `models.nodes.*` | `models.entities.*` |
| 方法调用 | 无变化 | 无变化 |

#### OrderItemService.java

| 变更点 | 原代码 | 新代码 |
|--------|--------|--------|
| import | `models.nodes.*` | `models.entities.*` |
| 方法调用 | 无变化 | 无变化 |

### 6.4 Controller 层适配

所有 Controller 文件只需替换 import 路径：

```
me.code.springboot_neo4j.models.nodes  →  me.code.springboot_postgres.models.entities
me.code.springboot_neo4j.services      →  me.code.springboot_postgres.services
me.code.springboot_neo4j.dtos          →  me.code.springboot_postgres.dtos
me.code.springboot_neo4j.exceptions    →  me.code.springboot_postgres.exceptions
```

Controller 中的业务逻辑无需修改。

---

## 7. 阶段五：配置与启动类修改

### 7.1 Application.java

**文件**：`server/src/main/java/me/code/springboot_postgres/Application.java`

**原代码**：
```java
package me.code.springboot_neo4j;

import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jAuditing
@EnableNeo4jRepositories(basePackages = {"me.code.springboot_neo4j.repositories"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Configuration cypherDslConfiguration() {
        return Configuration.newConfig().withDialect(Dialect.NEO4J_5).build();
    }
}
```

**新代码**：
```java
package me.code.springboot_postgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"me.code.springboot_postgres.repositories"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**变更要点**：
| 原代码 | 新代码 | 说明 |
|--------|--------|------|
| `@EnableNeo4jAuditing` | `@EnableJpaAuditing` | JPA 审计 |
| `@EnableNeo4jRepositories` | `@EnableJpaRepositories` | JPA Repository 扫描 |
| `cypherDslConfiguration()` Bean | 移除 | 不再需要 CypherDSL |
| 包名 | `springboot_neo4j` | `springboot_postgres` |

### 7.2 包目录结构重命名

```
server/src/main/java/me/code/springboot_neo4j/
  → server/src/main/java/me/code/springboot_postgres/

server/src/main/java/me/code/springboot_neo4j/models/nodes/
  → server/src/main/java/me/code/springboot_postgres/models/entities/

server/src/test/java/me/code/springboot_neo4j/
  → server/src/test/java/me/code/springboot_postgres/
```

---

## 8. 阶段六：数据库初始化与种子数据

### 8.1 Flyway 迁移脚本

**文件**：`server/src/main/resources/db/migration/V1__create_initial_schema.sql`

```sql
-- ============================================================
-- V1__create_initial_schema.sql
-- 电商系统初始数据库 Schema（PostgreSQL）
-- ============================================================

-- 用户表
CREATE TABLE users (
    id              VARCHAR(36)    PRIMARY KEY,
    email           VARCHAR(255)   NOT NULL UNIQUE,
    username        VARCHAR(255)   NOT NULL UNIQUE,
    password        VARCHAR(255)   NOT NULL,
    role            VARCHAR(10)    NOT NULL DEFAULT 'USER',
    avatar_url      VARCHAR(500),
    bio             VARCHAR(500)
);

-- 商品表
CREATE TABLE products (
    id              VARCHAR(36)    PRIMARY KEY,
    name            VARCHAR(255)   NOT NULL,
    description     TEXT,
    image_urls      JSONB,
    price           DOUBLE PRECISION NOT NULL,
    quantity        INTEGER        NOT NULL DEFAULT 0,
    category        VARCHAR(30)    NOT NULL,
    condition       VARCHAR(15),
    source          VARCHAR(20),
    seller_id       VARCHAR(36)    REFERENCES users(id) ON DELETE SET NULL
);

-- 订单表
CREATE TABLE orders (
    id                  VARCHAR(36)    PRIMARY KEY,
    status              VARCHAR(15)    NOT NULL DEFAULT 'PENDING',
    price               DOUBLE PRECISION NOT NULL,
    payment_method      VARCHAR(25)    NOT NULL,
    address             TEXT           NOT NULL,
    delivery_method     VARCHAR(30)    NOT NULL,
    received            TIMESTAMP,
    expected_delivery   TIMESTAMP,
    user_id             VARCHAR(36)    NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- 订单项表
CREATE TABLE order_items (
    id              VARCHAR(36)    PRIMARY KEY,
    amount          INTEGER        NOT NULL,
    price           DOUBLE PRECISION NOT NULL,
    product_id      VARCHAR(36)    NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    order_id        VARCHAR(36)    NOT NULL REFERENCES orders(id) ON DELETE CASCADE
);

-- 收藏表
CREATE TABLE favorites (
    id              VARCHAR(36)    PRIMARY KEY,
    created_at      TIMESTAMP      NOT NULL,
    user_id         VARCHAR(36)    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      VARCHAR(36)    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- 评价表
CREATE TABLE reviews (
    id              VARCHAR(36)    PRIMARY KEY,
    rating          INTEGER        NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment         TEXT,
    created_at      TIMESTAMP      NOT NULL,
    user_id         VARCHAR(36)    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      VARCHAR(36)    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- ============================================================
-- 索引
-- ============================================================

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_name_lower ON products(LOWER(name));

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_product_id ON favorites(product_id);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
```

### 8.2 种子数据迁移脚本

**文件**：`server/src/main/resources/db/migration/V2__insert_seed_data.sql`

```sql
-- ============================================================
-- V2__insert_seed_data.sql
-- 初始种子数据
-- ============================================================

-- 默认用户（密码：Password，BCrypt 加密）
INSERT INTO users (id, email, username, password, role) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'user@user.com', 'JohnDoe',
     '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Kz7aKdBdCkqy5uLbTLyGq', 'USER'),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'admin@admin.com', 'JaneDoe',
     '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Kz7aKdBdCkqy5uLbTLyGq', 'ADMIN');

-- 默认商品
INSERT INTO products (id, name, description, image_urls, price, quantity, category, condition, source, seller_id) VALUES
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', '初音未来 手办 Ver.', '初音未来经典造型手办，高精度涂装，约23cm高，附带底座。',
     '["https://m.media-amazon.com/images/I/71K3JnFqJWL._AC_SL1500_.jpg"]'::jsonb,
     299.00, 10, 'ANIME_FIGURE', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', '进击的巨人 海报套装', '进击的巨人动画经典场景海报5张套装，A3尺寸，高清印刷。',
     '["https://m.media-amazon.com/images/I/81Jqd2BxDNL._AC_SL1500_.jpg"]'::jsonb,
     49.90, 20, 'POSTER', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b03', '鬼灭之善逸 钥匙扣', '我妻善逸Q版造型钥匙扣，合金材质，精美电镀工艺。',
     '["https://m.media-amazon.com/images/I/61C5XwJqYqL._AC_SL1500_.jpg"]'::jsonb,
     19.90, 50, 'KEYCHAIN', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'Fate/stay night 徽章套装', 'Saber、Archer、Rider等角色金属徽章6枚套装，直径5.8cm。',
     '["https://m.media-amazon.com/images/I/71qw3P3mYPL._AC_SL1500_.jpg"]'::jsonb,
     35.00, 30, 'BADGE', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'Re:Zero 蕾姆抱枕', '蕾姆48cm×150cm长款抱枕，双面不同图案，柔软亲肤面料。',
     '["https://m.media-amazon.com/images/I/61ZLq3M5RjL._AC_SL1500_.jpg"]'::jsonb,
     89.00, 15, 'PILLOW', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b06', '咒术回战 五条悟亚克力立牌', '五条悟Q版亚克力立牌，高约15cm，附透明支架。',
     '["https://m.media-amazon.com/images/I/51C8K5V5qQL._AC_SL1000_.jpg"]'::jsonb,
     29.90, 25, 'STAND', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b07', 'JoJo的奇妙冒险 T恤', 'JoJo经典姿势印花T恤，纯棉面料，L码。',
     '["https://m.media-amazon.com/images/I/71YJZ3T9K6L._AC_SL1500_.jpg"]'::jsonb,
     79.00, 20, 'CLOTHING', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b08', 'YOASOBI 原声专辑', 'YOASOBI精选专辑CD，收录《夜に駆ける》《群青》等热门曲目。',
     '["https://m.media-amazon.com/images/I/71qVJQK5JfL._AC_SL1500_.jpg"]'::jsonb,
     128.00, 12, 'ALBUM', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b09', '宝可梦 皮卡丘手机壳', '皮卡丘可爱造型手机壳，适配iPhone 15，TPU软壳防摔。',
     '["https://m.media-amazon.com/images/I/61U+J3Q3qJL._AC_SL1500_.jpg"]'::jsonb,
     39.90, 40, 'ACCESSORY', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b10', '海贼王 路飞手办 二手', '路飞四档手办，轻微展示痕迹，整体品相良好，约18cm。',
     '["https://m.media-amazon.com/images/I/71R5F3JvWmL._AC_SL1500_.jpg"]'::jsonb,
     159.00, 5, 'ANIME_FIGURE', 'GOOD', 'USER', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', '原神 钟离亚克力立牌', '钟离角色立牌，精美印刷，高约12cm。',
     '["https://m.media-amazon.com/images/I/51C8K5V5qQL._AC_SL1000_.jpg"]'::jsonb,
     25.00, 30, 'STAND', 'NEW', 'PLATFORM', NULL),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12', '间谍过家家 阿尼亚钥匙扣', '阿尼亚wakuwaku造型钥匙扣，亚克力材质，双面印刷。',
     '["https://m.media-amazon.com/images/I/61C5XwJqYqL._AC_SL1500_.jpg"]'::jsonb,
     15.90, 60, 'KEYCHAIN', 'NEW', 'PLATFORM', NULL);
```

**注意**：种子数据中的密码哈希值需替换为实际 BCrypt 加密后的值。上述示例中的哈希值仅为占位符，实际部署时应由应用首次启动时通过 `CommandLineRunner` 生成（保留原有逻辑）。

### 8.3 移除旧的种子数据配置

**删除以下文件**：
- `server/src/main/java/me/code/springboot_postgres/config/neo4j/NodesConfig.java`
- `server/src/main/java/me/code/springboot_postgres/config/neo4j/MockUsersConfig.java`
- `server/src/main/java/me/code/springboot_postgres/config/neo4j/MockProductsConfig.java`

**新增**：`server/src/main/java/me/code/springboot_postgres/config/DataInitializer.java`

```java
package me.code.springboot_postgres.config;

import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            String encryptedPassword = passwordEncoder.encode("Password");

            User user = new User("user@user.com", "JohnDoe", encryptedPassword, User.Role.USER);
            userRepository.save(user);

            User admin = new User("admin@admin.com", "JaneDoe", encryptedPassword, User.Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
```

**说明**：商品种子数据已由 Flyway V2 脚本管理，用户种子数据因需密码加密而保留在 CommandLineRunner 中。

---

## 9. 阶段七：Docker 与部署配置

### 9.1 修改 docker-compose.yml

**文件**：`docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: merchandise-postgres
    environment:
      - POSTGRES_DB=${POSTGRES_DB:-merchandise}
      - POSTGRES_USER=${POSTGRES_USER:-postgres}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-merchandise123}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - merchandise-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-postgres}"]
      interval: 10s
      timeout: 5s
      retries: 10

  server:
    build:
      context: ./server
      dockerfile: Dockerfile
    container_name: merchandise-server
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB:-merchandise}
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER:-postgres}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD:-merchandise123}
      - JWT_SECRET=${JWT_SECRET:-dev-only-secret-key-change-in-production-min-32-chars}
      - JWT_EXPIRATION_MS=${JWT_EXPIRATION_MS:-3600000}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - merchandise-network

  client:
    build:
      context: ./client
      dockerfile: Dockerfile
    container_name: merchandise-client
    ports:
      - "80:80"
    depends_on:
      - server
    networks:
      - merchandise-network

volumes:
  postgres_data:

networks:
  merchandise-network:
    driver: bridge
```

**变更要点**：
| 原配置 | 新配置 | 说明 |
|--------|--------|------|
| `neo4j:5-community` | `postgres:16-alpine` | 数据库镜像替换 |
| `container_name: merchandise-neo4j` | `container_name: merchandise-postgres` | 容器名 |
| `NEO4J_AUTH` / `NEO4J_PLUGINS` | `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | PostgreSQL 环境变量 |
| 端口 `7474:7474` / `7687:7687` | 端口 `5432:5432` | PostgreSQL 默认端口 |
| `neo4j_data:/data` | `postgres_data:/var/lib/postgresql/data` | 数据卷路径 |
| 健康检查 `wget` | `pg_isready` | PostgreSQL 健康检查 |
| `SPRING_NEO4J_URI` 等 | `SPRING_DATASOURCE_URL` 等 | Spring Boot 数据源配置 |

### 9.2 Dockerfile 无需修改

`server/Dockerfile` 和 `client/Dockerfile` 无需修改，构建流程不变。

---

## 10. 阶段八：测试验证

### 10.1 编译验证

```bash
cd server
mvn clean compile
```

确保无编译错误。

### 10.2 启动验证

```bash
# 启动 PostgreSQL
docker compose up postgres -d

# 启动后端（本地开发）
cd server
mvn spring-boot:run
```

验证项：
- [ ] Flyway 迁移脚本成功执行
- [ ] 种子数据正确插入
- [ ] 应用无错误启动

### 10.3 API 端点功能测试

按以下顺序逐个验证所有 API 端点：

| # | 端点 | 方法 | 验证要点 |
|---|------|------|---------|
| 1 | `/api/auth/register` | POST | 用户注册，邮箱/用户名唯一性校验 |
| 2 | `/api/auth/login` | POST | 用户登录，JWT 令牌生成 |
| 3 | `/api/products` | GET | 商品列表查询 |
| 4 | `/api/products/{id}` | GET | 单个商品详情 |
| 5 | `/api/products/featured` | GET | 热门商品（按库存排序） |
| 6 | `/api/products/search?query=xxx` | GET | 模糊搜索 |
| 7 | `/api/products/search?query=xxx&filter=lowest_price` | GET | 搜索 + 价格排序 |
| 8 | `/api/products/search?query=xxx&category=ANIME_FIGURE` | GET | 搜索 + 分类过滤 |
| 9 | `/api/products/category/{category}` | GET | 分类查询 |
| 10 | `/api/orders` | POST | 下单（含库存扣减、订单项生成） |
| 11 | `/api/orders/user/{userId}` | GET | 用户订单列表（含订单项和商品详情） |
| 12 | `/api/favorites` | POST | 添加收藏 |
| 13 | `/api/favorites/user/{userId}` | GET | 用户收藏列表 |
| 14 | `/api/reviews` | POST | 添加评价 |
| 15 | `/api/reviews/product/{productId}` | GET | 商品评价列表 + 平均评分 |
| 16 | `/api/admin_tools/orders` | GET | 管理员查看所有订单 |
| 17 | `/api/admin_tools/orders/{id}/send` | PATCH | 管理员发货（修改状态和预期送达时间） |
| 18 | `/api/admin_tools/products` | POST | 管理员添加商品 |
| 19 | `/api/admin_tools/products/{id}` | PUT | 管理员编辑商品 |
| 20 | `/api/admin_tools/products/{id}` | DELETE | 管理员删除商品 |

### 10.4 前端集成测试

```bash
# 启动全部服务
docker compose up --build
```

验证项：
- [ ] 前端页面正常加载
- [ ] 用户注册/登录流程正常
- [ ] 商品浏览、搜索、分类筛选正常
- [ ] 购物车和下单流程正常
- [ ] 收藏和评价功能正常
- [ ] 管理员功能正常

---

## 11. 迁移检查清单

### 文件变更检查

- [ ] `server/pom.xml` — 依赖替换完成
- [ ] `server/src/main/resources/application.yml` — 配置替换完成
- [ ] `server/src/main/resources/db/migration/V1__create_initial_schema.sql` — 新增
- [ ] `server/src/main/resources/db/migration/V2__insert_seed_data.sql` — 新增
- [ ] `server/src/main/java/me/code/springboot_postgres/Application.java` — 启动类修改
- [ ] `server/src/main/java/me/code/springboot_postgres/models/entities/User.java` — 模型重构
- [ ] `server/src/main/java/me/code/springboot_postgres/models/entities/Product.java` — 模型重构
- [ ] `server/src/main/java/me/code/springboot_postgres/models/entities/Order.java` — 模型重构
- [ ] `server/src/main/java/me/code/springboot_postgres/models/entities/OrderItem.java` — 模型重构
- [ ] `server/src/main/java/me/code/springboot_postgres/models/entities/Favorite.java` — 模型重构
- [ ] `server/src/main/java/me/code/springboot_postgres/models/entities/Review.java` — 模型重构
- [ ] `server/src/main/java/me/code/springboot_postgres/repositories/UserRepository.java` — Repository 重写
- [ ] `server/src/main/java/me/code/springboot_postgres/repositories/ProductRepository.java` — Repository 重写
- [ ] `server/src/main/java/me/code/springboot_postgres/repositories/OrderRepository.java` — Repository 重写
- [ ] `server/src/main/java/me/code/springboot_postgres/repositories/FavoriteRepository.java` — Repository 重写
- [ ] `server/src/main/java/me/code/springboot_postgres/repositories/ReviewRepository.java` — Repository 重写
- [ ] 所有 Service 文件 — import 替换 + 方法适配
- [ ] 所有 Controller 文件 — import 替换
- [ ] 所有 DTO 文件 — import 替换
- [ ] 所有 Exception 文件 — import 替换
- [ ] `server/src/main/java/me/code/springboot_postgres/config/DataInitializer.java` — 新增
- [ ] 删除 `config/neo4j/` 目录下的 3 个文件
- [ ] 删除 `models/nodes/` 目录
- [ ] `docker-compose.yml` — Docker 配置替换
- [ ] 删除 `server/src/main/java/me/code/springboot_neo4j/` 旧包目录

### 功能回归检查

- [ ] 用户注册（邮箱/用户名唯一性校验）
- [ ] 用户登录（JWT 认证）
- [ ] 商品列表查询（全量/排序）
- [ ] 商品搜索（模糊匹配 + 排序 + 分类组合）
- [ ] 商品详情
- [ ] 热门商品（按库存排序 Top N）
- [ ] 下单（库存校验 + 扣减 + 订单生成）
- [ ] 用户订单列表（含订单项和商品详情）
- [ ] 收藏（添加/删除/列表/计数）
- [ ] 评价（添加/列表/平均评分/计数）
- [ ] 管理员功能（用户管理/订单发货/商品CRUD）

---

## 12. 风险与回退策略

### 12.1 已知风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| JPA `@OneToMany` + `JOIN FETCH` 可能产生重复结果 | 订单列表返回重复数据 | 使用 `SELECT DISTINCT` 或 `Set` 替代 `List` |
| `imageUrls` JSONB 列的 Hibernate 映射兼容性 | 读写异常 | 使用 `@JdbcTypeCode(SqlTypes.JSON)` 确保正确序列化 |
| Order 构造函数中 OrderItem 的双向关联设置 | 订单项未正确关联到 Order | 在 `placeOrder` 中显式调用 `item.setOrder(order)` |
| Flyway 迁移脚本与 Hibernate `ddl-auto: validate` 冲突 | 启动失败 | 确保 Flyway 脚本与 Entity 注解定义一致 |
| `findAllUsersOrdersByStatus` 的枚举参数传递 | 查询异常 | Service 层先将字符串转为枚举再传参 |

### 12.2 回退策略

如果迁移后出现严重问题：

1. **代码回退**：`git checkout main` — 回到 Neo4j 版本
2. **数据回退**：恢复 Neo4j 数据备份
3. **Docker 回退**：恢复原 `docker-compose.yml`，重新启动 Neo4j 容器

由于采用分支开发策略（`feature/migrate-to-postgresql`），主分支始终保留可用的 Neo4j 版本，回退零风险。

---

## 附录：Cypher → JPQL 查询对照表

| # | 原 Cypher 查询 | 新 JPQL / 方法命名 |
|---|----------------|-------------------|
| 1 | `MATCH (u:User) WHERE u.email = $email RETURN u` | `findByEmail(String email)` |
| 2 | `MATCH (u:User) WHERE u.username = $username RETURN u` | `findByUsername(String username)` |
| 3 | `MATCH (u:User) WHERE u.email = $email RETURN COUNT(u) > 0` | `existsByEmail(String email)` |
| 4 | `MATCH (u:User) WHERE u.username = $username RETURN COUNT(u) > 0` | `existsByUsername(String username)` |
| 5 | `MATCH (p:Product) RETURN p ORDER BY p.quantity DESC LIMIT $n` | `@Query("SELECT p FROM Product p ORDER BY p.quantity DESC LIMIT :limit")` |
| 6 | `MATCH (p:Product) RETURN p ORDER BY p.price ASC` | `findAllByOrderByPriceAsc()` |
| 7 | `MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($q) RETURN p` | `@Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")` |
| 8 | `MATCH (p:Product) WHERE toLower(p.name) CONTAINS toLower($q) RETURN p ORDER BY p.price ASC` | `@Query("... ORDER BY p.price ASC")` |
| 9 | `MATCH (p:Product) WHERE p.category = $cat RETURN p` | `findByCategory(String category)` |
| 10 | `MATCH (p:Product)<-[:SELLS]-(u:User {id: $uid}) RETURN p` | `findBySellerId(String sellerId)` |
| 11 | `MATCH (o:Order) WHERE o.id = $id SET o.status = $s, o.expectedDelivery = $d` | Service 层: `order.setStatus(s); order.setExpectedDelivery(d); save(order)` |
| 12 | `MATCH (user)...(order)...(item)...(product) WITH order, COLLECT(...) RETURN order, ...` | `@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user JOIN FETCH o.items i JOIN FETCH i.product WHERE u.id = :userId")` |
| 13 | `MATCH (f:Favorite)-[:FAVORITED_BY]->(u:User {id: $uid}) RETURN f` | `findByUserId(String userId)` |
| 14 | `MATCH (f:Favorite)-[:FAVORITED_BY]->(u:User {id: $uid})-[:FAVORITE_PRODUCT]->(p:Product {id: $pid}) RETURN f` | `findByUserIdAndProductId(String userId, String productId)` |
| 15 | `MATCH (r:Review)-[:ABOUT]->(p:Product {id: $pid}) RETURN AVG(r.rating)` | `@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")` |
