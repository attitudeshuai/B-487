<template>
  <!-- Toast Container -->
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="toast"
          :class="[`toast-${toast.type}`]"
        >
          <div class="toast-icon">
            <svg v-if="toast.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else-if="toast.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            <svg v-else-if="toast.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </div>
          <span class="toast-message">{{ toast.message }}</span>
          <button class="toast-close" @click="removeToast(toast.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>

  <!-- Confirm Modal -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="confirmModal.visible" class="modal-overlay" @click.self="handleConfirmCancel">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-icon modal-icon-warning">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <h3 class="modal-title">{{ confirmModal.title }}</h3>
          </div>
          <p class="modal-message">{{ confirmModal.message }}</p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="handleConfirmCancel">
              {{ confirmModal.cancelText }}
            </button>
            <button class="btn btn-primary" @click="handleConfirmOk">
              {{ confirmModal.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive } from 'vue'

// Toast 相关
const toasts = ref([])
let toastId = 0

const showToast = (message, type = 'info', duration = 3000) => {
  const id = ++toastId
  toasts.value.push({ id, message, type })
  
  if (duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }
  
  return id
}

const removeToast = (id) => {
  const index = toasts.value.findIndex(t => t.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// Confirm Modal 相关
const confirmModal = reactive({
  visible: false,
  title: '确认',
  message: '',
  confirmText: '确定',
  cancelText: '取消',
  resolve: null
})

const showConfirm = (message, options = {}) => {
  return new Promise((resolve) => {
    confirmModal.visible = true
    confirmModal.title = options.title || '确认'
    confirmModal.message = message
    confirmModal.confirmText = options.confirmText || '确定'
    confirmModal.cancelText = options.cancelText || '取消'
    confirmModal.resolve = resolve
  })
}

const handleConfirmOk = () => {
  confirmModal.visible = false
  if (confirmModal.resolve) {
    confirmModal.resolve(true)
  }
}

const handleConfirmCancel = () => {
  confirmModal.visible = false
  if (confirmModal.resolve) {
    confirmModal.resolve(false)
  }
}

// 暴露方法供外部使用
defineExpose({
  showToast,
  showConfirm,
  success: (msg, duration) => showToast(msg, 'success', duration),
  error: (msg, duration) => showToast(msg, 'error', duration),
  warning: (msg, duration) => showToast(msg, 'warning', duration),
  info: (msg, duration) => showToast(msg, 'info', duration)
})
</script>

<style scoped>
/* Toast Container */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--bg-primary, #fff);
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  border-left: 4px solid;
  min-width: 300px;
}

.toast-success {
  border-left-color: #48bb78;
}

.toast-success .toast-icon {
  color: #48bb78;
}

.toast-error {
  border-left-color: #f56565;
}

.toast-error .toast-icon {
  color: #f56565;
}

.toast-warning {
  border-left-color: #ed8936;
}

.toast-warning .toast-icon {
  color: #ed8936;
}

.toast-info {
  border-left-color: #667eea;
}

.toast-info .toast-icon {
  color: #667eea;
}

.toast-icon {
  flex-shrink: 0;
}

.toast-icon svg {
  width: 22px;
  height: 22px;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary, #1a202c);
  line-height: 1.5;
}

.toast-close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-muted, #a0aec0);
  border-radius: 4px;
  transition: all 0.2s;
}

.toast-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--text-primary, #1a202c);
}

.toast-close svg {
  width: 16px;
  height: 16px;
}

/* Toast Transitions */
.toast-enter-active {
  animation: toast-in 0.3s ease-out;
}

.toast-leave-active {
  animation: toast-out 0.3s ease-in;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes toast-out {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
}

/* Modal Overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10001;
  padding: 20px;
}

.modal-container {
  background: var(--bg-primary, #fff);
  border-radius: 16px;
  padding: 32px;
  max-width: 400px;
  width: 100%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.modal-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.modal-icon svg {
  width: 28px;
  height: 28px;
}

.modal-icon-warning {
  background: rgba(237, 137, 54, 0.1);
  color: #ed8936;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1a202c);
  text-align: center;
}

.modal-message {
  font-size: 15px;
  color: var(--text-secondary, #4a5568);
  text-align: center;
  line-height: 1.6;
  margin-bottom: 28px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-actions .btn {
  flex: 1;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-actions .btn-secondary {
  background: var(--bg-secondary, #f7fafc);
  border: 1px solid var(--border-color, #e2e8f0);
  color: var(--text-secondary, #4a5568);
}

.modal-actions .btn-secondary:hover {
  background: var(--border-color, #e2e8f0);
}

.modal-actions .btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;
}

.modal-actions .btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* Modal Transitions */
.modal-enter-active {
  animation: modal-in 0.3s ease-out;
}

.modal-leave-active {
  animation: modal-out 0.2s ease-in;
}

.modal-enter-active .modal-container {
  animation: modal-content-in 0.3s ease-out;
}

.modal-leave-active .modal-container {
  animation: modal-content-out 0.2s ease-in;
}

@keyframes modal-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes modal-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

@keyframes modal-content-in {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes modal-content-out {
  from {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
  to {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
}

/* Mobile Responsive */
@media (max-width: 480px) {
  .toast-container {
    left: 20px;
    right: 20px;
    max-width: none;
  }
  
  .toast {
    min-width: auto;
  }
  
  .modal-container {
    padding: 24px;
  }
}
</style>
