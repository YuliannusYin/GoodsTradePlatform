# 项目结构

> 本文档从 [README.md](../README.md) 拆分而来，详细描述项目的前后端目录结构。

---

## 整体结构

```
eCommerce-Project/
├── client/                          # 前端项目 (Vue 3 + TypeScript)
├── server/                          # 后端项目 (Spring Boot + PostgreSQL)
├── docs/                            # 项目文档
│   ├── project-structure.md         # 项目结构（本文件）
│   ├── database-design.md           # 数据库设计
│   └── api-reference.md             # API 接口文档
├── docker-compose.yml               # Docker Compose 编排
├── .gitignore
└── README.md
```

---

## 前端结构 (client/)

```
client/
├── public/
│   └── favicon.ico
├── src/
│   ├── components/                  # Vue 组件
│   │   ├── account/                 # 账户相关
│   │   │   ├── AccountEditForm.vue  # 账户编辑表单
│   │   │   ├── AccountInfo.vue      # 账户信息展示
│   │   │   └── ShowOrders.vue       # 订单列表展示
│   │   ├── admintools/              # 管理员工具
│   │   │   ├── AdminToolsItem.vue   # 管理工具导航项
│   │   │   ├── AdminToolsPopup.vue  # 管理工具弹窗
│   │   │   ├── ProductForm.vue      # 商品表单（新增/编辑/删除三种模式）
│   │   │   ├── UserOrderAside.vue   # 用户订单侧边栏
│   │   │   └── UsersOrdersTable.vue # 用户订单表格
│   │   ├── cart/                    # 购物车相关
│   │   │   ├── CartItemList.vue     # 购物车商品列表
│   │   │   ├── CartItemRow.vue      # 购物车商品行
│   │   │   └── CartSummary.vue      # 购物车汇总
│   │   ├── checkout/                # 结算相关
│   │   │   ├── CheckoutAddress.vue  # 结算地址
│   │   │   ├── CheckoutBalance.vue  # 结算余额
│   │   │   ├── CheckoutConfirm.vue  # 结算确认
│   │   │   ├── CheckoutDelivery.vue # 结算配送方式
│   │   │   ├── CheckoutItems.vue    # 结算商品列表
│   │   │   ├── CheckoutStepper.vue  # 结算步骤条
│   │   │   └── OrderSuccess.vue     # 下单成功
│   │   ├── footer/                  # 页脚
│   │   │   ├── FooterInfo.vue       # 页脚信息
│   │   │   ├── FooterNavItems.vue   # 页脚导航链接
│   │   │   └── SocialsIcons.vue     # 社交媒体图标
│   │   ├── header/                  # 导航栏
│   │   │   ├── NavBar.vue           # 顶部导航栏
│   │   │   ├── SearchBar.vue        # 搜索栏
│   │   │   ├── AccountItem.vue      # 账户入口
│   │   │   ├── LoginItem.vue        # 登录入口
│   │   │   └── ShoppingCartItem.vue # 购物车入口
│   │   ├── products/                # 商品相关
│   │   │   ├── ProductCard.vue      # 商品卡片（列表/详情双模式）
│   │   │   ├── ProductCards.vue     # 商品卡片网格
│   │   │   ├── ProductDetailCard.vue # 商品详情卡片
│   │   │   ├── ProductPublishForm.vue # 商品发布表单
│   │   │   ├── FeaturedProducts.vue # 精选商品
│   │   │   ├── MyProductsList.vue   # 我的商品列表（商户）
│   │   │   └── PlaceholderCards.vue # 占位骨架屏
│   │   ├── ConfirmDialogue.vue      # 确认对话框
│   │   ├── HeroSection.vue          # 首页横幅
│   │   ├── LoginOrSignupPopup.vue   # 登录/注册弹窗
│   │   ├── ProductPreview.vue       # 商品预览
│   │   └── SmallViewTitle.vue       # 小视图标题
│   ├── router/
│   │   └── index.ts                 # 路由定义与导航守卫
│   ├── stores/                      # Pinia 状态管理
│   │   ├── network/                 # 网络请求相关 Store
│   │   │   ├── accountStore.ts      # 账户与认证状态
│   │   │   ├── adminToolsStore.ts   # 管理员工具状态
│   │   │   ├── favoriteStore.ts     # 收藏状态
│   │   │   ├── orderStore.ts        # 订单状态
│   │   │   ├── productStore.ts      # 商品状态（含商户商品管理）
│   │   │   ├── reviewStore.ts       # 评价状态
│   │   │   └── requests.ts          # Axios 请求封装（拦截器）
│   │   └── shoppingCartStore.ts     # 购物车状态（本地）
│   ├── types/                       # TypeScript 类型定义
│   │   ├── api.ts                   # API 通用类型
│   │   ├── cart.ts                  # 购物车类型
│   │   ├── favorite.ts              # 收藏类型
│   │   ├── order.ts                 # 订单类型
│   │   ├── product.ts               # 商品类型
│   │   ├── review.ts                # 评价类型
│   │   └── user.ts                  # 用户类型
│   ├── views/                       # 页面视图
│   │   ├── admin/                   # 管理员页面
│   │   │   ├── AdminToolsView.vue   # 管理工具主页
│   │   │   ├── HandleOrdersView.vue # 订单管理
│   │   │   ├── HandleProductsView.vue # 商品管理
│   │   │   ├── ProductReviewView.vue # 商品审核
│   │   │   └── UserManagementView.vue # 用户管理
│   │   ├── AccountView.vue          # 账户中心
│   │   ├── CartView.vue             # 购物车
│   │   ├── CheckoutView.vue         # 结算页
│   │   ├── EditAccountView.vue      # 编辑账户
│   │   ├── FavoritesView.vue        # 我的收藏
│   │   ├── HomeView.vue             # 首页
│   │   ├── LoginView.vue            # 登录
│   │   ├── MyProductsView.vue       # 我的商品（商户）
│   │   ├── ProductView.vue          # 商品详情
│   │   ├── PublishProductView.vue   # 发布商品（商户）
│   │   ├── ShopView.vue             # 商城
│   │   ├── ShowAccountOrdersView.vue # 我的订单
│   │   └── SignupView.vue           # 注册
│   ├── App.vue                      # 根组件
│   ├── index.css                    # 全局样式
│   └── main.ts                      # 应用入口
├── Dockerfile                       # 前端 Docker 构建 (Node → Nginx)
├── nginx.conf                       # Nginx 反向代理配置
├── vite.config.ts                   # Vite 配置（含 API 代理）
├── tailwind.config.js               # Tailwind CSS 配置（自定义主题色）
├── tsconfig.json                    # TypeScript 配置
└── package.json
```

---

## 后端结构 (server/)

```
server/
├── src/main/java/me/code/springboot_postgres/
│   ├── config/                      # 配置类
│   │   └── DataInitializer.java     # 数据初始化（启动时确保内置用户和测试商品存在）
│   ├── controllers/                 # REST 控制器
│   │   ├── AdminToolsController.java      # 管理员工具（商品/订单管理）
│   │   ├── CartController.java            # 购物车管理
│   │   ├── FavoriteController.java        # 收藏管理
│   │   ├── LoginController.java           # 登录认证
│   │   ├── OrderController.java           # 订单管理
│   │   ├── ProductController.java         # 商品浏览/搜索
│   │   ├── ReviewController.java          # 评价管理
│   │   ├── UserAccountController.java     # 用户账户管理
│   │   ├── UserManagementController.java  # 用户管理（超级管理员）
│   │   └── UserProductController.java     # 商户商品管理
│   ├── dtos/                        # 数据传输对象
│   │   ├── requests/                # 请求 DTO（含 Bean Validation）
│   │   │   ├── AddToCartDTO.java          # 添加购物车请求
│   │   │   ├── AdjustBalanceDTO.java      # 调整余额请求
│   │   │   ├── AssignRoleDTO.java         # 分配角色请求
│   │   │   ├── ChangeEmailDTO.java        # 修改邮箱请求
│   │   │   ├── ChangePasswordDTO.java     # 修改密码请求
│   │   │   ├── ChangeUsernameDTO.java     # 修改用户名请求
│   │   │   ├── CreateReviewDTO.java       # 创建评价请求
│   │   │   ├── CreateUserDTO.java         # 创建用户请求
│   │   │   ├── MergeCartDTO.java          # 合并购物车请求
│   │   │   ├── OrderDeliveryDTO.java      # 订单发货请求
│   │   │   ├── PlaceOrderDTO.java         # 下单请求
│   │   │   ├── ProductDTO.java            # 商品请求
│   │   │   └── UserLoginDTO.java          # 用户登录请求
│   │   └── responses/              # 响应 DTO
│   │       ├── ApiResponse.java           # 统一 API 响应包装
│   │       ├── AuthenticationDTO.java     # 认证响应
│   │       ├── CartDTO.java               # 购物车响应
│   │       ├── FavoriteDTO.java           # 收藏响应
│   │       ├── OngoingOrderDTO.java       # 进行中订单响应
│   │       ├── OrderDTO.java              # 订单响应
│   │       ├── ProductDTO.java            # 商品响应
│   │       ├── ProductRatingDTO.java      # 商品评分响应
│   │       ├── ReviewDTO.java             # 评价响应
│   │       ├── UnavailableProductDTO.java # 不可用商品响应
│   │       ├── UserDTO.java               # 用户响应
│   │       ├── UserDetailsDTO.java        # 用户详情响应
│   │       └── UserOrderDTO.java          # 用户订单响应
│   ├── exceptions/                  # 全局异常处理
│   │   ├── GlobalExceptionHandler.java   # 全局异常处理器
│   │   └── types/
│   │       └── CustomRuntimeException.java # 自定义运行时异常
│   ├── models/                      # JPA 实体模型
│   │   └── entities/
│   │       ├── CartItem.java        # 购物车项实体
│   │       ├── Favorite.java        # 收藏实体
│   │       ├── Order.java           # 订单实体（含 Status/DeliveryMethod/PaymentMethod 枚举）
│   │       ├── OrderItem.java       # 订单项实体
│   │       ├── Product.java         # 商品实体（含 Category/Condition/Status/Source 枚举）
│   │       ├── Review.java          # 评价实体
│   │       └── User.java            # 用户实体（含 Role 枚举，实现 UserDetails）
│   ├── repositories/                # Spring Data JPA Repository
│   │   ├── CartItemRepository.java        # 购物车项仓库
│   │   ├── FavoriteRepository.java        # 收藏仓库
│   │   ├── OrderRepository.java           # 订单仓库
│   │   ├── ProductRepository.java         # 商品仓库
│   │   ├── ProductSpecifications.java     # 商品动态查询条件构建
│   │   ├── ReviewRepository.java          # 评价仓库
│   │   └── UserRepository.java            # 用户仓库
│   ├── security/                    # 安全配置
│   │   ├── CorsConfig.java                # CORS 跨域配置
│   │   ├── JwtTokenUtil.java              # JWT 令牌生成与解析
│   │   ├── JwtValidationFilter.java       # JWT 请求验证过滤器
│   │   └── SecurityConfig.java            # 安全过滤链与权限配置
│   ├── services/                    # 业务逻辑层
│   │   ├── AdminToolsService.java         # 管理员工具服务
│   │   ├── CartService.java               # 购物车服务
│   │   ├── FavoriteService.java           # 收藏服务
│   │   ├── OrderItemService.java          # 订单项服务
│   │   ├── OrderService.java              # 订单服务
│   │   ├── ProductService.java            # 商品服务
│   │   ├── ReviewService.java             # 评价服务
│   │   ├── UserAccountService.java        # 用户账户服务（含 UserDetailsService）
│   │   ├── UserManagementService.java     # 用户管理服务
│   │   └── UserProductService.java        # 商户商品服务
│   └── Application.java             # Spring Boot 启动类
├── src/main/resources/
│   └── application.yml              # Spring Boot 配置
├── src/test/                        # 测试
├── Dockerfile                       # 后端 Docker 构建 (Maven → JRE)
└── pom.xml
```
