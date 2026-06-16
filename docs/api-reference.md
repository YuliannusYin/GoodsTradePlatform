# API 接口文档

> 本文档从 [README.md](../README.md) 拆分而来，详细描述项目的 REST API 接口。

---

## 通用说明

### 基础路径

```
/api
```

### 认证方式

所有需要认证的接口需在请求头中携带 JWT 令牌：

```
Authorization: Bearer <token>
```

### 统一响应格式

```json
{
  "status": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 角色层级

- `SUPER_ADMIN` > `ADMIN` > `MERCHANT` > `USER`
- `ADMIN+` 表示 `ADMIN` 及以上角色可访问
- `MERCHANT+` 表示 `MERCHANT` 及以上角色可访问

---

## 认证相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/account/register | 用户注册 | 否 |
| POST | /api/account/login | 用户登录 | 否 |
| POST | /api/account/confirm | 验证凭据 | 是 |
| GET | /api/account/details | 获取账户详情 | 是 |
| PUT | /api/account/username | 修改用户名 | 是 |
| PUT | /api/account/email | 修改邮箱 | 是 |
| PUT | /api/account/password | 修改密码 | 是 |
| DELETE | /api/account/delete | 删除账户 | 是 |

---

## 商品相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/products/all | 获取所有已审核商品 | 否 |
| GET | /api/products/featured | 获取精选商品（库存降序前 4） | 否 |
| GET | /api/products/{productId} | 获取商品详情 | 否 |
| GET | /api/products/search | 搜索商品（支持分类、排序） | 否 |
| GET | /api/products/category/{category} | 按分类获取商品 | 否 |
| GET | /api/products/categories | 获取所有分类枚举 | 否 |
| GET | /api/products/conditions | 获取所有成色枚举 | 否 |

### 搜索参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `query` | String | 搜索关键词 |
| `filter` | String | 排序方式：`lowest_price`、`highest_price` |
| `category` | String | 分类筛选 |

---

## 购物车相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/cart | 获取购物车 | 是 |
| POST | /api/cart/items | 添加商品到购物车 | 是 |
| PUT | /api/cart/items/{productId}?quantity= | 更新购物车商品数量 | 是 |
| DELETE | /api/cart/items/{productId} | 移除购物车商品 | 是 |
| DELETE | /api/cart | 清空购物车 | 是 |
| POST | /api/cart/merge | 合并购物车（登录后） | 是 |

---

## 订单相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/orders/ongoing | 获取进行中订单预览 | 否 |
| POST | /api/orders/place | 下单（余额支付） | 是 |
| GET | /api/orders/all | 获取用户订单 | 是 |
| GET | /api/orders/delivery/methods | 获取配送方式枚举 | 否 |
| GET | /api/orders/payment/methods | 获取支付方式枚举 | 否 |

---

## 评价相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/reviews/add | 添加评价 | 是 |
| GET | /api/reviews/product/{productId} | 获取商品评价列表 | 否 |
| GET | /api/reviews/product/{productId}/rating | 获取商品评分统计 | 否 |
| GET | /api/reviews/user/{userId} | 获取用户评价 | 是 |
| DELETE | /api/reviews/{reviewId} | 删除评价（所有者或管理员） | 是 |

---

## 收藏相关

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/favorites/add?productId= | 添加收藏 | 是 |
| DELETE | /api/favorites/remove/{productId} | 取消收藏 | 是 |
| GET | /api/favorites/list | 获取收藏列表 | 是 |
| GET | /api/favorites/check/{productId} | 检查是否已收藏 | 是 |

---

## 用户商品（C2C / 商户）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/user_products/add | 商户发布商品 | MERCHANT+ |
| GET | /api/user_products/my | 获取我的商品 | MERCHANT+ |
| PUT | /api/user_products/edit/{productId} | 编辑我的商品 | MERCHANT+ |
| DELETE | /api/user_products/delete/{productId} | 删除我的商品 | MERCHANT+ |

---

## 管理员 - 商品管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/admin_tools/product/add | 添加商品（直接已审核） | ADMIN+ |
| PUT | /api/admin_tools/product/edit/{productId} | 编辑商品 | ADMIN+ |
| DELETE | /api/admin_tools/product/delete/{productId} | 删除商品 | ADMIN+ |
| GET | /api/admin_tools/product/pending | 获取待审核商品 | ADMIN+ |
| GET | /api/admin_tools/product/status/{status} | 按状态查商品 | ADMIN+ |
| PATCH | /api/admin_tools/product/approve/{productId} | 审核通过 | ADMIN+ |
| PATCH | /api/admin_tools/product/reject/{productId}?rejectReason= | 审核驳回 | ADMIN+ |
| PATCH | /api/admin_tools/product/disable/{productId} | 禁用商品 | ADMIN+ |
| PATCH | /api/admin_tools/product/enable/{productId} | 启用商品 | ADMIN+ |

---

## 管理员 - 订单管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin_tools/order/all | 获取所有订单 | ADMIN+ |
| GET | /api/admin_tools/order/all/{status} | 按状态查订单 | ADMIN+ |
| PATCH | /api/admin_tools/order/send | 发货并设置预计送达时间 | ADMIN+ |
| PATCH | /api/admin_tools/order/expected_delivery | 修改预计送达时间 | ADMIN+ |

---

## 管理员 - 用户管理

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /api/admin/users/all | 获取用户列表 | SUPER_ADMIN |
| GET | /api/admin/users/{userId} | 获取用户详情 | SUPER_ADMIN |
| PUT | /api/admin/users/{userId}/role | 分配用户角色 | SUPER_ADMIN |
| PATCH | /api/admin/users/{userId}/toggle-enabled | 禁用/启用用户 | SUPER_ADMIN |
| DELETE | /api/admin/users/{userId} | 删除用户 | SUPER_ADMIN |
| PUT | /api/admin/users/balance | 调整用户余额 | SUPER_ADMIN |

---

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
| `/cart` | 购物车 | 否 |
| `/checkout` | 结算页 | 否（需购物车有商品） |
| `/publish` | 发布商品 | 商户 |
| `/favorites` | 我的收藏 | 是 |
| `/admin_tools` | 管理工具主页 | 管理员 |
| `/admin_tools/products` | 管理商品 | 管理员 |
| `/admin_tools/products/add` | 添加商品 | 管理员 |
| `/admin_tools/products/edit` | 编辑商品 | 管理员 |
| `/admin_tools/products/delete` | 删除商品 | 管理员 |
| `/admin_tools/orders` | 管理订单 | 管理员 |
| `/admin_tools/orders/pending` | 待发货订单 | 管理员 |
| `/admin_tools/orders/sent` | 已发货订单 | 管理员 |
| `/admin_tools/orders/all` | 所有订单 | 管理员 |
| `/admin_tools/reviews` | 商品审核 | 管理员 |
| `/admin_tools/users` | 用户管理 | 超级管理员 |
