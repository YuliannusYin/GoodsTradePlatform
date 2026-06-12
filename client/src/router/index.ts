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

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/shop',
      name: 'shop',
      component: () => import('../views/ShopView.vue'),
      props: (route) => ({
        query: route.query.query,
        filter: route.query.filter,
        category: route.query.category
      })
    },
    {
      path: '/product/:productId',
      name: 'productView',
      component: ProductView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView
    },
    {
      path: '/account',
      name: 'account',
      component: AccountView,
      beforeEnter: (to, from, next) => {
        if (useAccountStore().isAuthenticated) {
          next()
        } else {
          next('/login')
        }
      },
      children: [
        {
          path: 'edit',
          name: 'EditAccountView',
          component: EditAccountView
        },
        {
          path: 'orders',
          name: 'ShowAccountOrdersView',
          component: ShowAccountOrdersView
        },
        {
          path: 'my-products',
          name: 'MyProductsView',
          component: MyProductsView
        }
      ]
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: CheckoutView,
      beforeEnter: (to, from, next) => {
        if (useShoppingCartStore().productAmount > 0) {
          next()
        } else {
          next('/shop')
        }
      }
    },
    {
      path: '/publish',
      name: 'publishProduct',
      component: PublishProductView,
      beforeEnter: (to, from, next) => {
        if (useAccountStore().isAuthenticated) {
          next()
        } else {
          next('/login')
        }
      }
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: FavoritesView,
      beforeEnter: (to, from, next) => {
        if (useAccountStore().isAuthenticated) {
          next()
        } else {
          next('/login')
        }
      }
    },
    {
      path: '/admin_tools',
      name: 'AdminTools',
      redirect: '/admin_tools/products',
      component: AdminToolsView,
      beforeEnter: (to, from, next) => {
        const accountStore = useAccountStore()
        if (accountStore.isAuthenticated && accountStore.isAdmin()) {
          next()
        } else {
          next('/login')
        }
      },
      children: [
        {
          path: 'products',
          name: 'HandleProductsView',
          component: HandleProductsView,
          redirect: '/admin_tools/products/add',
          children: [
            {
              path: 'add',
              name: 'AddProduct',
              component: ProductForm,
              props: { formMode: 'add' }
            },
            {
              path: 'edit',
              name: 'EditProduct',
              component: ProductForm,
              props: { formMode: 'edit' }
            },
            {
              path: 'delete',
              name: 'DeleteProduct',
              component: ProductForm,
              props: { formMode: 'delete' }
            }
          ]
        },
        {
          path: 'orders',
          name: 'HandleOrdersView',
          component: HandleOrdersView,
          redirect: '/admin_tools/orders/pending',
          children: [
            {
              path: 'pending',
              name: 'PendingOrders',
              component: UsersOrdersTable
            },
            {
              path: 'sent',
              name: 'SentOrders',
              component: UsersOrdersTable
            },
            {
              path: 'all',
              name: 'AllOrders',
              component: UsersOrdersTable
            }
          ]
        },
        {
          path: 'users',
          name: 'UserManagementView',
          component: UserManagementView,
          beforeEnter: (to, from, next) => {
            if (useAccountStore().isAdmin()) {
              next()
            } else {
              next('/admin_tools')
            }
          }
        },
        {
          path: 'reviews',
          name: 'ProductReviewView',
          component: ProductReviewView,
          beforeEnter: (to, from, next) => {
            if (useAccountStore().isAdmin()) {
              next()
            } else {
              next('/admin_tools')
            }
          }
        }
      ]
    },
    { path: '/:catchAll(.*)', redirect: '/' }
  ]
})

export default router
