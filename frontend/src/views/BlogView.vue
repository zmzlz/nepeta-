<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { Heart, MessageCircle, Star, Search } from 'lucide-vue-next'
import { blogApi } from '@/api'
import type { BlogPost } from '@/types'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const allPosts = ref<BlogPost[]>([])
const loading = ref(false)
const processingPosts = ref(new Set<number>())

// 标签和过滤器
const activeTab = ref('综合') // '综合', '最新', '热门', '关注'
const activeTag = ref('全部') // '全部', '美食', '旅行', '生活', '摄影', '手作', etc.

const tabs = ['综合', '最新', '热门', '关注']

const dynamicTags = computed(() => {
  const categories = new Set<string>()
  allPosts.value.forEach(post => {
    if (post.category) categories.add(post.category)
  })
  
  // 预设一些常见分类的图标
  const iconMap: Record<string, string> = {
    '美食': '🍔',
    '旅行': '✈️',
    '生活': '🏠',
    '摄影': '📷',
    '手作': '🧶',
    '烘焙': '🧁',
    '探店': '🏪',
    '茶饮': '🍵',
    '分享': '✨',
    '周边': '🛍️',
    '家居': '🛋️',
    '数码': '📱'
  }
  
  const generated = Array.from(categories).map(cat => ({
    name: cat
  }))
  
  // 确保如果后端没有数据时，至少显示基本标签，或者只显示实际存在的标签
  // 这里我们选择只显示实际存在的标签，加上'全部'
  return [{ name: '全部' }, ...generated]
})

const displayedPosts = computed(() => {
  let result = [...allPosts.value]

  // 按标签过滤
  if (activeTag.value !== '全部') {
    result = result.filter(post => post.category === activeTag.value)
  }

  // 按标签过滤和排序
  if (activeTab.value === '最新') {
    result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  } else if (activeTab.value === '热门') {
    result.sort((a, b) => (b.likes || 0) - (a.likes || 0))
  } else if (activeTab.value === '关注') {
    // 模拟关注功能：由于没有关注列表，这里仅过滤出非自己的帖子作为模拟
    if (userStore.isLoggedIn) {
      result = result.filter(post => post.author?.id !== userStore.user?.id)
    }
  } else {
    // 综合：默认排序（例如按ID降序或混合排序）
    result.sort((a, b) => b.id - a.id)
  }

  return result
})

const fetchPosts = async () => {
  loading.value = true
  try {
    // 一次性获取所有帖子，前端进行过滤和排序以保证标签和功能完全对应
    const response = await blogApi.getPosts(undefined, userStore.user?.id)
    allPosts.value = Array.isArray(response) ? response : []
  } catch (error) {
    console.error('Failed to fetch posts:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/blog/${id}`)
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = (now.getTime() - date.getTime()) / 1000 // 秒

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)}天前`
  return date.toLocaleDateString()
}

// 交互处理器
import { useToast } from '@/composables/useToast'
const toast = useToast()

const handleLike = async (post: BlogPost) => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if (processingPosts.value.has(post.id)) return
  
  processingPosts.value.add(post.id)
  
  // 乐观 UI 更新
  const originalState = { isLiked: post.isLiked, likes: post.likes }
  
  if (post.isLiked) {
    post.likes = Math.max(0, post.likes - 1)
    post.isLiked = false
  } else {
    post.likes++
    post.isLiked = true
  }

  try {
    await blogApi.likePost(post.id, userStore.user.id)
  } catch (error) {
    console.error('Like failed', error)
    // 恢复
    post.isLiked = originalState.isLiked
    post.likes = originalState.likes
    toast.error('操作失败')
  } finally {
    setTimeout(() => {
        processingPosts.value.delete(post.id)
    }, 500)
  }
}

const handleFavorite = async (post: BlogPost) => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if (processingPosts.value.has(post.id)) return
  
  processingPosts.value.add(post.id)
  
  // 乐观 UI 更新
  const originalState = { isFavorited: post.isFavorited, favorites: post.favorites }

  if (post.isFavorited) {
    post.favorites = Math.max(0, (post.favorites || 0) - 1)
    post.isFavorited = false
    toast.success('已取消收藏')
  } else {
    post.favorites = (post.favorites || 0) + 1
    post.isFavorited = true
    toast.success('已收藏')
  }

  try {
    await blogApi.favoritePost(post.id, userStore.user.id)
  } catch (error) {
    console.error('Favorite failed', error)
    // 恢复
    post.isFavorited = originalState.isFavorited
    post.favorites = originalState.favorites
    toast.error('操作失败')
  } finally {
    setTimeout(() => {
        processingPosts.value.delete(post.id)
    }, 500)
  }
}

// 删除 watch，因为我们使用计算属性进行过滤


onMounted(() => {
  fetchPosts()
})
</script>

<template>
  <div class="bg-[#FDFBF7] min-h-screen pb-20 font-sans">
    
    <!-- 横幅区域 -->
    <div class="container mx-auto px-4 pt-8 pb-6">
      <h1 class="text-3xl font-extrabold text-zinc-800 tracking-tight mb-2">发现美好生活</h1>
      <p class="text-zinc-400 text-sm font-medium">分享日常、记录点滴、交流心得</p>
    </div>

    <!-- 次级标签 -->
    <div class="container mx-auto px-4 mb-8 mt-4">
      <div class="flex items-center gap-2 overflow-x-auto no-scrollbar pb-2">
        <button 
            v-for="tag in dynamicTags" 
            :key="tag.name"
            @click="activeTag = tag.name"
            :class="[
              'px-4 py-1.5 rounded-full text-xs font-bold transition-all flex items-center gap-1 whitespace-nowrap border',
              activeTag === tag.name 
                ? 'border-[#FF6B6B] text-[#FF6B6B] bg-rose-50' 
                : 'border-zinc-100 bg-white text-zinc-500 hover:border-zinc-200'
            ]"
          >
            {{ tag.name }}
          </button>
      </div>
    </div>

    <!-- 卡片网格 -->
    <div class="container mx-auto px-4">
      <div v-if="loading" class="flex justify-center py-20">
        <div class="animate-spin rounded-full h-10 w-10 border-4 border-zinc-100 border-t-[#FF6B6B]"></div>
      </div>

      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
        <div 
          v-for="post in displayedPosts" 
          :key="post.id" 
          @click="goToDetail(post.id)" 
          class="bg-white rounded-2xl shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 overflow-hidden cursor-pointer group flex flex-col border border-zinc-50"
        >
          <!-- 图片轮播 -->
          <div class="aspect-[4/3] bg-zinc-100 relative overflow-hidden group/carousel">
            <div class="flex overflow-x-auto snap-x snap-mandatory no-scrollbar w-full h-full">
               <div v-for="(img, index) in (post.images && post.images.length ? post.images : [post.coverImage])" :key="index" class="snap-center shrink-0 w-full h-full">
                  <img :src="img" class="w-full h-full object-cover" />
               </div>
            </div>
            <!-- 指示器 -->
            <div v-if="(post.images?.length || 0) > 1" class="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1 bg-black/20 backdrop-blur px-1.5 py-1 rounded-full">
              <div v-for="(_, idx) in post.images" :key="idx" class="w-1 h-1 rounded-full bg-white/80"></div>
            </div>
            <div class="absolute top-2 right-2 bg-black/20 backdrop-blur px-2 py-0.5 rounded-full text-[10px] font-bold text-white">{{ post.category }}</div>
          </div>

          <!-- 内容 -->
          <div class="p-4 flex-1 flex flex-col">
            <!-- 标题和头像 -->
            <div class="flex items-start justify-between gap-3 mb-2">
               <h3 class="font-bold text-zinc-800 text-sm md:text-base line-clamp-2 leading-tight flex-1">
                 <span v-if="post.tags?.includes('官方')" class="inline-block bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded mr-1 align-middle">官方</span>
                 {{ post.title }}
               </h3>
               <div class="relative shrink-0" @click.stop="router.push(`/user/${post.author?.id}`)">
                  <img :src="post.author?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-8 h-8 rounded-full border-2 border-white shadow-sm object-cover hover:scale-110 transition-transform" />
               </div>
            </div>

            <!-- 标签 -->
            <div class="flex flex-wrap gap-1.5 mb-3">
              <span v-for="tag in post.tags" :key="tag" class="text-[10px] font-bold text-rose-500 bg-rose-50 px-2 py-0.5 rounded-full border border-rose-100">#{{ tag }}</span>
            </div>
            
            <p class="text-zinc-400 text-xs line-clamp-2 mb-3 leading-relaxed">{{ (post.content || '').substring(0, 30).replace(/<[^>]*>?/gm, '') + '...' }}</p>

            <!-- 互动和时间 -->
            <div class="mt-auto flex items-center justify-between pt-3 border-t border-zinc-50">
               <span class="text-[10px] text-zinc-300 font-bold">{{ formatTime(post.createdAt) }}</span>
               
               <div class="flex items-center gap-3">
                 <button class="flex items-center gap-1 group/btn" @click.stop="handleLike(post)">
                   <Heart class="w-4 h-4 text-zinc-300 group-hover/btn:text-[#FF6B6B] transition-colors" :class="{'fill-[#FF6B6B] text-[#FF6B6B]': post.isLiked}" />
                   <span class="text-xs text-zinc-400 font-bold group-hover/btn:text-[#FF6B6B]">{{ post.likes || 0 }}</span>
                 </button>
                 <button class="flex items-center gap-1 group/btn" @click.stop="handleFavorite(post)">
                   <Star class="w-4 h-4 text-zinc-300 group-hover/btn:text-amber-400 transition-colors" :class="{'fill-amber-400 text-amber-400': post.isFavorited}" />
                   <span class="text-xs text-zinc-400 font-bold group-hover/btn:text-amber-400">{{ post.favorites || 0 }}</span>
                 </button>
                 <button class="flex items-center gap-1 group/btn" @click.stop>
                   <MessageCircle class="w-4 h-4 text-zinc-300 group-hover/btn:text-blue-400 transition-colors" />
                   <span class="text-xs text-zinc-400 font-bold group-hover/btn:text-blue-400">{{ post.commentsCount || 0 }}</span>
                 </button>
               </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="flex justify-center mt-12 mb-8" v-if="!loading && displayedPosts.length > 0">
        <button class="px-8 py-2 border border-zinc-200 text-[#FF6B6B] rounded-full text-sm font-bold hover:bg-[#FF6B6B] hover:text-white transition-colors">
          加载更多
        </button>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="bg-white border-t border-zinc-100 pt-10 pb-6">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-3 gap-8 text-center mb-8">
          <div class="space-y-2">
            <h4 class="font-bold text-zinc-900 text-sm">关于我们</h4>
            <ul class="space-y-1 text-xs text-zinc-500">
              <li><a href="#" class="hover:text-[#FF6B6B]">关于小博客</a></li>
              <li><a href="#" class="hover:text-[#FF6B6B]">隐私政策</a></li>
              <li><a href="#" class="hover:text-[#FF6B6B]">使用条款</a></li>
            </ul>
          </div>
          <div class="space-y-2">
            <h4 class="font-bold text-zinc-900 text-sm">帮助中心</h4>
            <ul class="space-y-1 text-xs text-zinc-500">
              <li><a href="#" class="hover:text-[#FF6B6B]">常见问题</a></li>
              <li><a href="#" class="hover:text-[#FF6B6B]">联系我们</a></li>
              <li><a href="#" class="hover:text-[#FF6B6B]">社区规范</a></li>
            </ul>
          </div>
          <div class="space-y-2">
            <h4 class="font-bold text-zinc-900 text-sm">关注我们</h4>
            <ul class="space-y-1 text-xs text-zinc-500">
              <li><a href="#" class="hover:text-[#FF6B6B]">新浪微博</a></li>
              <li><a href="#" class="hover:text-[#FF6B6B]">微信公众号</a></li>
              <li><a href="#" class="hover:text-[#FF6B6B]">加入我们</a></li>
            </ul>
          </div>
        </div>
        <div class="text-center">
          <div class="flex justify-center mb-2">
             <Heart class="w-4 h-4 text-[#FF6B6B] fill-current animate-pulse" />
          </div>
          <p class="text-[10px] text-zinc-400">Copyright © 2024 小博客 All rights reserved</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* Hide scrollbar for Chrome, Safari and Opera */
.no-scrollbar::-webkit-scrollbar {
    display: none;
}
/* Hide scrollbar for IE, Edge and Firefox */
.no-scrollbar {
    -ms-overflow-style: none;  /* IE and Edge */
    scrollbar-width: none;  /* Firefox */
}
</style>