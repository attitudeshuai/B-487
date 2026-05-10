import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import Home from '../views/Home.vue'
import { getToken, clearAuth } from '../utils/request'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { title: '忘记密码', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 登录系统` : '登录系统'
  
  // 获取 Token 判断登录状态
  const token = getToken()
  const isAuthenticated = !!token
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (!to.meta.requiresAuth && isAuthenticated && (to.name === 'Login' || to.name === 'Register' || to.name === 'ForgotPassword')) {
    // 已登录但访问登录/注册页，跳转到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
