# 周边交易平台

![Java](https://img.shields.io/badge/Java-17-orange?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot) ![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vue.js) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql) ![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker) ![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?logo=tailwindcss)

## 项目简介

本项目是一个基于 Vue 3 + Spring Boot + PostgreSQL 的**周边交易平台**，支持 B2C 平台自营和 C2C 用户闲置交易两种模式。用户可以浏览、搜索、购买各类动漫、游戏、偶像周边商品，也可以申请成为商户发布自己的闲置周边进行售卖。系统采用固定角色权限模型，支持超级管理员、管理员、商户、普通用户四种角色。

## 功能特性

### 核心功能

- **用户注册/登录**：JWT 认证，固定角色权限模型（超级管理员、管理员、商户、普通用户）
- **商品浏览与搜索**：支持关键词搜索、价格排序、分类筛选
- **商品分类**：手办、海报、钥匙扣、徽章、抱枕、立牌、服饰、专辑、配件、其他
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

- **SUPER\_ADMIN**：拥有所有权限，包括用户管理
- **ADMIN**：管理商品、订单和审核
- **MERCHANT**：可发布/编辑/删除自己的商品
- **USER**：可浏览、购买、收藏和评价

## 技术栈

| 层级       | 技术                                                                               |
| -------- | -------------------------------------------------------------------------------- |
| 前端       | Vue 3 (`<script setup>` + Composition API) + TypeScript 5 + Pinia + Vue Router 4 |
| UI 框架    | Tailwind CSS 3 + Font Awesome + Headless UI + Heroicons                          |
| 构建工具     | Vite 4                                                                           |
| HTTP 客户端 | Axios                                                                            |
| 后端       | Java 17 + Spring Boot 3.2 + Spring Security + Spring Data JPA + Bean Validation  |
| 数据库      | PostgreSQL 16（关系型数据库，JPA 自动建表）                                                   |
| 认证       | JWT (jjwt 0.11.5)                                                                |
| 工具库      | Lombok + JetBrains Annotations                                                   |
| 部署       | Docker + Docker Compose + Nginx                                                  |

## 详细文档

| 文档 | 说明 |
|------|------|
| [项目结构](docs/project-structure.md) | 前后端目录结构、文件说明 |
| [数据库设计](docs/database-design.md) | 表结构、字段说明、索引设计、实体关系图 |
| [API 接口文档](docs/api-reference.md) | REST API 接口、认证方式、页面路由 |

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

2. **配置环境变量（域名部署时必须）**

如果通过域名访问，需在项目根目录创建 `.env` 文件设置 CORS 允许的来源：

```bash
# .env 文件示例（域名部署时必须设置，否则登录等功能会返回 403）
CORS_ALLOWED_ORIGINS=https://你的域名

# 同时建议修改以下安全相关配置
JWT_SECRET=你的安全密钥（至少32字符）
POSTGRES_PASSWORD=你的数据库密码
```

> **注意**：通过 `http://localhost` 访问时无需额外配置，默认值已包含 localhost。只有通过域名或 IP 访问时才需要设置 `CORS_ALLOWED_ORIGINS`。

3. **一键启动所有服务**

```bash
docker compose up -d --build
```

4. **等待服务启动完成**

```bash
# 查看服务状态
docker compose ps

# 查看后端日志
docker compose logs -f server
```

5. **访问应用**

- 前端页面：<http://localhost>
- 后端 API：<http://localhost:8080/api>

6. **默认测试账号**

```
超级管理员：admin@merchandise.com / Admin@2024（受保护账号，不可改名/改密码/删除）
测试商户：merchant@merchandise.com / Merchant@2024（受保护账号，所有内置商品的卖家）
测试用户：testuser@merchandise.com / Test@2024（受保护账号，余额 $10,000,000）
```

7. **停止服务**

```bash
docker compose down
```

8. **清除所有数据（包括数据库卷）**

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

- 前端：<http://localhost:5173（Vite> 开发服务器自动代理 `/api` 到后端）
- 后端：<http://localhost:8080>

## 环境变量

| 变量名                          | 说明                                | 默认值                                                              |
| ---------------------------- | --------------------------------- | ---------------------------------------------------------------- |
| `POSTGRES_DB`                | PostgreSQL 数据库名                   | `merchandise`                                                    |
| `POSTGRES_USER`              | PostgreSQL 用户名                    | `postgres`                                                       |
| `POSTGRES_PASSWORD`          | PostgreSQL 密码                     | `merchandise123`                                                 |
| `SPRING_DATASOURCE_URL`      | 数据库连接地址                           | `jdbc:postgresql://localhost:5432/merchandise`                   |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名                            | `postgres`                                                       |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码                             | 同 `POSTGRES_PASSWORD`                                            |
| `JWT_SECRET`                 | JWT 签名密钥                          | `please-change-this-to-a-secure-random-secret-key-in-production` |
| `JWT_EXPIRATION_MS`          | JWT 过期时间（毫秒）                      | `3600000`（1 小时）                                                  |
| `CORS_ALLOWED_ORIGINS`       | CORS 允许的来源（多个用逗号分隔，**域名部署时必须设置**） | `http://localhost,http://localhost:5173`                         |
| `SHOW_SQL`                   | 是否打印 SQL                          | `false`                                                          |

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

### 容器说明

| 容器                     | 镜像                      | 端口        | 说明                    |
| ---------------------- | ----------------------- | --------- | --------------------- |
| `merchandise-client`   | 自定义（Node 构建 → Nginx 运行） | 80:80     | 前端静态资源 + API 反向代理     |
| `merchandise-server`   | 自定义（Maven 构建 → JRE 运行）  | 8080:8080 | Spring Boot 后端服务      |
| `merchandise-postgres` | postgres:16-alpine      | 5432:5432 | PostgreSQL 数据库（含健康检查） |

## 许可证

本项目为课程设计项目，仅供学习参考。
