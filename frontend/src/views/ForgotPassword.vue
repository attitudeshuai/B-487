<template>
  <div class="auth-page">
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="auth-card card">
      <div class="auth-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
        </div>
        <h1 class="auth-title">找回密码</h1>
        <p class="auth-subtitle">通过邮箱验证重置您的密码</p>
      </div>

      <div class="steps-indicator">
        <div class="step" :class="{ active: step >= 1, done: step > 1 }">
          <div class="step-circle">{{ step > 1 ? '✓' : '1' }}</div>
          <span class="step-label">填写邮箱</span>
        </div>
        <div class="step-line" :class="{ active: step > 1 }"></div>
        <div class="step" :class="{ active: step >= 2, done: step > 2 }">
          <div class="step-circle">{{ step > 2 ? '✓' : '2' }}</div>
          <span class="step-label">输入验证码</span>
        </div>
        <div class="step-line" :class="{ active: step > 2 }"></div>
        <div class="step" :class="{ active: step >= 3 }">
          <div class="step-circle">3</div>
          <span class="step-label">设置新密码</span>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="auth-form">

        <transition name="fade" mode="out-in">
          <div v-if="step === 1" key="step1" class="step-content">
            <div class="input-group">
              <label class="input-label">注册邮箱</label>
              <input
                type="email"
                v-model="form.email"
                class="input-field"
                :class="{ error: errors.email }"
                placeholder="请输入注册时使用的邮箱"
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
              <span v-else>发送验证码</span>
            </button>
          </div>

          <div v-else-if="step === 2" key="step2" class="step-content">
            <div class="email-hint">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;flex-shrink:0">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              <span>验证码已发送至 <strong>{{ maskedEmail }}</strong></span>
            </div>

            <div class="input-group">
              <label class="input-label">6位验证码</label>
              <div class="code-inputs">
                <input
                  v-for="i in 6"
                  :key="i"
                  type="text"
                  maxlength="1"
                  class="code-input"
                  :ref="el => codeRefs[i - 1] = el"
                  v-model="codeDigits[i - 1]"
                  @input="onCodeInput(i - 1)"
                  @keydown.backspace="onCodeBackspace(i - 1, $event)"
                  @paste="onCodePaste"
                  inputmode="numeric"
                  pattern="[0-9]"
                />
              </div>
              <p v-if="errors.code" class="input-error">{{ errors.code }}</p>
            </div>

            <div class="resend-row">
              <button
                type="button"
                class="btn-link"
                :disabled="cooldown > 0"
                @click="resendCode"
              >
                {{ cooldown > 0 ? `${cooldown}秒后可重新发送` : '重新发送验证码' }}
              </button>
            </div>

            <div v-if="errorMessage" class="error-alert">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <span>{{ errorMessage }}</span>
            </div>

            <div class="btn-group">
              <button type="button" class="btn btn-secondary" @click="goBack(1)">返回</button>
              <button type="submit" class="btn btn-primary" :disabled="loading || !isCodeComplete">
                <span v-if="loading" class="loading-spinner"></span>
                <span v-else>验证</span>
              </button>
            </div>
          </div>

          <div v-else-if="step === 3" key="step3" class="step-content">
            <div class="input-group">
              <label class="input-label">新密码</label>
              <div class="password-wrapper">
                <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="form.newPassword"
                  class="input-field"
                  :class="{ error: errors.newPassword }"
                  placeholder="请输入新密码（6-20位）"
                  autocomplete="new-password"
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
              <p v-if="errors.newPassword" class="input-error">{{ errors.newPassword }}</p>
            </div>

            <div class="input-group">
              <label class="input-label">确认新密码</label>
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="form.confirmPassword"
                class="input-field"
                :class="{ error: errors.confirmPassword }"
                placeholder="请再次输入新密码"
                autocomplete="new-password"
              />
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

            <div v-if="successMessage" class="success-alert">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <span>{{ successMessage }}</span>
            </div>

            <div class="btn-group">
              <button type="button" class="btn btn-secondary" @click="goBack(2)">返回</button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <span v-if="loading" class="loading-spinner"></span>
                <span v-else>重置密码</span>
              </button>
            </div>
          </div>
        </transition>
      </form>

      <div class="auth-footer">
        <p><router-link to="/login">返回登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { passwordResetApi } from '../utils/request'

const router = useRouter()

const step = ref(1)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showPassword = ref(false)
const cooldown = ref(0)
const codeRefs = ref([])

const form = reactive({
  email: '',
  newPassword: '',
  confirmPassword: ''
})

const resetToken = ref('')

const codeDigits = reactive(['', '', '', '', '', ''])

const errors = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const maskedEmail = computed(() => {
  if (!form.email) return ''
  const [local, domain] = form.email.split('@')
  if (local.length <= 2) return form.email
  return local[0] + '***' + local[local.length - 1] + '@' + domain
})

const isCodeComplete = computed(() => codeDigits.every(d => d.length === 1))

let cooldownTimer = null

const startCooldown = () => {
  cooldown.value = 60
  cooldownTimer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

onUnmounted(() => {
  if (cooldownTimer) clearInterval(cooldownTimer)
})

const onCodeInput = (index) => {
  const val = codeDigits[index]
  codeDigits[index] = val.replace(/\D/g, '').slice(0, 1)
  if (codeDigits[index] && index < 5) {
    codeRefs.value[index + 1]?.focus()
  }
}

const onCodeBackspace = (index, event) => {
  if (!codeDigits[index] && index > 0) {
    codeRefs.value[index - 1]?.focus()
  }
}

const onCodePaste = (event) => {
  event.preventDefault()
  const paste = (event.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '').slice(0, 6)
  for (let i = 0; i < 6; i++) {
    codeDigits[i] = paste[i] || ''
  }
  const nextEmpty = codeDigits.findIndex(d => !d)
  if (nextEmpty >= 0) {
    codeRefs.value[nextEmpty]?.focus()
  } else {
    codeRefs.value[5]?.focus()
  }
}

const goBack = (targetStep) => {
  errorMessage.value = ''
  successMessage.value = ''
  step.value = targetStep
}

const validateStep1 = () => {
  errors.email = ''
  let valid = true
  if (!form.email.trim()) {
    errors.email = '请输入邮箱'
    valid = false
  } else if (!emailRegex.test(form.email)) {
    errors.email = '邮箱格式不正确'
    valid = false
  }
  return valid
}

const validateStep2 = () => {
  errors.code = ''
  let valid = true
  const code = codeDigits.join('')
  if (code.length !== 6) {
    errors.code = '请输入6位验证码'
    valid = false
  }
  return valid
}

const validateStep3 = () => {
  errors.newPassword = ''
  errors.confirmPassword = ''
  let valid = true
  if (!form.newPassword) {
    errors.newPassword = '请输入新密码'
    valid = false
  } else if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    errors.newPassword = '密码长度必须在6-20位之间'
    valid = false
  }
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认新密码'
    valid = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    valid = false
  }
  return valid
}

const resendCode = async () => {
  if (cooldown.value > 0) return
  errorMessage.value = ''
  try {
    await passwordResetApi.sendCode({ email: form.email })
    startCooldown()
  } catch (error) {
    errorMessage.value = error.message || '发送验证码失败'
  }
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (step.value === 1) {
    if (!validateStep1()) return
    loading.value = true
    try {
      await passwordResetApi.sendCode({ email: form.email })
      step.value = 2
      startCooldown()
      setTimeout(() => codeRefs.value[0]?.focus(), 300)
    } catch (error) {
      errorMessage.value = error.message || '发送验证码失败'
    } finally {
      loading.value = false
    }
  } else if (step.value === 2) {
    if (!validateStep2()) return
    loading.value = true
    try {
      const code = codeDigits.join('')
      const res = await passwordResetApi.verifyCode({ email: form.email, code })
      resetToken.value = res.data.resetToken
      step.value = 3
    } catch (error) {
      errorMessage.value = error.message || '验证码校验失败'
    } finally {
      loading.value = false
    }
  } else if (step.value === 3) {
    if (!validateStep3()) return
    loading.value = true
    try {
      await passwordResetApi.resetPassword({
        resetToken: resetToken.value,
        newPassword: form.newPassword
      })
      successMessage.value = '密码重置成功！正在跳转登录页...'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } catch (error) {
      errorMessage.value = error.message || '密码重置失败'
    } finally {
      loading.value = false
    }
  }
}
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
  width: 350px;
  height: 350px;
  top: -80px;
  left: -80px;
  animation-delay: 0s;
}

.bg-circle-2 {
  width: 250px;
  height: 250px;
  bottom: -50px;
  right: -50px;
  animation-delay: -7s;
}

.bg-circle-3 {
  width: 180px;
  height: 180px;
  top: 40%;
  right: 15%;
  animation-delay: -12s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(20px, -20px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(-20px, -20px) scale(1.02); }
}

.auth-card {
  width: 100%;
  max-width: 460px;
  padding: 44px 40px;
  position: relative;
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-secondary);
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

.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  gap: 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  background: var(--border-color);
  color: var(--text-muted);
  transition: all var(--transition-normal);
}

.step.active .step-circle {
  background: var(--gradient-primary);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.step.done .step-circle {
  background: var(--success-color);
  color: white;
}

.step-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  white-space: nowrap;
}

.step.active .step-label {
  color: var(--primary-color);
}

.step-line {
  width: 50px;
  height: 2px;
  background: var(--border-color);
  margin: 0 8px;
  margin-bottom: 22px;
  transition: background var(--transition-normal);
}

.step-line.active {
  background: var(--gradient-primary);
}

.auth-form {
  margin-bottom: 24px;
}

.step-content {
  min-height: 200px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.email-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
  background: rgba(66, 153, 225, 0.1);
  border: 1px solid rgba(66, 153, 225, 0.3);
  border-radius: var(--radius-md);
  color: var(--info-color);
  font-size: 14px;
}

.code-inputs {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.code-input {
  width: 48px;
  height: 56px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  font-family: inherit;
  color: var(--text-primary);
  background: var(--bg-primary);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  outline: none;
}

.code-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px var(--primary-light);
}

.resend-row {
  text-align: center;
  margin: 16px 0 20px;
}

.btn-link {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  transition: color var(--transition-fast);
}

.btn-link:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
}

.btn-link:hover:not(:disabled) {
  color: var(--primary-hover);
}

.btn-group {
  display: flex;
  gap: 12px;
}

.btn-group .btn-secondary {
  flex: 0 0 auto;
  min-width: 90px;
}

.btn-group .btn-primary {
  flex: 1;
}

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

.success-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 20px;
  background: rgba(72, 187, 120, 0.1);
  border: 1px solid rgba(72, 187, 120, 0.3);
  border-radius: var(--radius-md);
  color: var(--success-color);
  font-size: 14px;
}

.success-alert svg {
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

@media (max-width: 480px) {
  .auth-card {
    padding: 32px 20px;
  }

  .auth-title {
    font-size: 24px;
  }

  .code-inputs {
    gap: 6px;
  }

  .code-input {
    width: 42px;
    height: 48px;
    font-size: 18px;
  }

  .step-line {
    width: 30px;
  }
}
</style>
