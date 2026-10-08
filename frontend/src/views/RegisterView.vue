<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UploadCloud, Image as ImageIcon } from 'lucide-vue-next'
import axios from 'axios'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const toast = useToast()

const form = ref({
  username: '',
  password: '',
  nickname: '',
  avatar: ''
})

const fileInput = ref<HTMLInputElement | null>(null)
const isUploading = ref(false)

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  const file = input.files[0]
  if (!file.type.startsWith('image/')) {
    toast.error('请上传图片文件')
    return
  }

  isUploading.value = true
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await axios.post('/api/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.value.avatar = res.data.url
    toast.success('头像上传成功')
  } catch (error) {
    console.error('Upload failed:', error)
    toast.error('头像上传失败')
  } finally {
    isUploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

const loading = ref(false)
const error = ref('')

const handleRegister = async () => {
  if (!form.value.avatar) {
    error.value = '请上传头像'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await userStore.register(form.value)
    toast.success('注册成功')
    router.push('/')
  } catch (err: any) {
    error.value = '注册失败，用户名可能已存在'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-zinc-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8 bg-white p-8 rounded-xl shadow-lg">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-zinc-900">
          注册桃粿源账号
        </h2>
        <p class="mt-2 text-center text-sm text-zinc-600">
          已有账号？
          <router-link to="/login" class="font-medium text-rose-500 hover:text-rose-400">
            直接登录
          </router-link>
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div>
            <label class="block text-sm font-medium text-zinc-700 mb-2">上传头像</label>
            <div 
              @click="triggerUpload"
              class="w-full h-32 border-2 border-dashed border-zinc-300 rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-rose-500 hover:bg-rose-50 transition-all group relative overflow-hidden"
            >
               <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleFileChange" />
               
               <img v-if="form.avatar" :src="form.avatar" class="w-full h-full object-contain absolute inset-0 z-10" />
               
               <div v-if="!form.avatar" class="flex flex-col items-center text-zinc-400 group-hover:text-rose-500 transition-colors">
                  <div v-if="isUploading" class="animate-spin rounded-full h-8 w-8 border-b-2 border-rose-500 mb-2"></div>
                  <template v-else>
                    <UploadCloud class="w-8 h-8 mb-2" />
                    <span class="text-xs font-bold">点击上传头像</span>
                  </template>
               </div>
               
               <div v-if="form.avatar" class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-20 text-white font-bold text-xs">
                  更换图片
               </div>
            </div>
        </div>

          <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="username" class="sr-only">用户名</label>
            <input
              id="username"
              v-model="form.username"
              name="username"
              type="text"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-zinc-300 placeholder-zinc-500 text-zinc-900 rounded-t-md focus:outline-none focus:ring-rose-500 focus:border-rose-500 focus:z-10 sm:text-sm"
              placeholder="用户名 (登录账号)"
            />
          </div>
          <div>
            <label for="nickname" class="sr-only">昵称</label>
            <input
              id="nickname"
              v-model="form.nickname"
              name="nickname"
              type="text"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-zinc-300 placeholder-zinc-500 text-zinc-900 focus:outline-none focus:ring-rose-500 focus:border-rose-500 focus:z-10 sm:text-sm"
              placeholder="昵称 (显示的名称)"
            />
          </div>
          <div>
            <label for="password" class="sr-only">密码</label>
            <input
              id="password"
              v-model="form.password"
              name="password"
              type="password"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-zinc-300 placeholder-zinc-500 text-zinc-900 rounded-b-md focus:outline-none focus:ring-rose-500 focus:border-rose-500 focus:z-10 sm:text-sm"
              placeholder="密码"
            />
          </div>
        </div>

        <div v-if="error" class="text-red-500 text-sm text-center">
          {{ error }}
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-rose-500 hover:bg-rose-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500 disabled:opacity-50"
          >
            <span v-if="loading">注册中...</span>
            <span v-else>注册</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
