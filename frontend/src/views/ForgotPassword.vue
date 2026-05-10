<template>
  <div class="auth-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <!-- 找回密码卡片 -->
    <div class="auth-card card">
      <!-- Logo 区域 -->
      <div class="auth-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
        </div>
        <h1 class="auth-title">找回密码</h1>
        <p class="auth-subtitle">{{ stepSubtitle }}</p>
      </div>

      <!-- 步骤指示器 -->
      <div class="steps-indicator">
        <div
          v-for="(step, index) in steps"
          :key="index"
          class="step-item"
          :class="{
            active: currentStep === index + 1,
            completed: currentStep > index + 1
          }"
        >
          <div class="step-circle">
            <svg v-if="currentStep > index + 1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <span class="step-label">{{ step }}</span>
        </div>
        <div class="step-line" :style="{ width: stepLineWidth + '%' }"></div>
      </div>

      <!-- 第一步：输入邮箱 -->
      <form v-if="currentStep === 1" @submit.prevent="handleStep1" class="auth-form">
        <div class="input-group">
          <label class="input-label">注册邮箱</label>
          <input
            type="email"
            v-model="form.email"
            class="input-field"
            :class="{ error: errors.email }"
            placeholder="请输入注册邮箱"
            autocomplete="email"
          />
          <p v-if="errors.email" class="input-error">{{ errors.email }}</p>
        </div>

        <div v-if="errorMessage" class="error-alert">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMessage }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>下一步</span>
        </button>
      </form>

      <!-- 第二步：输入验证码 -->
      <form v-if="currentStep === 2" @submit.prevent="handleStep2" class="auth-form">
        <div class="email-display">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
            <polyline points="22,6 12,13 2,6"></polyline>
          </svg>
          <span>{{ form.email }}</span>
          <button type="button" class="change-email" @click="goToStep(1)">修改</button>
        </div>

        <div class="input-group">
          <label class="input-label">验证码</label>
          <div class="code-input-wrapper">
            <input
              type="text"
              v-model="form.code"
              class="input-field code-input"
              :class="{ error: errors.code }"
              placeholder="请输入6位验证码"
              maxlength="6"
              autocomplete="one-time-code"
            />
            <button
              type="button"
              class="send-code-btn"
              :disabled="countdown > 0 || loading"
              @click="sendCode"
            >
              <span v-if="countdown > 0">{{ countdown }}s 后重发</span>
              <span v-else>发送验证码</span>
            </button>
          </div>
          <p v-if="errors.code" class="input-error">{{ errors.code }}</p>
          <p class="code-tip">验证码已发送到您的邮箱，10分钟内有效</p>
        </div>

        <div v-if="errorMessage" class="error-alert">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMessage }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>下一步</span>
        </button>
      </form>

      <!-- 第三步：设置新密码 -->
      <form v-if="currentStep === 3" @submit.prevent="handleStep3" class="auth-form">
        <div class="input-group">
          <label class="input-label">新密码</label>
          <div class="password-wrapper">
            <input
              :type="showNewPassword ? 'text' : 'password'"
              v-model="form.newPassword"
              class="input-field"
              :class="{ error: errors.newPassword }"
              placeholder="请输入新密码"
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
          <p v-if="errors.newPassword" class="input-error">{{ errors.newPassword }}</p>
        </div>

        <div class="input-group">
          <label class="input-label">确认新密码</label>
          <div class="password-wrapper">
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              v-model="form.confirmPassword"
              class="input-field"
              :class="{ error: errors.confirmPassword }"
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
          <p v-if="errors.confirmPassword" class="input-error">{{ errors.confirmPassword }}</p>
        </div>

        <div v-if="errorMessage" class="error-alert">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>{{ errorMessage }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>重置密码</span>
        </button>
      </form>

      <!-- 成功提示 -->
      <div v-if="currentStep === 4" class="success-container">
        <div class="success-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
        </div>
        <h2 class="success-title">密码重置成功</h2>
        <p class="success-text">您的密码已成功重置，现在可以使用新密码登录了</p>
        <button class="btn btn-primary btn-full" @click="goToLogin">返回登录</button>
      </div>

      <!-- 底部链接 -->
      <div class="auth-footer" v-if="currentStep < 4">
        <p>想起密码了？ <router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../utils/request'

const router = useRouter()

const steps = ['输入邮箱', '验证邮箱', '设置密码']
const currentStep = ref(1)
const loading = ref(false)
const errorMessage = ref('')
const countdown = ref(0)
const countdownTimer = ref(null)

const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const errors = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const stepSubtitle = computed(() => {
  switch (currentStep.value) {
    case 1:
      return '请输入您注册时使用的邮箱'
    case 2:
      return '请输入邮箱收到的验证码'
    case 3:
      return '请设置新的登录密码'
    default:
      return ''
  }
})

const stepLineWidth = computed(() => {
  if (currentStep.value === 1) return 0
  if (currentStep.value === 2) return 50
  if (currentStep.value >= 3) return 100
  return 0
})

const goToStep = (step) => {
  errorMessage.value = ''
  currentStep.value = step
}

const goToLogin = () => {
  router.push('/login')
}

const clearErrors = () => {
  errors.email = ''
  errors.code = ''
  errors.newPassword = ''
  errors.confirmPassword = ''
}

const validateEmail = () => {
  errors.email = ''
  if (!form.email.trim()) {
    errors.email = '请输入邮箱'
    return false
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    errors.email = '邮箱格式不正确'
    return false
  }
  return true
}

const validateCode = () => {
  errors.code = ''
  if (!form.code.trim()) {
    errors.code = '请输入验证码'
    return false
  }
  if (form.code.length !== 6) {
    errors.code = '验证码长度不正确'
    return false
  }
  return true
}

const validatePassword = () => {
  errors.newPassword = ''
  errors.confirmPassword = ''
  let isValid = true

  if (!form.newPassword) {
    errors.newPassword = '请输入新密码'
    isValid = false
  } else if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    errors.newPassword = '密码长度必须在6-20位之间'
    isValid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认新密码'
    isValid = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

const sendCode = async () => {
  if (countdown.value > 0 || loading.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    await userApi.sendResetCode({ email: form.email })
    startCountdown()
  } catch (error) {
    errorMessage.value = error.message || '发送失败，请重试'
  } finally {
    loading.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  countdownTimer.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer.value)
    }
  }, 1000)
}

const handleStep1 = async () => {
  clearErrors()
  errorMessage.value = ''

  if (!validateEmail()) return

  loading.value = true

  try {
    await userApi.sendResetCode({ email: form.email })
    startCountdown()
    currentStep.value = 2
  } catch (error) {
    errorMessage.value = error.message || '发送失败，请重试'
  } finally {
    loading.value = false
  }
}

const handleStep2 = async () => {
  clearErrors()
  errorMessage.value = ''

  if (!validateCode()) return

  loading.value = true

  try {
    await userApi.verifyCode({ email: form.email, code: form.code })
    currentStep.value = 3
  } catch (error) {
    errorMessage.value = error.message || '验证码校验失败，请重试'
  } finally {
    loading.value = false
  }
}

const handleStep3 = async () => {
  clearErrors()
  errorMessage.value = ''

  if (!validatePassword()) return

  loading.value = true

  try {
    await userApi.resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    })
    currentStep.value = 4
  } catch (error) {
    errorMessage.value = error.message || '重置失败，请重试'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
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

/* 卡片 */
.auth-card {
  width: 100%;
  max-width: 460px;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
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

/* 步骤指示器 */
.steps-indicator {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  position: relative;
}

.step-line {
  position: absolute;
  top: 18px;
  left: 0;
  height: 2px;
  background: var(--primary-color);
  transition: width 0.3s ease;
  z-index: 0;
  margin-left: calc(100% / 6);
  width: 0;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
  flex: 1;
}

.step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.step-circle svg {
  width: 18px;
  height: 18px;
}

.step-item.active .step-circle {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.step-item.completed .step-circle {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.step-label {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}

.step-item.active .step-label {
  color: var(--primary-color);
  font-weight: 600;
}

.step-item.completed .step-label {
  color: var(--primary-color);
}

/* 表单 */
.auth-form {
  margin-bottom: 24px;
}

.input-group {
  margin-bottom: 20px;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.input-field {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 15px;
  transition: all var(--transition-fast);
  outline: none;
}

.input-field:focus {
  border-color: var(--primary-color);
  background: var(--bg-primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-field.error {
  border-color: var(--error-color);
  background: rgba(245, 101, 101, 0.05);
}

.input-error {
  margin-top: 6px;
  font-size: 13px;
  color: var(--error-color);
}

/* 邮箱显示 */
.email-display {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
}

.email-display svg {
  width: 20px;
  height: 20px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.email-display span {
  flex: 1;
  color: var(--text-primary);
  font-size: 15px;
  word-break: break-all;
}

.change-email {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.change-email:hover {
  background: rgba(59, 130, 246, 0.1);
}

/* 验证码输入 */
.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  padding: 12px 20px;
  background: var(--bg-secondary);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-fast);
}

.send-code-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.send-code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.code-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

/* 密码输入框 */
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

/* 成功提示 */
.success-container {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(16, 185, 129, 0.1);
  border-radius: 50%;
  color: var(--success-color, #10b981);
  animation: scaleIn 0.5s ease;
}

.success-icon svg {
  width: 40px;
  height: 40px;
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.success-text {
  color: var(--text-muted);
  margin-bottom: 32px;
  line-height: 1.6;
}

/* 按钮 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: none;
  outline: none;
}

.btn-primary {
  background: var(--gradient-primary);
  color: white;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.4);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.5);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-full {
  width: 100%;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 底部 */
.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-footer a {
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 480px) {
  .auth-card {
    padding: 36px 24px;
  }

  .auth-title {
    font-size: 24px;
  }

  .step-label {
    font-size: 11px;
  }

  .code-input-wrapper {
    flex-direction: column;
  }

  .send-code-btn {
    width: 100%;
  }
}
</style>
