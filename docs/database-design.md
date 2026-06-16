# 数据库设计

> 本文档从 [README.md](../README.md) 拆分而来，详细描述项目的数据库表结构和索引设计。

---

## 概述

共 7 张表，由 JPA 自动建表（`hibernate.ddl-auto=update`）。系统使用 `DataInitializer` 在应用启动时自动确保内置用户（超级管理员、商户、测试用户）存在且字段正确，并自动插入覆盖 10 个分类的 32 个测试商品，无需手动执行 SQL 迁移脚本。

---

## 表结构

### users — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `email` | VARCHAR(255) | 邮箱（唯一，非空） |
| `username` | VARCHAR(255) | 用户名（唯一，非空） |
| `password` | VARCHAR(255) | 密码（BCrypt 加密，非空） |
| `role` | ENUM | 角色：`USER`、`MERCHANT`、`ADMIN`、`SUPER_ADMIN` |
| `avatar_url` | VARCHAR(255) | 头像 URL |
| `bio` | VARCHAR(255) | 个人简介 |
| `balance` | DECIMAL(12,2) | 账户余额（默认 0） |
| `is_enabled` | BOOLEAN | 是否启用（默认 true，禁用后无法登录） |
| `is_protected` | BOOLEAN | 是否受保护（默认 false，受保护账号不可改名/改密码/删除） |
| `version` | INTEGER | 乐观锁版本号 |

> User 实体实现了 Spring Security 的 `UserDetails` 接口，支持认证集成。

### products — 商品表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `name` | VARCHAR(255) | 商品名称（非空） |
| `description` | TEXT | 商品描述 |
| `price` | DECIMAL(10,2) | 商品价格 |
| `quantity` | INTEGER | 库存数量 |
| `image_urls` | JSONB | 图片 URL 列表 |
| `category` | ENUM | 分类：`ANIME_FIGURE`、`POSTER`、`KEYCHAIN`、`BADGE`、`PILLOW`、`STAND`、`CLOTHING`、`ALBUM`、`ACCESSORY`、`OTHER` |
| `condition` | ENUM | 成色：`NEW`、`LIKE_NEW`、`GOOD`、`FAIR` |
| `status` | ENUM | 状态：`PENDING`、`APPROVED`、`REJECTED`、`DISABLED`（默认 `APPROVED`） |
| `source` | VARCHAR(255) | 来源：`PLATFORM`（平台自营）/ `USER`（商户发布） |
| `reject_reason` | TEXT | 驳回原因 |
| `seller_id` | VARCHAR(36) | 卖家 ID（外键 → users，ManyToOne 懒加载） |
| `version` | INTEGER | 乐观锁版本号 |

### orders — 订单表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 买家 ID（外键 → users，ManyToOne 懒加载） |
| `status` | ENUM | 状态：`PENDING`、`SHIPPED`、`DELIVERED` |
| `delivery_method` | ENUM | 配送方式：`STANDARD_DELIVERY`、`EXPRESS_DELIVERY` |
| `payment_method` | ENUM | 支付方式：`ACCOUNT_BALANCE` |
| `receiver_name` | VARCHAR(255) | 收货人姓名 |
| `receiver_phone` | VARCHAR(255) | 联系电话 |
| `region` | VARCHAR(255) | 省/市/区 |
| `detail_address` | TEXT | 详细地址 |
| `received` | TIMESTAMP | 接收时间 |
| `expected_delivery` | TIMESTAMP | 预计送达时间 |
| `price` | DECIMAL(10,2) | 订单总价 |

> 订单与订单项为一对多关系（`OneToMany`，级联 ALL）。

### order_items — 订单项表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `order_id` | VARCHAR(36) | 订单 ID（外键 → orders，ManyToOne 懒加载） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products，ManyToOne 懒加载） |
| `amount` | INTEGER | 购买数量 |
| `price` | DECIMAL(10,2) | 小计价格（单价 × 数量） |

### cart_items — 购物车项表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 用户 ID（外键 → users，ManyToOne 懒加载） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products，ManyToOne 懒加载） |
| `quantity` | INTEGER | 商品数量 |

**唯一约束**：`(user_id, product_id)`

### favorites — 收藏表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 用户 ID（外键 → users，ManyToOne 懒加载） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products，ManyToOne 懒加载） |
| `created_at` | TIMESTAMP | 收藏时间 |

**唯一约束**：`(user_id, product_id)`

### reviews — 评价表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 评价用户 ID（外键 → users，ManyToOne 懒加载） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products，ManyToOne 懒加载） |
| `rating` | INTEGER | 评分（1-5，Bean Validation 校验） |
| `comment` | TEXT | 评价内容 |
| `created_at` | TIMESTAMP | 评价时间 |

**唯一约束**：`(user_id, product_id)`

---

## 索引设计

| 表名 | 索引名 | 列 | 说明 |
|------|--------|-----|------|
| `cart_items` | `idx_cart_items_user_id` | `user_id` | 按用户查询购物车 |
| `cart_items` | 唯一约束 | `(user_id, product_id)` | 防止重复添加 |
| `favorites` | `idx_favorites_user_id` | `user_id` | 按用户查询收藏 |
| `favorites` | 唯一约束 | `(user_id, product_id)` | 防止重复收藏 |
| `reviews` | `idx_reviews_product_id` | `product_id` | 按商品查询评价 |
| `reviews` | `idx_reviews_user_id` | `user_id` | 按用户查询评价 |
| `reviews` | 唯一约束 | `(user_id, product_id)` | 每个用户对每件商品只能评价一次 |
| `orders` | `idx_orders_status` | `status` | 按状态筛选订单 |
| `products` | `idx_products_seller_id` | `seller_id` | 按卖家查询商品 |
| `products` | `idx_products_status` | `status` | 按状态筛选商品 |

---

## 实体关系图

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    users     │     │   products   │     │  cart_items  │
├──────────────┤     ├──────────────┤     ├──────────────┤
│ id (PK)      │◄──┐ │ id (PK)      │◄──┐ │ id (PK)      │
│ email (UQ)   │   │ │ name         │   │ │ user_id (FK) │──► users
│ username(UQ) │   │ │ price        │   │ │ product_id   │──► products
│ role         │   │ │ category     │   │ │ quantity     │
│ avatar_url   │   │ │ condition    │   └──────────────┘
│ bio          │   │ │ status       │
│ balance      │   │ │ source       │     ┌──────────────┐
│ is_enabled   │   │ │ seller_id(FK)│──►  │  favorites   │
│ is_protected │   │ │ reject_reason│     ├──────────────┤
│ version      │   │ │ version      │     │ id (PK)      │
└──────────────┘   │ └──────────────┘     │ user_id (FK) │──► users
     ▲             │                      │ product_id   │──► products
     │             │                      │ created_at   │
     │             │                      └──────────────┘
     │             │
     │             │  ┌──────────────┐  ┌──────────────┐
     │             │  │   orders     │  │ order_items  │
     │             │  ├──────────────┤  ├──────────────┤
     │             │  │ id (PK)      │  │ id (PK)      │
     └─────────────│─►│ user_id (FK) │  │ order_id(FK) │──► orders
                   │  │ status       │  │ product_id   │──► products
                   │  │ price        │  │ amount       │
                   │  │ delivery_method│ │ price        │
                   │  │ payment_method│  └──────────────┘
                   │  │ receiver_name│
                   │  │ receiver_phone│
                   │  │ region       │
                   │  │ detail_address│
                   │  │ received     │
                   │  │ expected_    │
                   │  │  delivery    │
                   │  └──────────────┘
                   │
                   │  ┌──────────────┐
                   │  │   reviews    │
                   │  ├──────────────┤
                   │  │ id (PK)      │
                   └─►│ user_id (FK) │──► users
                      │ product_id   │──► products
                      │ rating       │
                      │ comment      │
                      │ created_at   │
                      └──────────────┘
```
