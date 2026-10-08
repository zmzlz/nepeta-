<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, RouterLink, useRouter } from 'vue-router'
import { Search, ShoppingCart, Store, BookOpen, GraduationCap, UtensilsCrossed } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isScrolled = ref(false)
let ticking = false

const handleScroll = () => {
  if (!ticking) {
    window.requestAnimationFrame(() => {
      isScrolled.value = window.scrollY > 20
      ticking = false
    })
    ticking = true
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll() // 初始检查
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 根据路由路径确定当前所在的板块
const currentSection = computed(() => {
  if (route.path.startsWith('/blog')) return 'blog'
  if (route.path.startsWith('/classroom')) return 'classroom'
  if (route.path.startsWith('/market')) return 'market'
  return 'home'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

interface HeaderConfig {
  logoText: string;
  logoColor: string;
  logoIcon: any;
  navs: { name: string; path: string }[];
  isLoggedIn: boolean;
  user: any;
  showSearch?: boolean;
  showPublish?: boolean;
  placeholder?: string;
  showSearchIcon?: boolean;
  showCart?: boolean;
}

// 各个板块的头部导航配置
const headerConfig = computed<HeaderConfig>(() => {
  const common = {
    isLoggedIn: userStore.isLoggedIn,
    user: userStore.user
  }
  
  switch (currentSection.value) {
    case 'blog':
      return {
        ...common,
        logoText: '小博客',
        logoColor: 'bg-[#FF6B6B]', // 调整以匹配 UI
        logoIcon: BookOpen,
        navs: [
          { name: '首页', path: '/' },
          { name: '博客广场', path: '/blog' },
        ],
        showSearch: true,
        showPublish: true,
        placeholder: '搜索内容',
      }
    case 'classroom':
      return {
        ...common,
        logoText: '粿粿小课堂',
        logoColor: 'bg-emerald-500', // 已调整
        logoIcon: GraduationCap,
        navs: [
          { name: '首页', path: '/' },
          { name: '课程', path: '/classroom' },
        ],
        showSearchIcon: true,
      }
    case 'market':
      return {
        ...common,
        logoText: '源源小市集',
        logoColor: 'bg-amber-500', // 已调整
        logoIcon: Store,
        navs: [
          { name: '首页', path: '/' },
          { name: '市集', path: '/market' },
        ],
        showCart: true,
      }
    default: // 首页
      return {
        ...common,
        logoText: '桃粿源', // 项目名称
        logoColor: 'bg-rose-500',
        logoIcon: UtensilsCrossed,
        navs: [
          { name: '首页', path: '/' },
          { name: '桃桃小博客', path: '/blog' },
          { name: '粿粿小课堂', path: '/classroom' },
          { name: '源源小市集', path: '/market' },
        ],
        showSearchIcon: true,
      }
  }
})
</script>

<template>
  <div v-if="currentSection === 'home'" :class="['fixed top-0 left-0 w-full z-50 flex justify-center transition-all duration-[400ms] ease-out', isScrolled ? 'pt-0' : 'pt-10']">
    <header 
      :class="[
        'bg-white/95 backdrop-blur-md transition-all duration-[400ms] ease-out',
        isScrolled 
          ? 'w-full rounded-none border-b border-zinc-100 shadow-sm' 
          : 'w-[95%] max-w-6xl rounded-full shadow-[0_15px_30px_-5px_rgba(0,0,0,0.15)] border-[4px] border-white/90'
      ]"
    >
      <div :class="['mx-auto flex items-center justify-between transition-all duration-[400ms] ease-out', isScrolled ? 'container px-4 h-16' : 'px-8 h-16']">
        <!-- Logo标志 -->
        <RouterLink to="/" class="flex items-center gap-2 group">
          <div class="w-8 h-8 rounded-lg shadow-sm overflow-hidden transition-transform group-hover:scale-105 bg-white border border-zinc-100 flex items-center justify-center">
            <img src="/ico (2).png" alt="Logo" class="w-full h-full object-contain scale-110" />
          </div>
          <span class="font-extrabold text-xl text-zinc-800 tracking-tight">{{ headerConfig.logoText }}</span>
        </RouterLink>

        <!-- 导航栏 -->
        <nav class="hidden md:flex items-center gap-8">
          <RouterLink 
            v-for="item in headerConfig.navs" 
            :key="item.name"
            :to="item.path" 
            class="text-zinc-600 font-bold hover:text-rose-500 transition-colors relative py-1 after:content-[''] after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-rose-500 after:transition-all hover:after:w-full"
            active-class="text-rose-500 after:w-full"
          >
            {{ item.name }}
          </RouterLink>
        </nav>

        <!-- 右侧操作区 -->
        <div class="flex items-center gap-4">
          <div v-if="userStore.isLoggedIn">
             <div class="flex items-center gap-3">
               <RouterLink :to="currentSection === 'market' ? '/profile?mode=market' : '/profile?mode=blog'" class="flex items-center gap-2 cursor-pointer hover:bg-zinc-50 p-1 rounded-full pr-3 transition-colors">
                  <img :src="userStore.user?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20avatar%20small&image_size=square'" alt="Avatar" class="w-8 h-8 rounded-full border border-zinc-200 object-cover" />
                  <span class="text-sm font-bold text-zinc-700 hidden lg:block">{{ userStore.user?.nickname }}</span>
               </RouterLink>
               <button @click="handleLogout" class="text-xs text-zinc-400 hover:text-zinc-600 font-medium">退出</button>
             </div>
          </div>
          <div v-else class="flex items-center gap-3">
            <button @click="userStore.openAuthModal()" class="px-5 py-1.5 bg-rose-500 hover:bg-rose-600 text-white rounded-full text-sm font-bold shadow-md shadow-rose-200 transition-all transform hover:-translate-y-0.5">
              登录
            </button>
          </div>
        </div>
      </div>
    </header>
  </div>

  <header v-else class="bg-white/90 backdrop-blur-md sticky top-0 z-50 border-b border-zinc-100 shadow-sm">
    <div class="container mx-auto px-4 h-16 flex items-center justify-between">
      <!-- Logo标志 -->
      <RouterLink to="/" class="flex items-center gap-2 group">
        <div class="w-8 h-8 rounded-lg shadow-sm overflow-hidden transition-transform group-hover:scale-105 bg-white border border-zinc-100 flex items-center justify-center">
          <img src="/ico (2).png" alt="Logo" class="w-full h-full object-contain scale-110" />
        </div>
        <span class="font-extrabold text-xl text-zinc-800 tracking-tight">{{ headerConfig.logoText }}</span>
      </RouterLink>

      <!-- 导航栏 -->
      <nav class="hidden md:flex items-center gap-8">
        <RouterLink 
          v-for="item in headerConfig.navs" 
          :key="item.name"
          :to="item.path" 
          class="text-zinc-600 font-bold hover:text-rose-500 transition-colors relative py-1 after:content-[''] after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-rose-500 after:transition-all hover:after:w-full"
          active-class="text-rose-500 after:w-full"
        >
          {{ item.name }}
        </RouterLink>
      </nav>

      <!-- 右侧操作区 -->
      <div class="flex items-center gap-4">
        <!-- 搜索栏 (博客) -->
        <div v-if="headerConfig.showSearch" class="hidden md:flex relative">
          <input 
            type="text" 
            :placeholder="headerConfig.placeholder"
            class="pl-9 pr-4 py-1.5 bg-zinc-100 rounded-full text-sm w-48 focus:outline-none focus:ring-2 focus:ring-rose-200 transition-all"
          />
          <Search class="w-4 h-4 text-zinc-400 absolute left-3 top-1/2 -translate-y-1/2" />
        </div>

        <!-- 发布按钮 (博客) -->
        <RouterLink 
          v-if="headerConfig.showPublish" 
          to="/blog/create" 
          class="hidden md:flex items-center justify-center px-5 py-1.5 bg-[#FF6B6B] text-white rounded-full text-sm font-bold shadow-md hover:bg-rose-500 transition-all"
        >
          发布
        </RouterLink>

        <!-- 购物车徽标 (市集) -->
        <template v-if="headerConfig.showCart">
          <button @click="cartStore.openCart()" class="p-2 text-zinc-600 hover:bg-zinc-100 rounded-full transition-colors relative">
            <ShoppingCart class="w-5 h-5" />
            <!-- 只有存在商品时显示红点 -->
            <span v-if="cartStore.hasItems" class="absolute top-1 right-1 w-2.5 h-2.5 bg-rose-500 rounded-full border-2 border-white"></span>
          </button>
        </template>

        <!-- 认证状态 -->
        <div v-if="userStore.isLoggedIn">
           <div class="flex items-center gap-3">
             <RouterLink :to="currentSection === 'market' ? '/profile?mode=market' : '/profile?mode=blog'" class="flex items-center gap-2 cursor-pointer hover:bg-zinc-50 p-1 rounded-full pr-3 transition-colors">
                <img :src="userStore.user?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20avatar%20small&image_size=square'" alt="Avatar" class="w-8 h-8 rounded-full border border-zinc-200 object-cover" />
                <span class="text-sm font-bold text-zinc-700 hidden lg:block">{{ userStore.user?.nickname }}</span>
             </RouterLink>
             <button @click="handleLogout" class="text-xs text-zinc-400 hover:text-zinc-600 font-medium">退出</button>
           </div>
        </div>

        <div v-else class="flex items-center gap-3">
          <button @click="userStore.openAuthModal()" class="px-5 py-1.5 bg-rose-500 hover:bg-rose-600 text-white rounded-full text-sm font-bold shadow-md shadow-rose-200 transition-all transform hover:-translate-y-0.5">
            登录
          </button>
        </div>

      </div>
    </div>
  </header>
</template>
