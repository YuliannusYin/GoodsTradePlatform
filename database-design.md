# 数据库设计

> 本文档从 [README.md](../README.md) 拆分而来，详细描述项目的数据库表结构和索引设计。

---

## 概述

共 7 张表，由 JPA 自动建表（`hibernate.ddl-auto=update`）。系统使用 `DataInitializer` 在应用启动时自动确保内置用户（超级管理员、商户、测试用户）存在且字段正确，无需手动执行 SQL 迁移脚本。

---

## 表结构

### users — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `email` | VARCHAR(255) | 邮箱（唯一） |
| `username` | VARCHAR(255) | 用户名 |
| `password` | VARCHAR(255) | 密码（BCrypt 加密） |
| `role` | ENUM | 角色：`SUPER_ADMIN`、`ADMIN`、`MERCHANT`、`USER` |
| `balance` | DECIMAL | 账户余额 |
| `is_enabled` | BOOLEAN | 是否启用（禁用后无法登录） |
| `is_protected` | BOOLEAN | 是否受保护（受保护账号不可改名/改密码/删除） |
| `version` | INTEGER | 乐观锁版本号 |

### products — 商品表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `name` | VARCHAR(255) | 商品名称 |
| `description` | TEXT | 商品描述 |
| `price` | DECIMAL | 商品价格 |
| `quantity` | INTEGER | 库存数量 |
| `image_urls` | JSONB | 图片 URL 列表 |
| `category` | ENUM | 分类：`FIGURE`、`POSTER`、`KEYCHAIN`、`BADGE`、`CUSHION`、`STAND`、`CLOTHING`、`ALBUM`、`ACCESSORY` |
| `condition` | ENUM | 成色：`NEW`、`LIKE_NEW`、`GOOD`、`FAIR` |
| `status` | ENUM | 状态：`PENDING`、`APPROVED`、`REJECTED`、`DISABLED` |
| `source` | ENUM | 来源：`PLATFORM`、`USER` |
| `reject_reason` | TEXT | 驳回原因 |
| `seller_id` | VARCHAR(36) | 卖家 ID（外键 → users） |
| `version` | INTEGER | 乐观锁版本号 |

### orders — 订单表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 买家 ID（外键 → users） |
| `status` | ENUM | 状态：`PENDING`、`PROCESSING`、`SHIPPED`、`DELIVERED`、`CANCELLED` |
| `delivery_method` | ENUM | 配送方式：`STANDARD`、`EXPRESS`、`PICKUP` |
| `payment_method` | ENUM | 支付方式：`ACCOUNT_BALANCE` |
| `receiver_name` | VARCHAR(255) | 收货人姓名 |
| `receiver_phone` | VARCHAR(255) | 联系电话 |
| `region` | VARCHAR(255) | 省/市/区 |
| `detail_address` | VARCHAR(255) | 详细地址 |
| `expected_delivery` | TIMESTAMP | 预计送达时间 |
| `created_at` | TIMESTAMP | 创建时间 |
| `total_price` | DECIMAL | 订单总价 |

### order_items — 订单项表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `order_id` | VARCHAR(36) | 订单 ID（外键 → orders） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products） |
| `quantity` | INTEGER | 购买数量 |
| `price` | DECIMAL | 购买时总价（单价 × 数量） |

### cart_items — 购物车项表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 用户 ID（外键 → users） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products） |
| `quantity` | INTEGER | 商品数量 |

**唯一约束**：`(user_id, product_id)`

### favorites — 收藏表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 用户 ID（外键 → users） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products） |
| `created_at` | TIMESTAMP | 收藏时间 |

**唯一约束**：`(user_id, product_id)`

### reviews — 评价表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | VARCHAR(36) | 主键（UUID） |
| `user_id` | VARCHAR(36) | 评价用户 ID（外键 → users） |
| `product_id` | VARCHAR(36) | 商品 ID（外键 → products） |
| `rating` | INTEGER | 评分（1-5） |
| `comment` | TEXT | 评价内容 |
| `created_at` | TIMESTAMP | 评价时间 |

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
| `orders` | `idx_orders_status` | `status` | 按状态筛选订单 |
| `products` | `idx_products_seller_id` | `seller_id` | 按卖家查询商品 |
| `products` | `idx_products_status` | `status` | 按状态筛选商品 |

---

## 实体关系图

```
┌──────────┐     ┌──────────┐     ┌──────────────┐
│  users   │     │ products │     │  cart_items  │
├──────────┤     ├──────────┤     ├──────────────┤
│ id (PK)  │◄──┐ │ id (PK)  │◄──┐ │ id (PK)      │
│ email    │   │ │ name     │   │ │ user_id (FK) │──► users
│ username │   │ │ price    │   │ │ product_id   │──► products
│ role     │   │ │ category │   │ │ quantity     │
│ balance  │   │ │ status   │   └──────────────┘
│ is_enabled│  │ │ seller_id│──► users
└──────────┘   │ └──────────┘     ┌──────────────┐
     ▲         │                  │  favorites   │
     │         │                  ├──────────────┤
     │         │                  │ id (PK)      │
     │         │                  │ user_id (FK) │──► users
     │         │                  │ product_id   │──► products
     │         │                  └──────────────┘
     │         │
     │         │  ┌──────────────┐  ┌──────────────┐
     │         │  │   orders     │  │ order_items  │
     │         │  ├──────────────┤  ├──────────────┤
     │         │  │ id (PK)      │  │ id (PK)      │
     └─────────│─►│ user_id (FK) │  │ order_id(FK) │──► orders
               │  │ status       │  │ product_id   │──► products
               │  │ total_price  │  │ quantity     │
               │  └──────────────┘  │ price        │
               │                    └──────────────┘
               │
               │  ┌──────────────┐
               │  │   reviews    │
               │  ├──────────────┤
               │  │ id (PK)      │
               └─►│ user_id (FK) │──► users
                  │ product_id   │──► products
                  │ rating       │
                  │ comment      │
                  └──────────────┘
```
