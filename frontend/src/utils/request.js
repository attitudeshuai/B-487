import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Token 存储 key
const TOKEN_KEY = 'auth_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_KEY = 'user'

/**
 * 获取 Token
 */
export const getToken = () => localStorage.getItem(TOKEN_KEY)

/**
 * 设置 Token
 */
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token)

/**
 * 移除 Token
 */
export const removeToken = () => localStorage.removeItem(TOKEN_KEY)

/**
 * 获取 Refresh Token
 */
export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY)

/**
 * 设置 Refresh Token
 */
export const setRefreshToken = (token) => localStorage.setItem(REFRESH_TOKEN_KEY, token)

/**
 * 移除 Refresh Token
 */
export const removeRefreshToken = () => localStorage.removeItem(REFRESH_TOKEN_KEY)

/**
 * 获取用户信息
 */
export const getUser = () => {
  const userStr = localStorage.getItem(USER_KEY)
  return userStr ? JSON.parse(userStr) : null
}

/**
 * 设置用户信息
 */
export const setUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user))

/**
 * 移除用户信息
 */
export const removeUser = () => localStorage.removeItem(USER_KEY)

/**
 * 清除所有认证信息
 */
export const clearAuth = () => {
  removeToken()
  removeRefreshToken()
  removeUser()
}

/**
 * 保存认证信息
 */
export const saveAuth = (authVO) => {
  if (authVO.token) {
    setToken(authVO.token)
  }
  if (authVO.refreshToken) {
    setRefreshToken(authVO.refreshToken)
  }
  if (authVO.user) {
    setUser(authVO.user)
  }
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 Token
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    console.log(`[请求] ${config.method?.toUpperCase()} ${config.url}`, config.data || '')
    return config
  },
  (error) => {
    console.error('[请求错误]', error)
    return Promise.reject(error)
  }
)

// 是否正在刷新 Token
let isRefreshing = false
// 等待刷新的请求队列
let refreshSubscribers = []

// 订阅 Token 刷新
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者
const onTokenRefreshed = (token) => {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    console.log(`[响应] ${response.config.url}`, res)
    
    // 判断业务状态码
    if (res.code === 200) {
      return res
    } else {
      // 业务错误
      const errorMessage = res.message || '请求失败'
      console.error('[业务错误]', errorMessage)
      return Promise.reject(new Error(errorMessage))
    }
  },
  async (error) => {
    console.error('[响应错误]', error)
    
    const originalRequest = error.config
    
    // 401 错误处理 - 尝试刷新 Token
    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = getRefreshToken()
      
      if (refreshToken && !originalRequest.url.includes('/user/refresh')) {
        if (!isRefreshing) {
          isRefreshing = true
          originalRequest._retry = true
          
          try {
            const res = await userApi.refreshToken(refreshToken)
            saveAuth(res.data)
            onTokenRefreshed(res.data.token)
            isRefreshing = false
            
            // 重试原始请求
            originalRequest.headers.Authorization = `Bearer ${res.data.token}`
            return request(originalRequest)
          } catch (refreshError) {
            isRefreshing = false
            refreshSubscribers = []
            clearAuth()
            window.location.href = '/login'
            return Promise.reject(refreshError)
          }
        } else {
          // 等待 Token 刷新完成
          return new Promise((resolve) => {
            subscribeTokenRefresh((token) => {
              originalRequest.headers.Authorization = `Bearer ${token}`
              resolve(request(originalRequest))
            })
          })
        }
      } else {
        // 无 Refresh Token 或刷新失败，跳转登录
        clearAuth()
        window.location.href = '/login'
      }
    }
    
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = data?.message || '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请稍后重试'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败，请检查网络'
    }
    
    return Promise.reject(new Error(message))
  }
)

// API 方法封装
export const userApi = {
  /**
   * 用户登录
   * @param {Object} data - { username, password }
   */
  login(data) {
    return request.post('/user/login', data)
  },
  
  /**
   * 用户注册
   * @param {Object} data - { username, password, nickname, email }
   */
  register(data) {
    return request.post('/user/register', data)
  },
  
  /**
   * 刷新 Token
   * @param {string} refreshToken - 刷新令牌
   */
  refreshToken(refreshToken) {
    return request.post('/user/refresh', { refreshToken })
  },
  
  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return request.get('/user/me')
  },
  
  /**
   * 获取用户信息
   * @param {number} id - 用户ID
   */
  getUserInfo(id) {
    return request.get(`/user/info/${id}`)
  },
  
  /**
   * 用户登出
   */
  logout() {
    return request.post('/user/logout')
  },
  
  /**
   * 发送密码重置验证码
   * @param {string} email - 邮箱
   */
  sendResetCode(email) {
    return request.post('/user/send-reset-code', { email })
  },
  
  /**
   * 验证密码重置验证码
   * @param {Object} data - { email, code }
   */
  verifyResetCode(data) {
    return request.post('/user/verify-reset-code', data)
  },
  
  /**
   * 重置密码
   * @param {Object} data - { email, code, newPassword }
   */
  resetPassword(data) {
    return request.post('/user/reset-password', data)
  },
  
  /**
   * 健康检查
   */
  health() {
    return request.get('/health')
  }
}

export default request
