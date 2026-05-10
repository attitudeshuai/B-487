<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <div class="logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h1 class="site-title">登录系统</h1>
        </div>
        <div class="header-right">
          <div class="user-info">
            <div class="user-avatar">
              {{ userInitial }}
            </div>
            <span class="user-name">{{ user?.nickname || user?.username }}</span>
          </div>
          <button class="btn btn-outline logout-btn" @click="handleLogout">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="welcome-section">
        <div class="welcome-card card">
          <div class="welcome-header">
            <div class="welcome-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <div>
              <h2 class="welcome-title">欢迎回来，{{ user?.nickname || user?.username }}！</h2>
              <p class="welcome-subtitle">您已成功登录系统</p>
            </div>
          </div>

          <div class="user-details">
            <h3 class="section-title">账户信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <div class="info-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <div class="info-content">
                  <span class="info-label">用户名</span>
                  <span class="info-value">{{ user?.username }}</span>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="4"/>
                    <path d="M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94"/>
                  </svg>
                </div>
                <div class="info-content">
                  <span class="info-label">邮箱</span>
                  <span class="info-value">{{ user?.email || '未设置' }}</span>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                </div>
                <div class="info-content">
                  <span class="info-label">昵称</span>
                  <span class="info-value">{{ user?.nickname || '未设置' }}</span>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                </div>
                <div class="info-content">
                  <span class="info-label">注册时间</span>
                  <span class="info-value">{{ formatDate(user?.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 功能卡片 -->
        <div class="feature-cards">
          <div class="feature-card card">
            <div class="feature-icon feature-icon-1">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
            <h3>安全登录</h3>
            <p>采用加密传输，保障账户安全</p>
          </div>

          <div class="feature-card card">
            <div class="feature-icon feature-icon-2">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
            </div>
            <h3>数据保护</h3>
            <p>严格遵守数据隐私保护规范</p>
          </div>

          <div class="feature-card card">
            <div class="feature-icon feature-icon-3">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
              </svg>
            </div>
            <h3>高效体验</h3>
            <p>快速响应，流畅的用户体验</p>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2024 登录系统. 基于 Spring Boot + Vue 3 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import message from '../utils/message'
import { getUser, clearAuth, userApi } from '../utils/request'

const router = useRouter()
const user = ref(null)

// 获取用户首字母
const userInitial = computed(() => {
  const name = user.value?.nickname || user.value?.username || ''
  return name.charAt(0).toUpperCase()
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return dateStr
  }
}

// 退出登录
const handleLogout = async () => {
  const confirmed = await message.confirm('确定要退出登录吗？', {
    title: '退出确认',
    confirmText: '确定退出',
    cancelText: '取消'
  })
  
  if (confirmed) {
    try {
      // 调用后端登出接口（可选，JWT无状态）
      await userApi.logout()
    } catch (e) {
      // 忽略错误，仍然清除本地认证信息
    }
    clearAuth()
    message.success('已成功退出登录')
    router.push('/login')
  }
}

// 页面加载时获取用户信息
onMounted(async () => {
  // 先从本地获取用户信息
  user.value = getUser()
  
  // 然后从后端验证并刷新用户信息
  try {
    const res = await userApi.getCurrentUser()
    user.value = res.data
  } catch (e) {
    // Token 无效，会被拦截器自动处理
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

/* 顶部导航 */
.header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-md);
  color: white;
}

.logo svg {
  width: 22px;
  height: 22px;
}

.site-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 50%;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
}

.logout-btn {
  padding: 8px 16px;
  font-size: 14px;
}

.logout-btn svg {
  width: 16px;
  height: 16px;
}

/* 主要内容 */
.main-content {
  flex: 1;
  padding: 40px 24px;
}

.welcome-section {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  padding: 32px;
  margin-bottom: 32px;
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
}

.welcome-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(72, 187, 120, 0.1);
  border-radius: var(--radius-lg);
  color: var(--success-color);
}

.welcome-icon svg {
  width: 28px;
  height: 28px;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--text-muted);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.info-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  color: var(--primary-color);
}

.info-icon svg {
  width: 20px;
  height: 20px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: var(--text-muted);
}

.info-value {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 功能卡片 */
.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.feature-card {
  padding: 28px;
  text-align: center;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
}

.feature-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
}

.feature-icon svg {
  width: 28px;
  height: 28px;
}

.feature-icon-1 {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: var(--primary-color);
}

.feature-icon-2 {
  background: linear-gradient(135deg, rgba(72, 187, 120, 0.1) 0%, rgba(56, 161, 105, 0.1) 100%);
  color: var(--success-color);
}

.feature-icon-3 {
  background: linear-gradient(135deg, rgba(237, 137, 54, 0.1) 0%, rgba(221, 107, 32, 0.1) 100%);
  color: var(--warning-color);
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.6;
}

/* 页脚 */
.footer {
  padding: 24px;
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-primary);
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    height: auto;
    flex-direction: column;
    padding: 16px 0;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .welcome-header {
    flex-direction: column;
    text-align: center;
  }

  .welcome-title {
    font-size: 20px;
  }

  .user-name {
    display: none;
  }
}
</style>
