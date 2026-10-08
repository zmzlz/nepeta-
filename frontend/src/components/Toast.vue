<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { CheckCircle, AlertCircle, XCircle, Info } from 'lucide-vue-next'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

interface ToastProps {
  message: string
  type?: ToastType
  duration?: number
}

const props = withDefaults(defineProps<ToastProps>(), {
  type: 'info',
  duration: 3000
})

const emit = defineEmits(['close'])
const isVisible = ref(false)

onMounted(() => {
  // 短暂延迟允许动画开始
  setTimeout(() => {
    isVisible.value = true
  }, 10)

  // 自动关闭
  if (props.duration > 0) {
    setTimeout(() => {
      close()
    }, props.duration)
  }
})

const close = () => {
  isVisible.value = false
  setTimeout(() => {
    emit('close')
  }, 300) // 等待过渡动画
}
</script>

<template>
  <div 
    class="fixed top-24 left-1/2 -translate-x-1/2 z-[100] flex items-center gap-3 px-6 py-3 rounded-full shadow-lg border transition-all duration-300 transform"
    :class="[
      isVisible ? 'translate-y-0 opacity-100' : '-translate-y-4 opacity-0',
      type === 'success' ? 'bg-white border-green-100 text-green-600' :
      type === 'error' ? 'bg-white border-red-100 text-red-600' :
      type === 'warning' ? 'bg-white border-orange-100 text-orange-600' :
      'bg-white border-blue-100 text-blue-600'
    ]"
  >
    <CheckCircle v-if="type === 'success'" class="w-5 h-5 fill-green-50" />
    <XCircle v-if="type === 'error'" class="w-5 h-5 fill-red-50" />
    <AlertCircle v-if="type === 'warning'" class="w-5 h-5 fill-orange-50" />
    <Info v-if="type === 'info'" class="w-5 h-5 fill-blue-50" />
    
    <span class="font-bold text-sm">{{ message }}</span>
  </div>
</template>