# 周边交易平台

![Java](https://img.shields.io/badge/Java-17-orange?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=springboot) ![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vue.js) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql) ![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

## 项目简介

本项目是一个基于 Vue 3 + Spring Boot + Neo4j 的**周边交易平台**，支持 B2C 平台自营和 C2C 用户闲置交易两种模式。用户可以浏览、搜索、购买各类动漫、游戏、偶像周边商品，也可以发布自己的闲置周边进行售卖。

## 功能特性

### 核心功能
- **用户注册/登录**：JWT 认证，支持用户和管理员两种角色
- **商品浏览与搜索**：支持关键词搜索、价格排序、分类筛选
- **商品分类**：手办、海报、钥匙扣、徽章、抱枕、立牌、服饰、专辑、配件等
- **商品成色**：全新、几乎全新、良好、一般
- **购物车**：添加/移除商品，实时计算价格
- **订单管理**：下单、查看订单状态、管理员发货

### 新增功能
- **商品收藏**：用户可收藏感兴趣的商品，在收藏夹中统一管理
- **商品评价/评分**：购买后可对商品进行 1-5 星评分和文字评价
- **分类筛选**：商城页面左侧分类导航，快速筛选目标品类
- **用户发布商品（C2C）**：普通用户可发布闲置周边，标记为"个人闲置"
- **商品多图展示**：支持多张商品图片

### 管理员功能
- 商品增删改查
- 订单管理（查看、发货、修改预计送达时间）

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API) + TypeScript + Pinia + Tailwind CSS |
| 后端 | Java 17 + Spring Boot 3.2 + Spring Security + Spring Data Neo4j |
| 数据库 | Neo4j (图数据库) |
| 认证 | JWT (jjwt) |
| 部署 | Docker + Docker Compose + Nginx |

## 项目结构

```
├── client/                  # 前端项目
│   ├── src/
│   │   ├── components/      # Vue 组件
│   │   │   ├── account/     # 账户相关组件
│   │   │   ├── admintools/  # 管理员工具组件
│   │   │   ├── footer/      # 页脚组件
│   │   │   ├── header/      # 导航栏组件
│   │   │   └── products/    # 商品相关组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── types/           # TypeScript 类型定义
│   │   └── views/           # 页面视图
│   ├── Dockerfile
│   └── nginx.conf
├── server/                  # 后端项目
│   ├── src/main/java/me/code/springboot_neo4j/
│   │   ├── config/          # 配置类
│   │   ├── controllers/     # REST 控制器
│   │   ├── dtos/            # 数据传输对象
│   │   ├── exceptions/      # 异常处理
│   │   ├── models/          # Neo4j 节点模型
│   │   ├── repositories/    # 数据访问层
│   │   ├── security/        # 安全配置
│   │   └── services/        # 业务逻辑层
│   └── Dockerfile
├── docker-compose.yml       # Docker 编排文件
└── README.md
```

## 快速开始

### 方式一：Docker 一键部署（推荐，适用于 Ubuntu）

#### 前提条件
- 已安装 Docker 和 Docker Compose
- Ubuntu 20.04+ / 其他 Linux 发行版

#### 部署步骤

1. **克隆项目**
```bash
git clone https://github.com/your-username/eCommerce-Project.git
cd eCommerce-Project
```

2. **一键启动所有服务**
```bash
docker compose up -d --build
```

3. **等待服务启动完成**（首次启动需要下载镜像和编译，约 3-5 分钟）
```bash
# 查看服务状态
docker compose ps

# 查看后端日志
docker compose logs -f server
```

4. **访问应用**
- 前端页面：http://localhost
- 后端 API：http://localhost:8080/api
- Neo4j 管理界面：http://localhost:7474

5. **默认测试账号**
```
管理员：admin@admin.com / Password
普通用户：user@user.com / Password
```

6. **停止服务**
```bash
docker compose down
```

7. **清除所有数据（包括数据库）**
```bash
docker compose down -v
```

### 方式二：本地开发环境

#### 前提条件
- Node.js 18+
- Java 17+
- Maven 3.9+
- Neo4j 数据库实例（本地或 AuraDB）

#### 步骤

1. **配置 Neo4j 连接**

编辑 `server/src/main/resources/application.yml`：
```yaml
spring:
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: YOUR_NEO4J_PASSWORD
```

2. **启动后端**
```bash
cd server
mvn spring-boot:run
```

3. **安装前端依赖并启动**
```bash
cd client
npm install
npm run dev
```

4. **访问应用**
- 前端：http://localhost:5173
- 后端：http://localhost:8080

## API 接口概览

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/account/register | 用户注册 | 否 |
| POST | /api/account/login | 用户登录 | 否 |
| GET | /api/products/all | 获取所有商品 | 否 |
| GET | /api/products/featured | 获取热门商品 | 否 |
| GET | /api/products/{id} | 获取商品详情 | 否 |
| GET | /api/products/search | 搜索商品（支持分类） | 否 |
| GET | /api/products/categories | 获取分类列表 | 否 |
| POST | /api/orders/place | 下单 | 是 |
| GET | /api/orders/all | 获取用户订单 | 是 |
| POST | /api/reviews/add | 添加评价 | 是 |
| GET | /api/reviews/product/{id} | 获取商品评价 | 否 |
| POST | /api/favorites/add | 添加收藏 | 是 |
| DELETE | /api/favorites/remove/{id} | 取消收藏 | 是 |
| GET | /api/favorites/list | 获取收藏列表 | 是 |
| POST | /api/user_products/add | 用户发布商品 | 是 |
| GET | /api/user_products/my | 获取我的商品 | 是 |

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_NEO4J_URI | Neo4j 连接地址 | bolt://localhost:7687 |
| SPRING_NEO4J_AUTHENTICATION_USERNAME | Neo4j 用户名 | neo4j |
| SPRING_NEO4J_AUTHENTICATION_PASSWORD | Neo4j 密码 | - |

## 许可证

本项目为《互联网应用开发》课程期末大作业，仅供学习参考。
