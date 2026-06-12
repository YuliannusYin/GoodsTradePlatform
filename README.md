# 周边交易平台

![Java](https://img.shields.io/badge/Java-17-orange?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot) ![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vue.js) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql) ![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker) ![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?logo=tailwindcss)

## 项目简介

本项目是一个基于 Vue 3 + Spring Boot + PostgreSQL 的**周边交易平台**，支持 B2C 平台自营和 C2C 用户闲置交易两种模式。用户可以浏览、搜索、购买各类动漫、游戏、偶像周边商品，也可以申请成为商户发布自己的闲置周边进行售卖。系统采用固定角色权限模型，支持超级管理员、管理员、商户、普通用户四种角色。

## 功能特性

### 核心功能
- **用户注册/登录**：JWT 认证，固定角色权限模型（超级管理员、管理员、商户、普通用户）
- **商品浏览与搜索**：支持关键词搜索、价格排序、分类筛选
- **商品分类**：手办、海报、钥匙扣、徽章、抱枕、立牌、服饰、专辑、配件等
- **商品成色**：全新、几乎全新、良好、一般
- **购物车**：添加/移除商品，实时计算价格
- **订单管理**：下单、查看订单状态、管理员发货

### 扩展功能
- **商品收藏**：用户可收藏感兴趣的商品，在收藏夹中统一管理
- **商品评价/评分**：购买后可对商品进行 1-5 星评分和文字评价
- **分类筛选**：商城页面左侧分类导航，快速筛选目标品类
- **商户发布商品（C2C）**：用户可申请成为商户，发布闲置周边，标记为"个人闲置"
- **商品审核**：商户发布的商品需经管理员审核（通过/驳回），平台商品自动通过
- **商品多图展示**：支持多张商品图片

### 管理员功能
- 商品增删改查、审核（通过/驳回）、禁用/启用
- 订单管理（查看、发货、修改预计送达时间）
- 用户管理（查看、禁用/启用、分配角色）

### 角色权限系统
- **SUPER_ADMIN**：拥有所有权限，包括用户管理
- **ADMIN**：管理商品、订单和审核
- **MERCHANT**：可发布/编辑/删除自己的商品
- **USER**：可浏览、购买、收藏和评价

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (`<script setup>` + Composition API) + TypeScript 5 + Pinia + Vue Router 4 |
| UI 框架 | Tailwind CSS 3 + Font Awesome |
| 构建工具 | Vite 4 |
| 后端 | Java 17 + Spring Boot 3.2 + Spring Security + Spring Data JPA + Bean Validation |
| 数据库 | PostgreSQL 16 (关系型数据库) |
| 数据库迁移 | Flyway 10.10 |
| 认证 | JWT (jjwt 0.11.5) |
| 工具库 | Lombok |
| 部署 | Docker + Docker Compose + Nginx |

## 项目结构

```
eCommerce-Project/
├── client/                          # 前端项目 (Vue 3 + TypeScript)
│   ├── public/
│   │   └── favicon.ico
│   ├── src/
│   │   ├── components/              # Vue 组件
│   │   │   ├── account/             # 账户相关 (AccountInfo, AccountEditForm, ShowOrders)
│   │   │   ├── admintools/          # 管理员工具 (ProductForm, UsersOrdersTable, UserOrderAside)
│   │   │   ├── footer/              # 页脚 (FooterInfo, FooterNavItems, SocialsIcons)
│   │   │   ├── header/              # 导航栏 (NavBar, SearchBar, AccountItem, LoginItem, ShoppingCartItem)
│   │   │   ├── products/            # 商品相关 (ProductCard, ProductCards, FeaturedProducts, OngoingOrder, PlaceholderCards)
│   │   │   ├── ConfirmDialogue.vue
│   │   │   ├── HeroSection.vue
│   │   │   ├── LoadingOverlay.vue
│   │   │   ├── LoginOrSignupPopup.vue
│   │   │   ├── ProductPreview.vue
│   │   │   └── SmallViewTitle.vue
│   │   ├── router/
│   │   │   └── index.ts             # 路由定义与导航守卫
│   │   ├── stores/                  # Pinia 状态管理
│   │   │   └── network/             # 网络请求相关 Store
│   │   │       ├── accountStore.ts  # 账户与认证状态
│   │   │       ├── adminToolsStore.ts
│   │   │       ├── favoriteStore.ts
│   │   │       ├── orderStore.ts
│   │   │       ├── productStore.ts  # 商品状态（含商户商品管理）
│   │   │       ├── reviewStore.ts
│   │   │       └── requests.ts      # Axios 请求封装（拦截器）
│   │   │   └── shoppingCartStore.ts # 购物车状态
│   │   ├── types/                   # TypeScript 类型定义
│   │   │   ├── api.ts
│   │   │   ├── favorite.ts
│   │   │   ├── order.ts
│   │   │   ├── product.ts
│   │   │   ├── review.ts
│   │   │   └── user.ts
│   │   ├── views/                   # 页面视图
│   │   │   ├── admin/               # 管理员页面
│   │   │   │   ├── AdminToolsView.vue
│   │   │   │   ├── HandleOrdersView.vue
│   │   │   │   ├── HandleProductsView.vue
│   │   │   │   ├── ProductReviewView.vue
│   │   │   │   └── UserManagementView.vue
│   │   │   ├── AccountView.vue
│   │   │   ├── CheckoutView.vue
│   │   │   ├── EditAccountView.vue
│   │   │   ├── FavoritesView.vue
│   │   │   ├── HomeView.vue
│   │   │   ├── LoginView.vue
│   │   │   ├── MyProductsView.vue
│   │   │   ├── ProductView.vue
│   │   │   ├── PublishProductView.vue
│   │   │   ├── ShopView.vue
│   │   │   ├── ShowAccountOrdersView.vue
│   │   │   └── SignupView.vue
│   │   ├── App.vue
│   │   ├── index.css
│   │   └── main.ts
│   ├── Dockerfile                   # 前端 Docker 构建 (Node → Nginx)
│   ├── nginx.conf                   # Nginx 反向代理配置
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── tsconfig.json
│   └── package.json
├── server/                          # 后端项目 (Spring Boot + PostgreSQL)
│   ├── src/main/java/me/code/springboot_postgres/
│   │   ├── config/                  # 配置类
│   │   │   └── DataInitializer.java # 数据初始化 (种子用户)
│   │   ├── controllers/             # REST 控制器
│   │   │   ├── AdminToolsController.java
│   │   │   ├── FavoriteController.java
│   │   │   ├── LoginController.java
│   │   │   ├── OrderController.java
│   │   │   ├── ProductController.java
│   │   │   ├── ReviewController.java
│   │   │   ├── UserAccountController.java
│   │   │   ├── UserManagementController.java
│   │   │   └── UserProductController.java
│   │   ├── dtos/                    # 数据传输对象
│   │   │   ├── requests/            # 请求 DTO (含 Bean Validation)
│   │   │   └── responses/           # 响应 DTO (success/ error/ entities/)
│   │   ├── exceptions/              # 全局异常处理
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── types/               # CustomRuntimeException
│   │   ├── models/                  # JPA 实体模型
│   │   │   └── entities/            # User, Product, Order, OrderItem, Review, Favorite
│   │   ├── repositories/            # Spring Data JPA Repository
│   │   ├── security/                # 安全配置
│   │   │   ├── CorsConfig.java
│   │   │   ├── JwtTokenUtil.java
│   │   │   ├── JwtValidationFilter.java
│   │   │   └── SecurityConfig.java
│   │   └── services/                # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── application.yml          # Spring Boot 配置
│   │   └── db/migration/            # Flyway 数据库迁移脚本
│   │       ├── V1__create_initial_schema.sql
│   │       ├── V2__insert_builtin_accounts.sql
│   │       ├── V3__insert_seed_products.sql
│   │       └── V4__simplify_rbac.sql
│   ├── Dockerfile                   # 后端 Docker 构建 (Maven → JRE)
│   └── pom.xml
├── docs/                            # 项目文档
│   ├── develop-log.md
│   ├── migration-plan.md
│   └── refactoring-plan.md
├── images/                          # 项目截图
├── docker-compose.yml               # Docker Compose 编排
├── .gitignore
└── README.md
```

## 数据库设计

共 5 张表（RBAC 简化后）：

| 表名 | 说明 |
|------|------|
| `users` | 用户表（含 role 枚举字段、is_enabled 禁用状态、is_protected 保护标记） |
| `products` | 商品表（含 status 审核状态、reject_reason 驳回原因） |
| `orders` | 订单表 |
| `order_items` | 订单项表 |
| `favorites` | 收藏表 |
| `reviews` | 评价表 |

> V4 迁移脚本已将原 RBAC 四张表（roles, permissions, role_permissions, user_roles）合并为 users 表的 role 枚举字段。

## 快速开始

### 方式一：Docker 一键部署（推荐）

#### 前提条件
- 已安装 Docker 和 Docker Compose
- Ubuntu 20.04+ / 其他 Linux 发行版 / Windows (WSL2)

#### 部署步骤

1. **克隆项目**
```bash
git clone https://github.com/YuliannusYin/GoodsTradePlatform
cd GoodsTradePlatform
```

2. **一键启动所有服务**
```bash
docker compose up -d --build
```

3. **等待服务启动完成**
```bash
# 查看服务状态
docker compose ps

# 查看后端日志
docker compose logs -f server
```

4. **访问应用**
- 前端页面：http://localhost
- 后端 API：http://localhost:8080/api

5. **默认测试账号**
```
超级管理员：admin@merchandise.com / Admin@2024（不可改名/改密码/删除）
测试商户：merchant@merchandise.com / Merchant@2024（所有内置商品的卖家）
测试用户：testuser@merchandise.com / Test@2024（余额 $10,000,000）
```

6. **停止服务**
```bash
docker compose down
```

7. **清除所有数据（包括数据库卷）**
```bash
docker compose down -v
```

### 方式二：本地开发环境

#### 前提条件
- Node.js 18+
- Java 17+
- Maven 3.9+
- PostgreSQL 16 数据库实例（本地或 Docker）

#### 步骤

1. **启动 PostgreSQL 数据库**

使用 Docker：
```bash
docker run -d \
  --name merchandise-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=merchandise \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16-alpine
```

2. **配置后端数据库连接**

编辑 `server/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/merchandise
    username: postgres
    password: YOUR_POSTGRES_PASSWORD

jwt:
  secret: YOUR_JWT_SECRET_KEY_AT_LEAST_32_CHARACTERS
  expiration-ms: 3600000
```

3. **启动后端**
```bash
cd server
mvn spring-boot:run
```

4. **安装前端依赖并启动**
```bash
cd client
npm install
npm run dev
```

5. **访问应用**
- 前端：http://localhost:5173
- 后端：http://localhost:8080

## API 接口概览

### 认证相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/account/register | 用户注册 | 否 |
| POST | /api/account/login | 用户登录 | 否 |
| GET | /api/account/details | 获取账户详情 | 是 |
| PUT | /api/account/username | 修改用户名 | 是 |
| PUT | /api/account/email | 修改邮箱 | 是 |
| PUT | /api/account/password | 修改密码 | 是 |
| DELETE | /api/account/delete | 删除账户 | 是 |

### 商品相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/products/all | 获取所有已审核商品 | 否 |
| GET | /api/products/featured | 获取热门商品 | 否 |
| GET | /api/products/{id} | 获取商品详情 | 否 |
| GET | /api/products/search | 搜索商品（支持分类） | 否 |
| GET | /api/products/category/{category} | 按分类获取商品 | 否 |
| GET | /api/products/categories | 获取分类列表 | 否 |
| GET | /api/products/conditions | 获取成色列表 | 否 |

### 用户商品（C2C / 商户）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/user_products/add | 商户发布商品 | 商户 |
| GET | /api/user_products/my | 获取我的商品 | 商户 |
| PUT | /api/user_products/edit/{id} | 编辑我的商品 | 商户 |
| DELETE | /api/user_products/delete/{id} | 删除我的商品 | 商户 |

### 订单相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/orders/ongoing | 获取进行中订单预览 | 否 |
| POST | /api/orders/place | 下单 | 是 |
| GET | /api/orders/all | 获取用户订单 | 是 |
| GET | /api/orders/delivery/methods | 获取配送方式 | 否 |
| GET | /api/orders/payment/methods | 获取支付方式 | 否 |

### 评价相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/reviews/add | 添加评价 | 是 |
| GET | /api/reviews/product/{id} | 获取商品评价 | 否 |
| GET | /api/reviews/product/{id}/rating | 获取商品评分 | 否 |
| GET | /api/reviews/user/{id} | 获取用户评价 | 是 |
| DELETE | /api/reviews/{id} | 删除评价 | 是 |

### 收藏相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/favorites/add?productId= | 添加收藏 | 是 |
| DELETE | /api/favorites/remove/{id} | 取消收藏 | 是 |
| GET | /api/favorites/list | 获取收藏列表 | 是 |
| GET | /api/favorites/check/{id} | 检查是否已收藏 | 是 |

### 管理员 - 商品管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/admin_tools/product/add | 添加商品 | ADMIN+ |
| PUT | /api/admin_tools/product/edit/{id} | 编辑商品 | ADMIN+ |
| DELETE | /api/admin_tools/product/delete/{id} | 删除商品 | ADMIN+ |
| PATCH | /api/admin_tools/product/approve/{id} | 审核通过 | ADMIN+ |
| PATCH | /api/admin_tools/product/reject/{id}?rejectReason= | 审核驳回 | ADMIN+ |
| PATCH | /api/admin_tools/product/disable/{id} | 禁用商品 | ADMIN+ |
| PATCH | /api/admin_tools/product/enable/{id} | 启用商品 | ADMIN+ |
| GET | /api/admin_tools/product/pending | 获取待审核商品 | ADMIN+ |
| GET | /api/admin_tools/product/status/{status} | 按状态查商品 | ADMIN+ |

### 管理员 - 订单管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin_tools/order/all | 获取所有订单 | ADMIN+ |
| GET | /api/admin_tools/order/all/{status} | 按状态查订单 | ADMIN+ |
| PATCH | /api/admin_tools/order/send | 发货 | ADMIN+ |
| PATCH | /api/admin_tools/order/expected_delivery | 修改预计送达时间 | ADMIN+ |

### 管理员 - 用户管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin/users/all | 获取用户列表 | SUPER_ADMIN |
| GET | /api/admin/users/{id} | 获取用户详情 | SUPER_ADMIN |
| PUT | /api/admin/users/{id}/role | 分配用户角色 | SUPER_ADMIN |
| PATCH | /api/admin/users/{id}/toggle-enabled | 禁用/启用用户 | SUPER_ADMIN |
| DELETE | /api/admin/users/{id} | 删除用户 | SUPER_ADMIN |

## 页面路由

| 路径 | 页面 | 认证 |
|------|------|------|
| `/` | 首页 | 否 |
| `/shop` | 商城（支持 query/filter/category 参数） | 否 |
| `/product/:productId` | 商品详情 | 否 |
| `/login` | 登录 | 否 |
| `/signup` | 注册 | 否 |
| `/account` | 账户中心 | 是 |
| `/account/edit` | 编辑账户 | 是 |
| `/account/orders` | 我的订单 | 是 |
| `/account/my-products` | 我的商品 | 商户 |
| `/checkout` | 结算页 | 否（需购物车有商品） |
| `/publish` | 发布商品 | 商户 |
| `/favorites` | 我的收藏 | 是 |
| `/admin_tools/products` | 管理商品 | 管理员 |
| `/admin_tools/orders` | 管理订单 | 管理员 |
| `/admin_tools/reviews` | 商品审核 | 管理员 |
| `/admin_tools/users` | 用户管理 | 超级管理员 |

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `POSTGRES_DB` | PostgreSQL 数据库名 | `merchandise` |
| `POSTGRES_USER` | PostgreSQL 用户名 | `postgres` |
| `POSTGRES_PASSWORD` | PostgreSQL 密码 | `merchandise123` |
| `SPRING_DATASOURCE_URL` | 数据库连接地址 | `jdbc:postgresql://localhost:5432/merchandise` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | 同 `POSTGRES_PASSWORD` |
| `JWT_SECRET` | JWT 签名密钥 | `dev-only-secret-key-change-in-production-min-32-chars` |
| `JWT_EXPIRATION_MS` | JWT 过期时间（毫秒） | `3600000`（1 小时） |
| `CORS_ALLOWED_ORIGINS` | CORS 允许的来源 | `*` |
| `SHOW_SQL` | 是否打印 SQL | `false` |

## Docker 架构

```
                    ┌─────────────────┐
                    │   Browser :80   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Nginx (client) │  ← 静态资源 + 反向代理 /api/
                    └────────┬────────┘
                             │ /api/
                    ┌────────▼────────┐
                    │  Spring Boot    │  ← REST API :8080
                    │  (server)       │
                    └────────┬────────┘
                             │ jdbc:postgresql://
                    ┌────────▼────────┐
                    │  PostgreSQL 16  │  ← 关系型数据库 :5432
                    └─────────────────┘
```

## 许可证

本项目为本科毕业设计项目，仅供学习参考。
