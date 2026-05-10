// 全局消息服务
import { ref, reactive, createApp, h } from 'vue'

// Toast 状态
const toasts = ref([])
let toastId = 0

// Confirm Modal 状态
const confirmModal = reactive({
  visible: false,
  title: '确认',
  message: '',
  confirmText: '确定',
  cancelText: '取消',
  resolve: null
})

// Toast 方法
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

// Confirm Modal 方法
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

// 导出消息服务
export const message = {
  success: (msg, duration) => showToast(msg, 'success', duration),
  error: (msg, duration) => showToast(msg, 'error', duration),
  warning: (msg, duration) => showToast(msg, 'warning', duration),
  info: (msg, duration) => showToast(msg, 'info', duration),
  confirm: showConfirm
}

// 导出状态供组件使用
export const messageState = {
  toasts,
  confirmModal,
  removeToast,
  handleConfirmOk,
  handleConfirmCancel
}

export default message
