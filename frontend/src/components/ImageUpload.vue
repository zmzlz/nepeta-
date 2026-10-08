<script setup lang="ts">
import { ref } from 'vue'
import { UploadCloud, X, Image as ImageIcon } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import axios from 'axios'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits(['update:modelValue'])
const toast = useToast()
const fileInput = ref<HTMLInputElement | null>(null)
const isUploading = ref(false)

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  const file = input.files[0]
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    toast.error('请上传图片文件')
    return
  }

  // 验证文件大小 (例如 5MB)
  if (file.size > 5 * 1024 * 1024) {
    toast.error('图片大小不能超过5MB')
    return
  }

  isUploading.value = true
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await axios.post('/api/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 假设后端返回 { url: '...' }
    emit('update:modelValue', res.data.url)
    toast.success('上传成功')
  } catch (error) {
    console.error('Upload failed:', error)
    toast.error('图片上传失败')
  } finally {
    isUploading.value = false
    // 重置输入
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

const removeImage = () => {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="w-full">
    <input 
      type="file" 
      ref="fileInput" 
      class="hidden" 
      accept="image/*"
      @change="handleFileChange"
    />

    <div v-if="modelValue" class="relative group rounded-xl overflow-hidden border border-zinc-200 bg-zinc-50">
      <img :src="modelValue" class="w-full h-48 object-cover" />
      <button 
        @click.stop="removeImage" 
        class="absolute top-2 right-2 p-1.5 bg-black/50 hover:bg-red-500 text-white rounded-full transition-colors opacity-0 group-hover:opacity-100"
      >
        <X class="w-4 h-4" />
      </button>
    </div>

    <div 
      v-else
      @click="triggerUpload"
      class="w-full h-48 border-2 border-dashed border-zinc-300 rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-rose-400 hover:bg-rose-50/30 transition-all group"
    >
      <div v-if="isUploading" class="flex flex-col items-center text-rose-500">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-rose-500 mb-2"></div>
        <span class="text-xs font-bold">上传中...</span>
      </div>
      <div v-else class="flex flex-col items-center text-zinc-400 group-hover:text-rose-500">
        <UploadCloud class="w-10 h-10 mb-2" />
        <span class="text-sm font-bold">点击上传图片</span>
        <span class="text-xs mt-1 opacity-70">支持 JPG, PNG, GIF (Max 5MB)</span>
      </div>
    </div>
  </div>
</template>
