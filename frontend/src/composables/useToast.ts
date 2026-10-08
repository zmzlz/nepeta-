import { ref, createApp, h } from 'vue'
import Toast, { type ToastType } from '@/components/Toast.vue'

const container = document.createElement('div')
document.body.appendChild(container)

export function useToast() {
  const show = (message: string, type: ToastType = 'info', duration = 3000) => {
    const wrapper = document.createElement('div')
    container.appendChild(wrapper)

    const app = createApp({
      render() {
        return h(Toast, {
          message,
          type,
          duration,
          onClose: () => {
            app.unmount()
            container.removeChild(wrapper)
          }
        })
      }
    })

    app.mount(wrapper)
  }

  return {
    success: (msg: string) => show(msg, 'success'),
    error: (msg: string) => show(msg, 'error'),
    warning: (msg: string) => show(msg, 'warning'),
    info: (msg: string) => show(msg, 'info')
  }
}