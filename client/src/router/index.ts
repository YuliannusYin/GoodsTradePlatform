/**
 * @file index.ts
 * @description Vue Router 路由配置文件，定义应用所有页面路由、嵌套路由及导航守卫
 * @input 无
 * @output Vue Router 实例
 */

import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import AccountView from '../views/AccountView.vue'
import ProductView from '@/views/ProductView.vue'
import EditAccountView from '../views/EditAccountView.vue'
import ShowAccountOrdersView from '@/views/ShowAccountOrdersView.vue'
import { useAccountStore } from '@/stores/network/accountStore'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'
import AdminToolsView from '@/views/admin/AdminToolsView.vue'
import HandleProductsView from '@/views/admin/HandleProductsView.vue'
import ProductForm from '@/components/admintools/ProductForm.vue'
import HandleOrdersView from '@/views/admin/HandleOrdersView.vue'
import CheckoutView from '@/views/CheckoutView.vue'
import UsersOrdersTable from '@/components/admintools/UsersOrdersTable.vue'
import PublishProductView from '@/views/PublishProductView.vue'
import FavoritesView from '@/views/FavoritesView.vue'
import UserManagementView from '@/views/admin/UserManagementView.vue'
import ProductReviewView from '@/views/admin/ProductReviewView.vue'
import MyProductsView from '@/views/MyProductsView.vue'

// 创建路由实例，使用 HTML5 History 模式
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // 首页路由
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      // 商城页面路由，使用懒加载优化性能
      path: '/shop',
      name: 'shop',
      component: () => import('../views/ShopView.vue'),
      // 将路由查询参数作为组件 props 传入
      props: (route) => ({
        query: route.query.query,
        filter: route.query.filter,
        category: route.query.category
      })
    },
    {
      // 商品详情页路由，通过动态参数 productId 标识具体商品
      path: '/product/:productId',
      name: 'productView',
      component: ProductView
    },
    {
      // 登录页路由
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      // 注册页路由
      path: '/signup',
      name: 'signup',
      component: SignupView
    },
    {
      // 账户中心路由，需要登录后才能访问
      path: '/account',
      name: 'account',
      component: AccountView,
      beforeEnter: (to, from, next) => {
        if (useAccountStore().isAuthenticated) {
          // 已登录，允许进入
          next()
        } else {
          // 未登录，重定向到登录页
          next('/login')
        }
      },
      children: [
        {
          // 编辑账户信息子路由
          path: 'edit',
          name: 'EditAccountView',
          component: EditAccountView
        },
        {
          // 查看我的订单子路由
          path: 'orders',
          name: 'ShowAccountOrdersView',
          component: ShowAccountOrdersView
        },
        {
          // 我的商品子路由
          path: 'my-products',
          name: 'MyProductsView',
          component: MyProductsView
        }
      ]
    },
    {
      // 购物车页面路由
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue')
    },
    {
      // 结算页面路由，购物车中需有商品才能访问
      path: '/checkout',
      name: 'checkout',
      component: CheckoutView,
      beforeEnter: (to, from, next) => {
        if (useShoppingCartStore().productAmount > 0) {
          // 购物车中有商品，允许进入结算
          next()
        } else {
          // 购物车为空，重定向到商城页
          next('/shop')
        }
      }
    },
    {
      // 发布商品页面路由，需要商户/管理员角色才能访问
      path: '/publish',
      name: 'publishProduct',
      component: PublishProductView,
      beforeEnter: (to, from, next) => {
        const accountStore = useAccountStore()
        if (accountStore.isAuthenticated && (accountStore.isMerchant() || accountStore.isAdmin())) {
          // 已登录且为商户或管理员，允许进入
          next()
        } else if (accountStore.isAuthenticated) {
          // 已登录但非商户/管理员，重定向到账户页
          next('/account')
        } else {
          // 未登录，重定向到登录页
          next('/login')
        }
      }
    },
    {
      // 收藏夹页面路由，需要登录后才能访问
      path: '/favorites',
      name: 'favorites',
      component: FavoritesView,
      beforeEnter: (to, from, next) => {
        if (useAccountStore().isAuthenticated) {
          // 已登录，允许进入
          next()
        } else {
          // 未登录，重定向到登录页
          next('/login')
        }
      }
    },
    {
      // 管理员工具页面路由，仅管理员可访问，默认重定向到商品管理
      path: '/admin_tools',
      name: 'AdminTools',
      redirect: '/admin_tools/products',
      component: AdminToolsView,
      beforeEnter: (to, from, next) => {
        const accountStore = useAccountStore()
        if (accountStore.isAuthenticated && accountStore.isAdmin()) {
          // 已登录且为管理员，允许进入
          next()
        } else {
          // 非管理员，重定向到登录页
          next('/login')
        }
      },
      children: [
        {
          // 商品管理子路由，默认重定向到添加商品
          path: 'products',
          name: 'HandleProductsView',
          component: HandleProductsView,
          redirect: '/admin_tools/products/add',
          children: [
            {
              // 添加商品
              path: 'add',
              name: 'AddProduct',
              component: ProductForm,
              props: { formMode: 'add' }
            },
            {
              // 编辑商品
              path: 'edit',
              name: 'EditProduct',
              component: ProductForm,
              props: { formMode: 'edit' }
            },
            {
              // 删除商品
              path: 'delete',
              name: 'DeleteProduct',
              component: ProductForm,
              props: { formMode: 'delete' }
            }
          ]
        },
        {
          // 订单管理子路由，默认重定向到待处理订单
          path: 'orders',
          name: 'HandleOrdersView',
          component: HandleOrdersView,
          redirect: '/admin_tools/orders/pending',
          children: [
            {
              // 待处理订单
              path: 'pending',
              name: 'PendingOrders',
              component: UsersOrdersTable
            },
            {
              // 已发货订单
              path: 'sent',
              name: 'SentOrders',
              component: UsersOrdersTable
            },
            {
              // 全部订单
              path: 'all',
              name: 'AllOrders',
              component: UsersOrdersTable
            }
          ]
        },
        {
          // 用户管理子路由，仅管理员可访问
          path: 'users',
          name: 'UserManagementView',
          component: UserManagementView,
          beforeEnter: (to, from, next) => {
            if (useAccountStore().isAdmin()) {
              // 是管理员，允许进入
              next()
            } else {
              // 非管理员，重定向到管理工具首页
              next('/admin_tools')
            }
          }
        },
        {
          // 商品审核子路由，仅管理员可访问
          path: 'reviews',
          name: 'ProductReviewView',
          component: ProductReviewView,
          beforeEnter: (to, from, next) => {
            if (useAccountStore().isAdmin()) {
              // 是管理员，允许进入
              next()
            } else {
              // 非管理员，重定向到管理工具首页
              next('/admin_tools')
            }
          }
        }
      ]
    },
    {
      // 捕获所有未匹配路由，重定向到首页
      path: '/:catchAll(.*)',
      redirect: '/'
    }
  ]
})

export default router
