# 前端待修复问题清单

> 生成日期：2026-06-16
> 本文档记录前端代码审查中发现的尚未修复的问题，按严重程度分类。

---

## 高优先级（功能缺陷/Bug）

### 1. NavBar 搜索框与汉堡菜单可同时打开
- **文件**: `client/src/components/header/NavBar.vue`
- **问题**: `toggleSearchInput` 和 `toggleAsideVisibility` 互不干扰，用户可同时打开搜索框和汉堡菜单，导致界面混乱
- **建议**: 打开一个时自动关闭另一个

### 2. 移动端登录/注册弹出菜单永远不可见
- **文件**: `client/src/components/header/LoginItem.vue`, `client/src/components/LoginOrSignupPopup.vue`
- **问题**: `LoginOrSignupPopup` 使用 `hidden md:block` CSS，移动端永远不显示；移动端用户悬停"登录"后无任何反馈
- **建议**: 为移动端提供替代交互方式（如点击跳转登录页），或移除 `hidden md:block` 改用 JS 控制

### 3. SearchBar 筛选条件变更后不自动触发搜索
- **文件**: `client/src/components/header/SearchBar.vue`
- **问题**: `handleFilterChange` 仅修改响应式状态，不会自动调用 `handleSearch`，用户勾选排序后必须手动按 Enter
- **建议**: 筛选条件变更后自动执行搜索

### 4. AdminToolsItem hover 弹出菜单可能闪烁关闭
- **文件**: `client/src/components/admintools/AdminToolsItem.vue`
- **问题**: `@mouseover/@mouseleave` 绑定在父 div 上，弹出菜单使用 absolute 定位，鼠标移出父元素边界时菜单立即关闭
- **建议**: 改用 click 触发或增加过渡延迟/padding

### 5. UsersOrdersTable 中 UserOrderAside 放在 table 标签内
- **文件**: `client/src/components/admintools/UsersOrdersTable.vue`
- **问题**: `UserOrderAside`（渲染为 div）放在 `<table>` 标签内部，违反 HTML 规范，浏览器可能自动修复导致不可预期的渲染
- **建议**: 将 `UserOrderAside` 移到 `<table>` 标签外部

### 6. UserOrderAside 发货操作未校验预计送达日期时间
- **文件**: `client/src/components/admintools/UserOrderAside.vue`
- **问题**: `sendOrder` 和 `changeExpectedDelivery` 未验证日期时间是否已填写，可能提交无效时间字符串
- **建议**: 增加日期时间非空校验

### 7. ProductView 商品加载失败无 UI 提示
- **文件**: `client/src/views/ProductView.vue`
- **问题**: 商品加载失败时 `product` 为 null，页面空白无任何提示
- **建议**: 增加"商品不存在或加载失败"的提示 UI

### 8. SignupView 注册成功后未自动跳转登录页
- **文件**: `client/src/views/SignupView.vue`
- **问题**: 注册成功后仅显示提示，用户需手动点击登录链接
- **建议**: 注册成功后自动跳转到登录页

---

## 中优先级（功能缺陷/逻辑问题）

### 9. AdminToolsView 的 isSuperAdmin 非响应式
- **文件**: `client/src/views/admin/AdminToolsView.vue`
- **问题**: `isSuperAdmin` 从 `sessionStorage` 读取一次后不更新，角色变更时 UI 不同步
- **建议**: 使用 `accountStore` 中的响应式数据替代 `sessionStorage` 直接读取

### 10. CommissionConfigView 的 isSuperAdmin 同样非响应式
- **文件**: `client/src/views/admin/CommissionConfigView.vue`
- **问题**: 虽使用 `computed`，但 `sessionStorage.getItem` 不是响应式数据源，computed 不会因 sessionStorage 变化而重新计算
- **建议**: 同上，使用 accountStore 的响应式数据

### 11. UserManagementView 直接调用底层请求函数
- **文件**: `client/src/views/admin/UserManagementView.vue`
- **问题**: 直接导入 `callGet/callPut/callPatch/callDelete` 而非通过 Store，破坏分层架构
- **建议**: 在 adminToolsStore 中封装用户管理 API，视图通过 Store 调用

### 12. CartItemList 缺少空购物车状态展示
- **文件**: `client/src/components/cart/CartItemList.vue`
- **问题**: 购物车为空且无错误时，三个条件分支均不满足，页面不渲染任何内容
- **建议**: 添加 `v-else` 分支显示"购物车为空"提示（或确认父组件 CartView 已处理此状态）

### 13. FavoritesView 收藏图片无 error 处理
- **文件**: `client/src/views/FavoritesView.vue`
- **问题**: `<img :src="fav.imageUrl">` 无 `@error` 处理和空值保护
- **建议**: 添加图片加载失败的 fallback 处理

### 14. PublishProductView 未登录跳转使用 push 而非 replace
- **文件**: `client/src/views/PublishProductView.vue`
- **问题**: 在 `<script setup>` 顶层调用 `router.push('/login')`，组件仍会短暂渲染后跳转，且 push 允许回退
- **建议**: 使用 `router.replace('/login')` 或导航守卫替代

### 15. AccountEditForm 删除操作成功判断逻辑不一致
- **文件**: `client/src/components/account/AccountEditForm.vue`
- **问题**: delete 操作使用 `if (response)` 判断成功，其他操作使用 `if (response !== undefined && response !== null)` 判断成功，两处标准不一致
- **建议**: 统一成功判断逻辑

### 16. ProductReviewView loadTabCounts 串行加载完整商品列表仅取长度
- **文件**: `client/src/views/admin/ProductReviewView.vue`
- **问题**: 4 个标签页各请求一次完整商品列表仅用于统计数量，性能浪费
- **建议**: 后端提供专门的计数接口，或前端缓存已加载的数据

### 17. ShopView watch 中分类回退逻辑未重置 selectedCategory
- **文件**: `client/src/views/ShopView.vue`
- **问题**: 从带分类的 URL 跳转到不带分类的 URL 时，`selectedCategory` 不会被重置为空
- **建议**: 在 category 参数为空时将 `selectedCategory` 重置为空字符串

---

## 低优先级（代码质量/风格问题）

### 18. SearchBar emit 事件名 onClose 不符合 Vue 惯例
- **文件**: `client/src/components/header/SearchBar.vue`
- **问题**: Vue 3 惯例是 emit 事件名不带 `on` 前缀，应为 `close`
- **建议**: 改为 `emit('close', true)`，父组件用 `@close` 监听

### 19. SearchBar handleFilterChange 中两个 if 应为 else if
- **文件**: `client/src/components/header/SearchBar.vue`
- **问题**: `targetFilter` 不可能同时等于两个值，使用 `else if` 语义更清晰
- **建议**: 改为 `else if`

### 20. ConfirmDialogue 使用 props 回调而非 emits
- **文件**: `client/src/components/ConfirmDialogue.vue`
- **问题**: `onConfirm` 和 `onCancel` 定义为 props，违反 Vue 组件通信规范
- **建议**: 改用 `defineEmits` 声明事件

### 21. ShoppingCartItem 角标 CSS 语义矛盾
- **文件**: `client/src/components/header/ShoppingCartItem.vue`
- **问题**: `p-3 w-3 h-3` 中 padding 使实际尺寸与 `w-3 h-3` 矛盾
- **建议**: 移除 `w-3 h-3` 或改用更合理的尺寸

### 22. SocialsIcons 图标颜色在深色背景上对比度不足
- **文件**: `client/src/components/footer/SocialsIcons.vue`
- **问题**: 使用 `text-gray-700`，在深色页脚背景 `bg-primary-800` 上可读性差
- **建议**: 改用 `text-primary-200` 或 `text-white`

### 23. FooterInfo 版权年份过时
- **文件**: `client/src/components/footer/FooterInfo.vue`
- **问题**: `© 2025`，当前已是 2026 年
- **建议**: 更新为当前年份或使用动态年份

### 24. HeroSection background-size: cover 对渐变背景无效
- **文件**: `client/src/components/HemoSection.vue`
- **问题**: `background-size: cover` 仅对图片背景有效，对 `linear-gradient` 无作用
- **建议**: 移除冗余的 `background-size: cover`

### 25. ProductPreview 在 xs 屏幕上完全不可见
- **文件**: `client/src/components/ProductPreview.vue`
- **问题**: `hidden sm:flex` 导致小屏不显示，但文件头注释未说明此限制
- **建议**: 在注释中说明此响应式行为限制

### 26. 多个组件 product.price.toFixed(2) 缺防御性处理
- **文件**: `ProductCard.vue`, `ProductDetailCard.vue`, `MyProductsList.vue`, `CartItemRow.vue`
- **问题**: 如果 `product.price` 为 `undefined`/`null`，将抛出运行时错误
- **建议**: 统一加可选链保护：`product.price?.toFixed(2) ?? '0.00'`

### 27. PlaceholderCards 的 placeholderCards 不响应 prop 变化
- **文件**: `client/src/components/products/PlaceholderCards.vue`
- **问题**: 在 `onMounted` 中通过循环生成，如果 `placeholderAmount` prop 变化，数量不会更新
- **建议**: 改用 `computed` 属性

### 28. 6个 admintools/account 组件文件头注释位置不规范
- **文件**: `AdminToolsItem.vue`, `AdminToolsPopup.vue`, `ProductForm.vue`, `UserOrderAside.vue`, `UsersOrdersTable.vue`, `AccountInfo.vue`, `AccountEditForm.vue`, `ShowOrders.vue`
- **问题**: 文件头注释位于 `<script setup>` 内部而非文件顶部，与 checkout 系列组件风格不一致
- **建议**: 统一移到文件顶部（`<template>` 之前）

### 29. UserOrderAside/UsersOrdersTable 时间解析缺少容错
- **文件**: `client/src/components/admintools/UserOrderAside.vue`, `client/src/components/admintools/UsersOrdersTable.vue`
- **问题**: `split('T')[1].slice(0, 5)` 假设时间字符串一定包含 'T'，格式异常时会崩溃
- **建议**: 增加格式校验和 try-catch

---

## 缺失的 API 端点实现

### 30. productStore 缺少以下端点
- `GET /api/products/categories` — 获取分类列表
- `GET /api/products/conditions` — 获取成色列表
- `GET /api/products/category/{category}` — 按分类获取商品

### 31. orderStore 缺少以下端点
- `GET /api/orders/delivery/methods` — 获取配送方式列表
- `GET /api/orders/payment/methods` — 获取支付方式列表

### 32. reviewStore 缺少以下端点
- `GET /api/reviews/user/{userId}` — 获取指定用户的评价列表
- `DELETE /api/reviews/{reviewId}` — 删除评价

### 33. adminToolsStore 缺少用户管理 API
- `GET /api/admin/users/all`
- `GET /api/admin/users/{userId}`
- `PUT /api/admin/users/{userId}/role`
- `PATCH /api/admin/users/{userId}/toggle-enabled`
- `DELETE /api/admin/users/{userId}`
- `PUT /api/admin/users/balance`

---

## 类型定义问题

### 34. ProductStatus 缺少 ENABLED 状态
- **文件**: `client/src/types/product.ts`
- **问题**: 管理员 API 有 `enable` 操作，但 `ProductStatus` 类型无 `'ENABLED'` 值
- **建议**: 确认后端启用商品后的状态值，如为 `ENABLED` 则补充到类型定义

### 35. OrderStatus 枚举可能不完整
- **文件**: `client/src/types/order.ts`
- **问题**: 仅包含 `PENDING`、`SHIPPED`、`DELIVERED`，可能缺少 `CANCELLED` 等状态
- **建议**: 与后端确认完整的状态枚举值

### 36. User 类型与 AccountDetails 字段不一致
- **文件**: `client/src/types/user.ts`, `client/src/stores/network/accountStore.ts`
- **问题**: `User` 包含 `avatarUrl`、`bio`、`isEnabled` 等字段，`AccountDetails` 包含 `email`、`username`、`balance` 等，两者有部分重叠但结构不同
- **建议**: 明确各自使用场景，避免混淆
