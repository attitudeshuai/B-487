<template>
  <div class="auth-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="auth-card card">
      <!-- Logo 区域 -->
      <div class="auth-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1 class="auth-title">{{ showForgotPassword ? '找回密码' : '欢迎回来' }}</h1>
        <p class="auth-subtitle">
          {{ showForgotPassword ? (forgotStep === 3 ? '设置新密码' : (forgotStep === 2 ? '输入验证码' : '输入注册邮箱')) : '登录您的账户以继续' }}
        </p>
      </div>

      <!-- 登录表单 -->
      <form v-if="!showForgotPassword" @submit.prevent="handleLogin" class="auth-form">
        <!-- 用户名 -->
        <div class="input-group">
          <label class="input-label">用户名</label>
          <input
            type="text"
            v-model="form.username"
            class="input-field"
            :class="{ error: errors.username }"
            placeholder="请输入用户名"
            autocomplete="username"
          />
          <p v-if="errors.username" class="input-error">{{ errors.username }}</p>
        </div>

        <!-- 密码 -->
        <div class="input-group">
          <label class="input-label">密码</label>
          <div class="password-wrapper">
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              class="input-field"
              :class="{ error: errors.password }"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
            <button type="button" class="password-toggle" @click="showPassword = !showPassword">
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <p v-if="errors.password" class="input-error">{{ errors.password }}</p>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="error-alert">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMessage }}</span>
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>登录</span>
        </button>
      </form>

      <!-- 找回密码表单 -->
      <div v-else class="auth-form">
        <!-- 步骤指示器 -->
        <div class="step-indicator">
          <div class="step" :class="{ active: forgotStep >= 1, done: forgotStep > 1 }">
            <span class="step-number">1</span>
            <span class="step-label">输入邮箱</span>
          </div>
          <div class="step-line" :class="{ active: forgotStep >= 2 }"></div>
          <div class="step" :class="{ active: forgotStep >= 2, done: forgotStep > 2 }">
            <span class="step-number">2</span>
            <span class="step-label">验证邮箱</span>
          </div>
          <div class="step-line" :class="{ active: forgotStep >= 3 }"></div>
          <div class="step" :class="{ active: forgotStep >= 3 }">
            <span class="step-number">3</span>
            <span class="step-label">设置密码</span>
          </div>
        </div>

        <!-- 第一步：输入邮箱 -->
        <div v-if="forgotStep === 1">
          <div class="input-group">
            <label class="input-label">注册邮箱</label>
            <input
              type="email"
              v-model="forgotForm.email"
              class="input-field"
              :class="{ error: forgotErrors.email }"
              placeholder="请输入注册邮箱"
              autocomplete="email"
            />
            <p v-if="forgotErrors.email" class="input-error">{{ forgotErrors.email }}</p>
          </div>

          <div v-if="forgotErrorMessage" class="error-alert">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{{ forgotErrorMessage }}</span>
          </div>

          <button type="button" class="btn btn-primary btn-full" :disabled="forgotLoading" @click="handleSendCode">
            <span v-if="forgotLoading" class="loading-spinner"></span>
            <span v-else>发送验证码</span>
          </button>
        </div>

        <!-- 第二步：输入验证码 -->
        <div v-else-if="forgotStep === 2">
          <div class="input-group">
            <label class="input-label">邮箱地址</label>
            <div class="readonly-email">{{ forgotForm.email }}</div>
          </div>
          
          <div class="input-group">
            <label class="input-label">验证码</label>
            <div class="code-input-wrapper">
              <input
                type="text"
                v-model="forgotForm.code"
                class="input-field code-input"
                :class="{ error: forgotErrors.code }"
                placeholder="请输入6位验证码"
                maxlength="6"
              />
              <button 
                type="button" 
                class="code-send-btn" 
                :disabled="countdown > 0 || forgotLoading"
                @click="handleResendCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '重新发送' }}
              </button>
            </div>
            <p v-if="forgotErrors.code" class="input-error">{{ forgotErrors.code }}</p>
          </div>

          <div v-if="forgotErrorMessage" class="error-alert">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{{ forgotErrorMessage }}</span>
          </div>

          <button type="button" class="btn btn-primary btn-full" :disabled="forgotLoading" @click="handleVerifyCode">
            <span v-if="forgotLoading" class="loading-spinner"></span>
            <span v-else>下一步</span>
          </button>
        </div>

        <!-- 第三步：设置新密码 -->
        <div v-else-if="forgotStep === 3">
          <div class="input-group">
            <label class="input-label">新密码</label>
            <div class="password-wrapper">
              <input
                :type="showNewPassword ? 'text' : 'password'"
                v-model="forgotForm.newPassword"
                class="input-field"
                :class="{ error: forgotErrors.newPassword }"
                placeholder="请输入新密码（6-20位）"
                autocomplete="new-password"
              />
              <button type="button" class="password-toggle" @click="showNewPassword = !showNewPassword">
                <svg v-if="!showNewPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
            <p v-if="forgotErrors.newPassword" class="input-error">{{ forgotErrors.newPassword }}</p>
          </div>

          <div class="input-group">
            <label class="input-label">确认新密码</label>
            <div class="password-wrapper">
              <input
                :type="showConfirmPassword ? 'text' : 'password'"
                v-model="forgotForm.confirmPassword"
                class="input-field"
                :class="{ error: forgotErrors.confirmPassword }"
                placeholder="请再次输入新密码"
                autocomplete="new-password"
              />
              <button type="button" class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                <svg v-if="!showConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
            <p v-if="forgotErrors.confirmPassword" class="input-error">{{ forgotErrors.confirmPassword }}</p>
          </div>

          <div v-if="forgotErrorMessage" class="error-alert">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{{ forgotErrorMessage }}</span>
          </div>

          <button type="button" class="btn btn-primary btn-full" :disabled="forgotLoading" @click="handleResetPassword">
            <span v-if="forgotLoading" class="loading-spinner"></span>
            <span v-else>重置密码</span>
          </button>
        </div>

        <!-- 成功提示 -->
        <div v-if="resetSuccess" class="success-message">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <p>密码重置成功！</p>
          <button type="button" class="btn btn-primary btn-full mt-20" @click="backToLogin">
            返回登录
          </button>
        </div>
      </div>

      <!-- 底部链接 -->
      <div class="auth-footer" v-if="!showForgotPassword">
        <p><a href="javascript:void(0)" @click="showForgotPassword = true">忘记密码？</a></p>
        <p>还没有账户？ <router-link to="/register">立即注册</router-link></p>
      </div>
      <div class="auth-footer" v-else-if="!resetSuccess">
        <p><a href="javascript:void(0)" @click="backToLogin">返回登录</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userApi, saveAuth } from '../utils/request'

const router = useRouter()
const route = useRoute()

// 登录表单数据
const form = reactive({
  username: '',
  password: ''
})

// 登录状态
const loading = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')
const errors = reactive({
  username: '',
  password: ''
})

// 找回密码状态
const showForgotPassword = ref(false)
const forgotStep = ref(1)
const forgotLoading = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const forgotErrorMessage = ref('')
const resetSuccess = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 找回密码表单数据
const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const forgotErrors = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 登录表单验证
const validateForm = () => {
  let isValid = true
  errors.username = ''
  errors.password = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (form.username.length < 3 || form.username.length > 20) {
    errors.username = '用户名长度必须在3-20位之间'
    isValid = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6 || form.password.length > 20) {
    errors.password = '密码长度必须在6-20位之间'
    isValid = false
  }

  return isValid
}

// 登录处理
const handleLogin = async () => {
  errorMessage.value = ''
  
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    const res = await userApi.login({
      username: form.username,
      password: form.password
    })

    // 保存认证信息（Token + 用户信息）
    saveAuth(res.data)

    // 跳转到原页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

// 验证邮箱格式
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 发送验证码
const handleSendCode = async () => {
  forgotErrorMessage.value = ''
  forgotErrors.email = ''

  if (!forgotForm.email.trim()) {
    forgotErrors.email = '请输入邮箱'
    return
  }
  if (!validateEmail(forgotForm.email)) {
    forgotErrors.email = '邮箱格式不正确'
    return
  }

  forgotLoading.value = true

  try {
    await userApi.sendResetCode(forgotForm.email)
    forgotStep.value = 2
    startCountdown()
  } catch (error) {
    forgotErrorMessage.value = error.message || '发送失败，请重试'
  } finally {
    forgotLoading.value = false
  }
}

// 重新发送验证码
const handleResendCode = async () => {
  if (countdown.value > 0) return
  
  forgotErrorMessage.value = ''
  forgotLoading.value = true

  try {
    await userApi.sendResetCode(forgotForm.email)
    startCountdown()
  } catch (error) {
    forgotErrorMessage.value = error.message || '发送失败，请重试'
  } finally {
    forgotLoading.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 验证验证码
const handleVerifyCode = async () => {
  forgotErrorMessage.value = ''
  forgotErrors.code = ''

  if (!forgotForm.code.trim()) {
    forgotErrors.code = '请输入验证码'
    return
  }
  if (forgotForm.code.length !== 6) {
    forgotErrors.code = '验证码长度为6位'
    return
  }

  forgotLoading.value = true

  try {
    await userApi.verifyResetCode({
      email: forgotForm.email,
      code: forgotForm.code
    })
    forgotStep.value = 3
  } catch (error) {
    forgotErrorMessage.value = error.message || '验证失败，请重试'
  } finally {
    forgotLoading.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  forgotErrorMessage.value = ''
  forgotErrors.newPassword = ''
  forgotErrors.confirmPassword = ''

  if (!forgotForm.newPassword) {
    forgotErrors.newPassword = '请输入新密码'
    return
  }
  if (forgotForm.newPassword.length < 6 || forgotForm.newPassword.length > 20) {
    forgotErrors.newPassword = '密码长度必须在6-20位之间'
    return
  }
  if (!forgotForm.confirmPassword) {
    forgotErrors.confirmPassword = '请确认新密码'
    return
  }
  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    forgotErrors.confirmPassword = '两次输入的密码不一致'
    return
  }

  forgotLoading.value = true

  try {
    await userApi.resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword
    })
    resetSuccess.value = true
  } catch (error) {
    forgotErrorMessage.value = error.message || '重置失败，请重试'
  } finally {
    forgotLoading.value = false
  }
}

// 返回登录
const backToLogin = () => {
  showForgotPassword.value = false
  forgotStep.value = 1
  resetSuccess.value = false
  forgotForm.email = ''
  forgotForm.code = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  forgotErrorMessage.value = ''
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdown.value = 0
}

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: var(--gradient-background);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s ease-in-out infinite;
}

.bg-circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation-delay: -5s;
}

.bg-circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(20px, -20px) scale(1.05);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(-20px, -20px) scale(1.02);
  }
}

/* 登录卡片 */
.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  color: white;
}

.logo svg {
  width: 32px;
  height: 32px;
}

.auth-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.auth-subtitle {
  font-size: 15px;
  color: var(--text-muted);
}

.auth-form {
  margin-bottom: 24px;
}

/* 密码输入框包装 */
.password-wrapper {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.password-toggle:hover {
  color: var(--text-secondary);
}

.password-toggle svg {
  width: 20px;
  height: 20px;
}

/* 错误提示 */
.error-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 20px;
  background: rgba(245, 101, 101, 0.1);
  border: 1px solid rgba(245, 101, 101, 0.3);
  border-radius: var(--radius-md);
  color: var(--error-color);
  font-size: 14px;
}

.error-alert svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-footer a {
  font-weight: 600;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  background: var(--bg-secondary);
  color: var(--text-muted);
  border: 2px solid var(--border-color);
  transition: all var(--transition-fast);
}

.step.active .step-number {
  background: var(--gradient-primary);
  color: white;
  border-color: transparent;
}

.step.done .step-number {
  background: var(--success-color);
  color: white;
  border-color: transparent;
}

.step-label {
  font-size: 12px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.step.active .step-label {
  color: var(--text-primary);
  font-weight: 500;
}

.step-line {
  width: 40px;
  height: 2px;
  background: var(--border-color);
  margin: 0 8px;
  margin-bottom: 24px;
  transition: background var(--transition-fast);
}

.step-line.active {
  background: var(--primary-color);
}

/* 只读邮箱显示 */
.readonly-email {
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 14px;
}

/* 验证码输入框 */
.code-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.code-input {
  flex: 1;
}

.code-send-btn {
  padding: 0 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--primary-color);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  min-width: 120px;
}

.code-send-btn:hover:not(:disabled) {
  background: var(--primary-color);
  color: white;
}

.code-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 成功消息 */
.success-message {
  text-align: center;
  padding: 24px 0;
}

.success-message svg {
  width: 64px;
  height: 64px;
  color: var(--success-color);
  margin-bottom: 16px;
}

.success-message p {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.mt-20 {
  margin-top: 20px;
}

/* 底部链接调整 */
.auth-footer p {
  margin: 8px 0;
}

/* 响应式 */
@media (max-width: 480px) {
  .auth-card {
    padding: 36px 24px;
  }

  .auth-title {
    font-size: 24px;
  }
  
  .step-line {
    width: 24px;
  }
  
  .step-label {
    font-size: 11px;
  }
}
</style>
