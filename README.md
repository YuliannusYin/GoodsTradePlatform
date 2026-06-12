# 周边交易平台

![Java](https://img.shields.io/badge/Java-17-orange?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot) ![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vue.js) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql) ![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker) ![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?logo=tailwindcss)

## 项目简介

本项目是一个基于 Vue 3 + Spring Boot + PostgreSQL 的**周边交易平台**，支持 B2C 平台自营和 C2C 用户闲置交易两种模式。用户可以浏览、搜索、购买各类动漫、游戏、偶像周边商品，也可以发布自己的闲置周边进行售卖。系统采用 RBAC 权限模型，支持细粒度的角色与权限管理。

## 功能特性

### 核心功能
- **用户注册/登录**：JWT 认证，RBAC 权限模型（超级管理员、管理员、普通用户）
- **商品浏览与搜索**：支持关键词搜索、价格排序、分类筛选
- **商品分类**：手办、海报、钥匙扣、徽章、抱枕、立牌、服饰、专辑、配件等
- **商品成色**：全新、几乎全新、良好、一般
- **购物车**：添加/移除商品，实时计算价格
- **订单管理**：下单、查看订单状态、管理员发货

### 扩展功能
- **商品收藏**：用户可收藏感兴趣的商品，在收藏夹中统一管理
- **商品评价/评分**：购买后可对商品进行 1-5 星评分和文字评价
- **分类筛选**：商城页面左侧分类导航，快速筛选目标品类
- **用户发布商品（C2C）**：普通用户可发布闲置周边，标记为"个人闲置"
- **商品审核**：用户发布的商品需经管理员审核（通过/驳回），平台商品自动通过
- **商品多图展示**：支持多张商品图片

### 管理员功能
- 商品增删改查、审核（通过/驳回）
- 订单管理（查看、发货、修改预计送达时间）
- 用户管理（查看、编辑、禁用/启用、分配角色）
- 角色管理（创建、编辑、删除角色及权限分配）

### RBAC 权限系统
- **SUPER_ADMIN**：拥有所有权限，包括角色管理
- **ADMIN**：管理商品和订单，无角色管理权限
- **USER**：可发布商品、购买、收藏和评价

权限模块：角色管理、用户管理、商品管理、订单管理、个人商品、购物

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API) + TypeScript 5 + Pinia + Vue Router 4 |
| UI 框架 | Tailwind CSS 3 + Headless UI + Heroicons + Font Awesome |
| 构建工具 | Vite 4 |
| 后端 | Java 17 + Spring Boot 3.2 + Spring Security + Spring Data JPA |
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
│   │   │   ├── account/             # 账户相关 (信息、修改、删除、订单)
│   │   │   ├── admintools/          # 管理员工具 (增删改商品、订单管理)
│   │   │   ├── footer/              # 页脚 (导航、信息、社交图标)
│   │   │   ├── header/              # 导航栏 (搜索、购物车、账户、汉堡菜单)
│   │   │   └── products/            # 商品相关 (卡片、搜索、收藏、下单)
│   │   ├── router/                  # Vue Router 路由配置
│   │   │   ├── index.ts             # 路由定义与导航守卫
│   │   │   └── navigationProvider.ts
│   │   ├── stores/                  # Pinia 状态管理
│   │   │   ├── network/             # 网络请求相关 Store
│   │   │   │   ├── accountStore.ts  # 账户状态
│   │   │   │   ├── adminToolsStore.ts
│   │   │   │   ├── connectionStore.ts
│   │   │   │   ├── favoriteStore.ts
│   │   │   │   ├── loadingStore.ts
│   │   │   │   ├── orderStore.ts
│   │   │   │   ├── productStore.ts
│   │   │   │   ├── requests.ts      # Axios 请求封装
│   │   │   │   ├── reviewStore.ts
│   │   │   │   ├── roleManagementStore.ts
│   │   │   │   └── userProductStore.ts
│   │   │   ├── authenticationStore.ts # 认证状态
│   │   │   └── shoppingCartStore.ts   # 购物车状态
│   │   ├── types/                   # TypeScript 类型定义
│   │   │   ├── favorite.ts
│   │   │   ├── order.ts
│   │   │   ├── product.ts
│   │   │   └── review.ts
│   │   ├── views/                   # 页面视图
│   │   │   ├── admin/               # 管理员页面
│   │   │   │   ├── AdminToolsView.vue
│   │   │   │   ├── HandleOrdersView.vue
│   │   │   │   ├── HandleProductsView.vue
│   │   │   │   ├── ProductReviewView.vue
│   │   │   │   ├── RoleManagementView.vue
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
│   │   │   ├── RoleManagementController.java
│   │   │   ├── UserAccountController.java
│   │   │   ├── UserManagementController.java
│   │   │   └── UserProductController.java
│   │   ├── dtos/                    # 数据传输对象
│   │   │   ├── requests/            # 请求 DTO
│   │   │   └── responses/           # 响应 DTO (success/ error/ entities/)
│   │   ├── exceptions/              # 全局异常处理
│   │   │   └── types/               # 自定义异常 (Validation/Order)
│   │   ├── models/                  # JPA 实体模型
│   │   │   └── entities/            # User, Product, Order, OrderItem, Review, Favorite, Role, Permission
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
│   │       └── V3__insert_seed_products.sql
│   ├── Dockerfile                   # 后端 Docker 构建 (Maven → JRE)
│   └── pom.xml
├── images/                          # 项目截图
│   ├── home_page.png
│   ├── product_search.png
│   ├── ongoing_order.png
│   ├── user_orders.png
│   ├── admin_ui.png
│   └── admin_orders.png
├── docker-compose.yml               # Docker Compose 编排
├── .gitignore
└── README.md
```

## 数据库设计

共 10 张表：

| 表名 | 说明 |
|------|------|
| `users` | 用户表（含 is_enabled 禁用状态、is_protected 保护标记） |
| `products` | 商品表（含 status 审核状态、reject_reason 驳回原因） |
| `orders` | 订单表 |
| `order_items` | 订单项表 |
| `favorites` | 收藏表 |
| `reviews` | 评价表 |
| `roles` | 角色表（RBAC） |
| `permissions` | 权限表（RBAC，按 module 分组） |
| `role_permissions` | 角色-权限关联表 |
| `user_roles` | 用户-角色关联表 |

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

### 商品相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/products/all | 获取所有商品 | 否 |
| GET | /api/products/featured | 获取热门商品 | 否 |
| GET | /api/products/{id} | 获取商品详情 | 否 |
| GET | /api/products/search | 搜索商品（支持分类） | 否 |
| GET | /api/products/categories | 获取分类列表 | 否 |

### 订单相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/orders/place | 下单 | 是 |
| GET | /api/orders/all | 获取用户订单 | 是 |

### 评价相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/reviews/add | 添加评价 | 是 |
| GET | /api/reviews/product/{id} | 获取商品评价 | 否 |

### 收藏相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/favorites/add | 添加收藏 | 是 |
| DELETE | /api/favorites/remove/{id} | 取消收藏 | 是 |
| GET | /api/favorites/list | 获取收藏列表 | 是 |

### 用户商品（C2C）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/user_products/add | 用户发布商品 | 是 |
| GET | /api/user_products/my | 获取我的商品 | 是 |

### 管理员 - 商品管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/admin_tools/products/add | 添加商品 | 管理员 |
| PUT | /api/admin_tools/products/edit | 编辑商品 | 管理员 |
| DELETE | /api/admin_tools/products/delete/{id} | 删除商品 | 管理员 |

### 管理员 - 订单管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin_tools/orders/all | 获取所有订单 | 管理员 |
| PUT | /api/admin_tools/orders/send | 发货 | 管理员 |
| PUT | /api/admin_tools/orders/expected_delivery | 修改预计送达时间 | 管理员 |

### 管理员 - 商品审核

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin_tools/reviews/pending | 获取待审核商品 | 管理员 |
| PUT | /api/admin_tools/reviews/{id}/approve | 审核通过 | 管理员 |
| PUT | /api/admin_tools/reviews/{id}/reject | 审核驳回 | 管理员 |

### 管理员 - 用户管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin/users | 获取用户列表 | 超级管理员/管理员 |
| PUT | /api/admin/users/{id} | 编辑用户信息 | 超级管理员/管理员 |
| PUT | /api/admin/users/{id}/roles | 分配用户角色 | 超级管理员/管理员 |
| PUT | /api/admin/users/{id}/toggle-enabled | 禁用/启用用户 | 超级管理员/管理员 |

### 管理员 - 角色管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin/roles | 获取角色列表 | 超级管理员 |
| POST | /api/admin/roles | 创建角色 | 超级管理员 |
| PUT | /api/admin/roles/{id} | 编辑角色及权限 | 超级管理员 |
| DELETE | /api/admin/roles/{id} | 删除角色 | 超级管理员 |

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
| `/account/my-products` | 我的商品 | 是 |
| `/checkout` | 结算页 | 否（需购物车有商品） |
| `/publish` | 发布商品 | 是 |
| `/favorites` | 我的收藏 | 是 |
| `/admin_tools/products` | 管理商品 | 管理员 |
| `/admin_tools/orders` | 管理订单 | 管理员 |
| `/admin_tools/reviews` | 商品审核 | 管理员 |
| `/admin_tools/users` | 用户管理 | 管理员 |
| `/admin_tools/roles` | 角色管理 | 超级管理员 |

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

本项目为《互联网应用开发》课程期末大作业，仅供学习参考。
