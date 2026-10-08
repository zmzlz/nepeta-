<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import PuzzleCaptcha from '@/components/PuzzleCaptcha.vue'

const userStore = useUserStore()
const toast = useToast()
const isLogin = ref(true)
const loading = ref(false)
const showCaptcha = ref(false)
const isVerified = ref(false)
const rememberMe = ref(false)

const form = ref({
  username: '',
  password: '',
  nickname: '',
  avatar: 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20boy%20avatar%20flat%20design&image_size=square'
})

const avatars = [
  // 卡通 / 动漫
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20boy%20avatar%20flat%20design&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20girl%20avatar%20pink%20hair%20flat%20design&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cool%20anime%20boy%20avatar%20blue%20hair&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=beautiful%20anime%20girl%20avatar%20long%20hair&image_size=square',
  
  // 动物
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20cat%20avatar%20illustration&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20dog%20avatar%20illustration&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20panda%20avatar%20flat%20design&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cool%20fox%20avatar%20illustration&image_size=square',

  // 极简 / 抽象
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=minimalist%20robot%20avatar&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=pixel%20art%20avatar%20face&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=abstract%20geometric%20avatar%20colorful&image_size=square',
  'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=3d%20emoji%20avatar%20smile&image_size=square',
]

const showAvatarGrid = ref(false)

const selectAvatar = (url: string) => {
  form.value.avatar = url
  showAvatarGrid.value = false
}

const handleCaptchaSuccess = async () => {
  isVerified.value = true
  showCaptcha.value = false
  await performLogin()
}

const handleCaptchaFail = () => {
  toast.error('验证失败，请重试')
}

const performLogin = async () => {
  loading.value = true
  try {
    await userStore.login({
      username: form.value.username,
      password: form.value.password
    }, rememberMe.value)
    toast.success('登录成功')
    userStore.closeAuthModal()
    form.value = { username: '', password: '', nickname: '', avatar: '' }
  } catch (error) {
    console.error(error)
    toast.error('登录失败，请检查用户名或密码')
    isVerified.value = false // 失败时重置验证
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) {
    toast.error('请填写完整信息')
    return
  }
  if (!isLogin.value && !form.value.nickname) {
    toast.error('请填写昵称')
    return
  }

  if (isLogin.value && !isVerified.value) {
    showCaptcha.value = true
    return
  }

  loading.value = true
  try {
    if (isLogin.value) {
      await performLogin()
    } else {
      await userStore.register({
        username: form.value.username,
        password: form.value.password,
        nickname: form.value.nickname,
        avatar: form.value.avatar
      })
      toast.success('注册并登录成功')
      userStore.closeAuthModal()
      form.value = { 
          username: '', 
          password: '', 
          nickname: '',
          avatar: avatars[0] 
      }
    }
  } catch (error) {
    console.error(error)
    toast.error(isLogin.value ? '登录失败，请检查用户名或密码' : '注册失败，用户名可能已存在')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="userStore.showAuthModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4 backdrop-blur-sm animate-in fade-in duration-200">
    <div class="bg-white rounded-3xl w-full max-w-md p-8 shadow-2xl relative overflow-hidden">
      <!-- 装饰背景 -->
      <div class="absolute top-0 right-0 w-32 h-32 bg-rose-100 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
      <div class="absolute bottom-0 left-0 w-24 h-24 bg-orange-100 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2"></div>

      <button @click="userStore.closeAuthModal()" class="absolute top-4 right-4 p-2 hover:bg-zinc-100 rounded-full transition-colors z-10">
        <X class="w-5 h-5 text-zinc-400" />
      </button>

      <div class="relative z-10">
        <div class="text-center mb-8">
          <h2 class="text-2xl font-black text-dark-900 mb-2">欢迎来到桃粿源</h2>
          <p class="text-zinc-500 text-sm">探索潮汕传统美食文化</p>
        </div>

        <div class="flex bg-zinc-100 p-1 rounded-xl mb-6">
          <button 
            @click="isLogin = true"
            :class="['flex-1 py-2 rounded-lg text-sm font-bold transition-all', isLogin ? 'bg-white text-rose-500 shadow-sm' : 'text-zinc-500 hover:text-zinc-700']"
          >
            登录
          </button>
          <button 
            @click="isLogin = false"
            :class="['flex-1 py-2 rounded-lg text-sm font-bold transition-all', !isLogin ? 'bg-white text-rose-500 shadow-sm' : 'text-zinc-500 hover:text-zinc-700']"
          >
            注册
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div v-if="!isLogin" class="animate-in slide-in-from-top-2 fade-in duration-300">
             <div class="flex justify-center mb-6">
               <div class="relative group">
                 <div 
                   @click="showAvatarGrid = true"
                   class="w-24 h-24 rounded-full border-4 border-white shadow-lg cursor-pointer overflow-hidden bg-zinc-100 hover:scale-105 transition-transform relative z-10"
                 >
                   <img :src="form.avatar" class="w-full h-full object-cover" />
                   <div class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                     <span class="text-white text-xs font-bold">更换头像</span>
                   </div>
                 </div>
                 <div class="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-rose-500 text-white text-[10px] px-2 py-0.5 rounded-full font-bold shadow-sm z-20 whitespace-nowrap">
                   点击选择头像
                 </div>
               </div>
             </div>

             <!-- 头像选择弹窗/遮罩 -->
             <div v-if="showAvatarGrid" class="absolute inset-0 bg-white z-50 flex flex-col animate-in fade-in slide-in-from-bottom-4">
               <div class="p-4 border-b border-zinc-100 flex items-center justify-between">
                 <h3 class="font-bold text-zinc-800">选择您喜欢的头像</h3>
                 <button @click="showAvatarGrid = false" class="p-2 hover:bg-zinc-100 rounded-full transition-colors">
                   <X class="w-5 h-5 text-zinc-400" />
                 </button>
               </div>
               <div class="flex-1 overflow-y-auto p-4">
                 <div class="grid grid-cols-3 gap-3">
                   <div 
                     v-for="(avatar, index) in avatars" 
                     :key="index"
                     @click="selectAvatar(avatar)"
                     class="aspect-square rounded-xl overflow-hidden cursor-pointer border-2 transition-all hover:scale-105 shadow-sm"
                     :class="form.avatar === avatar ? 'border-rose-500 ring-2 ring-rose-200' : 'border-zinc-100 hover:border-rose-200'"
                   >
                     <img :src="avatar" class="w-full h-full object-cover" />
                   </div>
                 </div>
               </div>
             </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-zinc-500 mb-1 ml-1">用户名</label>
            <input 
              v-model="form.username" 
              type="text" 
              class="w-full px-4 py-3 bg-zinc-50 border-none rounded-xl focus:ring-2 focus:ring-rose-200 transition-all font-medium"
              placeholder="请输入用户名"
            />
          </div>
          
          <div v-if="!isLogin" class="animate-in slide-in-from-top-2 fade-in duration-300">
             <label class="block text-xs font-bold text-zinc-500 mb-1 ml-1">昵称</label>
            <input 
              v-model="form.nickname" 
              type="text" 
              class="w-full px-4 py-3 bg-zinc-50 border-none rounded-xl focus:ring-2 focus:ring-rose-200 transition-all font-medium"
              placeholder="怎么称呼您？"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-zinc-500 mb-1 ml-1">密码</label>
            <input 
              v-model="form.password" 
              type="password" 
              class="w-full px-4 py-3 bg-zinc-50 border-none rounded-xl focus:ring-2 focus:ring-rose-200 transition-all font-medium"
              placeholder="请输入密码"
            />
          </div>

          <div v-if="isLogin" class="flex items-center ml-1">
            <input 
              id="remember-me" 
              type="checkbox" 
              v-model="rememberMe"
              class="w-4 h-4 accent-rose-500 cursor-pointer"
            />
            <label for="remember-me" class="ml-2 block text-xs font-bold text-zinc-500 cursor-pointer select-none">
              3天内自动登录
            </label>
          </div>

          <button 
            type="submit" 
            :disabled="loading"
            class="w-full bg-rose-500 text-white py-3.5 rounded-xl font-bold shadow-lg shadow-rose-200 hover:bg-rose-600 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50 mt-6"
          >
            <span v-if="loading" class="animate-spin inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full mr-2"></span>
            {{ isLogin ? '立即登录' : '创建账号' }}
          </button>
        </form>

        <p class="text-center text-xs text-zinc-400 mt-6">
          {{ isLogin ? '还没有账号？' : '已有账号？' }}
          <button @click="isLogin = !isLogin" class="text-rose-500 font-bold hover:underline">
            {{ isLogin ? '去注册' : '去登录' }}
          </button>
        </p>
      </div>
    </div>
    
    <PuzzleCaptcha 
      :show="showCaptcha" 
      @success="handleCaptchaSuccess"
      @fail="handleCaptchaFail"
      @close="showCaptcha = false"
    />
  </div>
</template>
