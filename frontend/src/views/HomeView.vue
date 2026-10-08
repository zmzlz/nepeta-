<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Heart, MessageCircle, Star, ShoppingCart, Leaf, Clock, Award } from 'lucide-vue-next'
import { blogApi, marketApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { BlogPost, Product } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const recentPosts = ref<BlogPost[]>([])
const recentProducts = ref<Product[]>([])

onMounted(async () => {
  try {
    const posts = await blogApi.getPosts()
    if (Array.isArray(posts)) {
      recentPosts.value = posts.slice(0, 3)
    }
    
    const products = await marketApi.getProducts()
    if (Array.isArray(products)) {
      recentProducts.value = products.slice(0, 4)
    }
  } catch (error) {
    console.error('Failed to fetch home data:', error)
  }
})

const goToPostDetail = (id: number) => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  router.push(`/blog/${id}`)
}

const goToMarket = () => {
  router.push('/market')
}

const goToClassroom = () => {
  router.push('/classroom')
}

const goToTutorial = (id: number) => {
  router.push(`/classroom/${id}`)
}
</script>

<template>
  <div class="bg-white w-full">
    <!-- 横幅区域（完美 1:1 布局匹配） -->
    <section class="relative w-full h-[100vh] min-h-[700px] bg-white flex items-center justify-center overflow-hidden">
      
      <!-- 主波浪/撕纸边框（现在在圆圈后面） -->
      <div class="absolute w-[95%] h-[90%] max-w-[1400px] z-0" style="
        filter: drop-shadow(0 20px 30px rgba(0,0,0,0.15));
      ">
        <!-- 撕纸效果的 SVG 剪切路径 -->
        <svg width="0" height="0" class="absolute">
          <defs>
            <clipPath id="torn-paper-clip" clipPathUnits="objectBoundingBox">
              <path d="M 0.02,0.03 
                       C 0.1,0.01 0.3,0.04 0.5,0.02 
                       C 0.7,0.00 0.9,0.03 0.98,0.02 
                       C 0.99,0.2 0.97,0.5 0.99,0.8 
                       C 0.98,0.95 0.99,0.98 0.97,0.97 
                       C 0.8,0.99 0.5,0.96 0.3,0.98 
                       C 0.1,0.99 0.03,0.97 0.02,0.98 
                       C 0.01,0.8 0.03,0.5 0.02,0.2 
                       Z" />
            </clipPath>
          </defs>
        </svg>

        <!-- 带有剪切路径的内部内容容器 -->
        <div class="w-full h-full" style="clip-path: url(#torn-paper-clip);">
          <!-- 边框内的视频背景 -->
          <div class="relative w-full h-full overflow-hidden bg-zinc-100">
            <video 
              src="../assets/images/h1.mp4" 
              autoplay 
              loop 
              muted 
              playsinline
              class="absolute inset-0 w-full h-full object-cover scale-[1.02]"
            ></video>

            <!-- 文本叠加层（左侧居中） -->
            <div class="absolute left-[15%] top-1/2 -translate-y-1/2 flex flex-col items-center md:items-start z-20">
              <!-- 主标题 -->
              <div class="text-white text-5xl md:text-7xl font-black tracking-widest text-center md:text-left drop-shadow-xl" style="text-shadow: 2px 4px 8px rgba(0,0,0,0.4);">
                桃粿源里
                <!-- 文本下方的小曲线装饰 -->
                <svg width="80" height="20" viewBox="0 0 60 15" class="mt-2 mx-auto md:ml-0 fill-none stroke-rose-400 stroke-[3px]" style="filter: drop-shadow(1px 2px 2px rgba(0,0,0,0.3));">
                  <path d="M 5,10 Q 30,20 50,5" />
                  <circle cx="55" cy="5" r="2.5" fill="#fb7185" stroke="none" />
                </svg>
              </div>
              
              <!-- 副标题 -->
              <div class="text-white text-3xl md:text-5xl font-bold mt-4 text-center md:text-left tracking-wider drop-shadow-lg" style="text-shadow: 2px 2px 6px rgba(0,0,0,0.5);">
                粿香满溢
              </div>

              <!-- 小字文案 -->
              <div class="text-white/90 text-lg md:text-xl font-medium mt-6 text-center md:text-left leading-relaxed tracking-wide" style="text-shadow: 1px 1px 3px rgba(0,0,0,0.6);">
                潮汕的甜，都在这粿里<br/>
                一口桃粿，一口潮汕情
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 悬浮黄色圆圈背景（现在在边框上方，z-10） -->
      <!-- 左侧（从上到下） -->
      <div class="absolute -top-10 -left-10 w-[250px] h-[250px] md:w-[320px] md:h-[320px] bg-[#FFD166] rounded-full z-10"></div>
      <div class="absolute top-[25%] -left-8 w-[160px] h-[160px] md:w-[220px] md:h-[220px] bg-[#FFD166] rounded-full z-10 opacity-90"></div>
      <div class="absolute bottom-10 left-8 w-[120px] h-[120px] md:w-[150px] md:h-[150px] bg-[#FFD166] rounded-full z-10"></div>
      
      <!-- 顶部 / 顶部居中 -->
      <div class="absolute -top-8 left-[60%] w-[120px] h-[120px] md:w-[140px] md:h-[140px] bg-[#FFD166] rounded-full z-10"></div>
      
      <!-- 底部居中 -->
      <div class="absolute -bottom-16 left-[35%] w-[180px] h-[180px] md:w-[240px] md:h-[240px] bg-[#FFD166] rounded-full z-10 opacity-95"></div>
      
      <!-- 右侧（从上到下） -->
      <div class="absolute top-[10%] -right-10 w-[200px] h-[200px] md:w-[260px] md:h-[260px] bg-[#FFD166] rounded-full z-10"></div>
      <div class="absolute top-[45%] right-10 w-[140px] h-[140px] md:w-[180px] md:h-[180px] bg-[#FFD166] rounded-full z-10"></div>

      <!-- 前景徽章（必须是最高 z-index） -->
      <!-- 潮汕粿粿小天地 Badge (Replaced Play Movie) -->
      <div class="absolute bottom-20 right-[22%] flex items-center gap-3 z-30 cursor-pointer group transform hover:scale-105 transition-transform hover:-rotate-2">
          <div class="bg-rose-500/90 backdrop-blur px-5 py-2.5 rounded-full shadow-xl flex items-center gap-2 border-2 border-white/50">
            <Leaf class="w-5 h-5 text-white" />
            <span class="font-bold text-white text-base tracking-wider">潮汕粿粿小天地</span>
          </div>
      </div>

      <!-- 桃粿源打卡点 Badge (Replaced Instagram character) -->
      <div class="absolute bottom-12 right-16 z-30">
          <div class="w-28 h-28 rounded-full bg-[#FFD166] flex flex-col items-center justify-center border-4 border-white shadow-xl transform rotate-6 hover:rotate-0 transition-transform relative cursor-pointer group">
            <div class="absolute -top-3 bg-white px-2 py-0.5 rounded-full text-xs font-bold text-rose-500 shadow-sm whitespace-nowrap">热门</div>
            <Heart class="w-8 h-8 text-rose-500 mb-1 group-hover:scale-110 transition-transform" />
            <span class="font-black text-sm text-dark-900 leading-tight text-center tracking-wide">桃粿源<br/>打卡点</span>
          </div>
      </div>

    </section>

    <!-- 第一板块：桃桃小博客 -->
    <section class="container mx-auto px-6 md:px-12 py-16">
      <!-- 板块分割标题 -->
      <div class="text-center mb-10">
        <div class="inline-block px-3 py-1 bg-secondary-orange text-white rounded-full text-xs font-bold mb-3 shadow-sm">
          热门内容
        </div>
        <h2 class="text-3xl font-extrabold text-dark-900 mb-2">桃桃小博客</h2>
        
  
      </div>

      <!-- 博客卡片行 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div v-for="post in recentPosts" :key="post.id" @click="goToPostDetail(post.id)" class="bg-white rounded-3xl p-5 shadow-card hover:shadow-soft transition-all duration-300 border border-zinc-50 flex flex-col group cursor-pointer">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-full bg-zinc-200 overflow-hidden ring-2 ring-white shadow-sm">
               <img src="../assets/images/85.png" class="w-full h-full object-cover"/>
            </div>
            <span class="font-bold text-dark-900">{{ post.author?.username || '桃桃' }}</span>
            <span class="ml-auto px-2.5 py-1 bg-secondary-yellow text-white rounded-lg text-xs font-bold">{{ post.category }}</span>
          </div>
          
          <div class="mb-4 overflow-hidden rounded-2xl h-48 relative">
             <img :src="post.coverImage" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"/>
          </div>

          <h3 class="text-lg font-bold text-dark-900 mb-2 leading-snug">{{ post.title }}</h3>
          
          <div class="mt-auto flex items-center justify-between pt-2">
            <span class="text-xs text-dark-500 font-medium">刚刚 · {{ post.views }} 阅读</span>
            <div class="flex gap-3 text-dark-400">
              <Heart class="w-4 h-4 hover:text-primary-500 cursor-pointer transition-colors" />
              <MessageCircle class="w-4 h-4 hover:text-primary-500 cursor-pointer transition-colors" />
            </div>
          </div>
        </div>
        
        <!-- 无文章时的后备显示 -->
        <div v-if="recentPosts.length === 0" class="col-span-3 text-center text-gray-400 py-10">
          暂无热门内容
        </div>
      </div>
      
      <div class="text-center mt-12">
        <RouterLink to="/blog" class="inline-flex items-center gap-2 px-8 py-2.5 border-2 border-zinc-200 text-zinc-500 rounded-full font-bold hover:border-primary-500 hover:text-primary-500 transition-colors">
          查看全部
        </RouterLink>
      </div>
    </section>

    <!-- 第二板块：粿粿小课堂 -->
    <section class="bg-cream-200/50 py-16">
      <div class="container mx-auto px-6 md:px-12">
        <div class="flex items-center justify-between mb-10">
          <div>
            <div class="inline-block px-3 py-1 bg-secondary-green text-white rounded-full text-xs font-bold mb-2 shadow-sm">精选</div>
            <h2 class="text-3xl font-extrabold text-dark-900">桃桃小课堂</h2>
            <p class="text-dark-500 text-sm font-medium mt-1">轻松上手，简单又好玩</p>
          </div>
          <RouterLink to="/classroom" class="text-primary-500 font-bold hover:underline">更多 ></RouterLink>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- 卡片 1 -->
        <div @click="goToTutorial(1)" class="md:col-span-1 group cursor-pointer">
            <div class="relative rounded-3xl overflow-hidden mb-4 shadow-md aspect-[4/3] bg-white border border-zinc-100 flex items-center justify-center p-4">
              <img 
                src="../assets/images/25.png" 
                class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-700"
              />
            </div>
            <h3 class="text-xl font-bold text-dark-900 mb-1">红桃粿</h3>
            <p class="text-xs text-dark-500 mb-2 font-medium">超详细的红桃粿制作教程</p>
            <div class="flex items-center gap-4 text-xs text-dark-500 font-medium">
              <span class="flex items-center gap-1"><Clock class="w-3 h-3"/> 60min</span>
              <span class="flex items-center gap-1"><Award class="w-3 h-3"/> 中级</span>
            </div>
          </div>

          <!-- 卡片 2 -->
          <div @click="goToTutorial(2)" class="md:col-span-1 group cursor-pointer">
            <div class="relative rounded-3xl overflow-hidden mb-4 shadow-md aspect-[4/3] bg-white border border-zinc-100 flex items-center justify-center p-4">
              <img 
                src="../assets/images/26.png" 
                class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-700"
              />
            </div>
            <h3 class="text-xl font-bold text-dark-900 mb-1">鼠曲粿</h3>
            <p class="text-xs text-dark-500 mb-2 font-medium">清香四溢的传统味道</p>
            <div class="flex items-center gap-4 text-xs text-dark-500 font-medium">
              <span class="flex items-center gap-1"><Clock class="w-3 h-3"/> 90min</span>
              <span class="flex items-center gap-1"><Award class="w-3 h-3"/> 中级</span>
            </div>
          </div>

          <!-- 卡片 3 -->
          <div @click="goToTutorial(3)" class="md:col-span-1 group cursor-pointer">
            <div class="relative rounded-3xl overflow-hidden mb-4 shadow-md aspect-[4/3] bg-white border border-zinc-100 flex items-center justify-center p-4">
              <img 
                src="../assets/images/27.png" 
                class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-700"
              />
            </div>
            <h3 class="text-xl font-bold text-dark-900 mb-1">朴籽粿</h3>
            <p class="text-xs text-dark-500 mb-2 font-medium">潮汕春日限定美味</p>
            <div class="flex items-center gap-4 text-xs text-dark-500 font-medium">
              <span class="flex items-center gap-1"><Clock class="w-3 h-3"/> 45min</span>
              <span class="flex items-center gap-1"><Award class="w-3 h-3"/> 初级</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 第三板块：源源小市集 -->
    <section class="container mx-auto px-6 md:px-12 py-16">
      <div class="mb-10 text-center">
        <div class="inline-block px-3 py-1 bg-secondary-purple text-white rounded-full text-xs font-bold mb-3 shadow-sm">
          发现
        </div>
        <h2 class="text-3xl font-extrabold text-dark-900 mb-2">趣淘小市集</h2>
        <p class="text-dark-500 text-sm font-medium">好物分享，精挑细选，价格亲民</p>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
        <div v-for="product in recentProducts" :key="product.id" @click="goToMarket" class="bg-white p-4 rounded-3xl shadow-card hover:shadow-soft transition-all border border-zinc-50 group cursor-pointer">
          <div class="relative bg-zinc-50 rounded-2xl mb-4 overflow-hidden aspect-square">
            <img :src="product.imageUrl" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
          </div>
          <h3 class="font-bold text-dark-900 text-sm mb-2">{{ product.name }}</h3>
          <div class="flex items-center justify-between">
            <span class="text-secondary-orange font-extrabold text-lg">¥{{ product.price }}</span>
            <span class="text-[10px] text-zinc-400 bg-zinc-100 px-2 py-1 rounded-full">包邮</span>
          </div>
        </div>
        
        <!-- 无商品时的后备显示 -->
        <div v-if="recentProducts.length === 0" class="col-span-4 text-center text-gray-400 py-10">
          暂无商品
        </div>
      </div>

      <div class="text-center mt-12">
        <RouterLink to="/market" class="px-10 py-3 bg-secondary-yellow text-dark-900 font-bold rounded-full hover:bg-yellow-400 transition-colors shadow-lg shadow-yellow-200">
          继续逛逛
        </RouterLink>
      </div>
    </section>

  </div>
</template>
