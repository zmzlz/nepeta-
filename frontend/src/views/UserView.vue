<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MapPin, Heart, MessageCircle, ArrowLeft, UserPlus, UserCheck } from 'lucide-vue-next'
import { blogApi, userApi } from '@/api'
import type { BlogPost, User } from '@/types'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = Number(route.params.id)
const user = ref<User | null>(null)
const loading = ref(false)
const isFollowing = ref(false) // 模拟关注状态

// 博客模式标签
const activeBlogTab = ref('posts') // 'posts', 'liked', 'favorited'

// 数据
const userPosts = ref<BlogPost[]>([])
const likedPosts = ref<BlogPost[]>([])
const favoritedPosts = ref<BlogPost[]>([])

const fetchUser = async () => {
  loading.value = true
  try {
    const res: any = await userApi.getUserById(userId, userStore.user?.id)
    user.value = res
    isFollowing.value = res.following || false
  } catch (error) {
    console.error('Failed to fetch user:', error)
  } finally {
    loading.value = false
  }
}

const loadPosts = async () => {
  try {
      // 用户的帖子
      const allPosts: BlogPost[] = await blogApi.getPosts(undefined, userStore.user?.id) as any
      if (Array.isArray(allPosts)) {
          userPosts.value = allPosts.filter(p => p.author.id === userId)
      }
      
      // 喜欢的帖子
      const liked: BlogPost[] = await blogApi.getLikedPosts(userId) as any
      likedPosts.value = Array.isArray(liked) ? liked : []

      // 收藏的帖子
      const favorited: BlogPost[] = await blogApi.getFavoritedPosts(userId) as any
      favoritedPosts.value = Array.isArray(favorited) ? favorited : []
      
  } catch (e) {
      console.error("Failed to load posts", e)
  }
}

const toggleFollow = async () => {
    if (!userStore.isLoggedIn) {
        toast.warning('请先登录')
        return
    }
    
    try {
        await userApi.followUser(userId, userStore.user.id)
        isFollowing.value = !isFollowing.value
        // 本地更新计数
        if (user.value) {
            user.value.followersCount = (user.value.followersCount || 0) + (isFollowing.value ? 1 : -1)
        }
        toast.success(isFollowing.value ? '关注成功' : '已取消关注')
    } catch (error) {
        console.error('Follow failed', error)
        toast.error('操作失败')
    }
}

const formatTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
    if (isNaN(userId)) {
        router.push('/')
        return
    }
    if (userStore.user?.id === userId) {
        router.push('/profile')
        return
    }
    fetchUser()
    loadPosts()
})
</script>

<template>
  <div class="min-h-screen bg-[#FDFBF7] py-12">
    <div class="container mx-auto px-4 md:px-8">
      
      <button @click="router.back()" class="mb-6 flex items-center gap-2 text-zinc-500 hover:text-[#FF6B6B] font-bold transition-colors">
          <ArrowLeft class="w-5 h-5" /> 返回
      </button>

      <div v-if="loading" class="flex justify-center py-20">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-[#FF6B6B]"></div>
      </div>

      <div v-else-if="user" class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- 侧边栏 -->
        <div class="md:col-span-1">
          <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 sticky top-24">
            <div class="flex flex-col items-center mb-6">
              <img :src="user.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20avatar%20small&image_size=square'" class="w-24 h-24 rounded-full border-4 border-rose-100 object-cover mb-4" />
              <h2 class="text-xl font-black text-zinc-800">{{ user.nickname }}</h2>
              <p class="text-xs text-zinc-400 mt-1">@{{ user.username }}</p>
              
              <button 
                @click="toggleFollow"
                class="mt-4 px-8 py-2 rounded-full font-bold text-sm flex items-center gap-2 transition-all"
                :class="isFollowing ? 'bg-zinc-100 text-zinc-500' : 'bg-[#FF6B6B] text-white shadow-lg shadow-rose-200 hover:bg-rose-600'"
              >
                 <component :is="isFollowing ? UserCheck : UserPlus" class="w-4 h-4" />
                 {{ isFollowing ? '已关注' : '关注' }}
              </button>
            </div>

            <div class="text-center mb-6 px-2">
                 <p class="text-sm text-zinc-600 font-medium italic">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
            </div>
               
            <div class="flex justify-center gap-3 mb-6 flex-wrap">
                 <span v-if="user.age" class="px-2 py-1 bg-blue-50 text-blue-500 text-xs font-bold rounded-lg">{{ user.age }}岁</span>
                 <span v-if="user.location" class="px-2 py-1 bg-green-50 text-green-600 text-xs font-bold rounded-lg flex items-center gap-1"><MapPin class="w-3 h-3"/> {{ user.location }}</span>
            </div>

            <div class="grid grid-cols-3 gap-2 mb-6 text-center">
                 <div class="bg-zinc-50 rounded-xl p-2">
                   <div class="text-lg font-black text-zinc-800">{{ user.followersCount || 0 }}</div>
                   <div class="text-[10px] text-zinc-400 font-bold">粉丝</div>
                 </div>
                 <div class="bg-zinc-50 rounded-xl p-2">
                   <div class="text-lg font-black text-zinc-800">{{ user.followingCount || 0 }}</div>
                   <div class="text-[10px] text-zinc-400 font-bold">关注</div>
                 </div>
                 <div class="bg-zinc-50 rounded-xl p-2">
                   <div class="text-lg font-black text-zinc-800">{{ user.totalLikesReceived || 0 }}</div>
                   <div class="text-[10px] text-zinc-400 font-bold">获赞</div>
                 </div>
            </div>
          </div>
        </div>

        <!-- 主要内容 -->
        <div class="md:col-span-3">
             <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 min-h-[500px]">
               <!-- 标签页 -->
               <div class="flex border-b border-zinc-100 mb-6">
                 <button 
                   @click="activeBlogTab = 'posts'"
                   :class="['px-6 py-3 font-bold text-sm transition-all border-b-2', activeBlogTab === 'posts' ? 'border-[#FF6B6B] text-[#FF6B6B]' : 'border-transparent text-zinc-400 hover:text-zinc-600']"
                 >
                   Ta的帖子
                 </button>
                 <button 
                   @click="activeBlogTab = 'liked'"
                   :class="['px-6 py-3 font-bold text-sm transition-all border-b-2', activeBlogTab === 'liked' ? 'border-[#FF6B6B] text-[#FF6B6B]' : 'border-transparent text-zinc-400 hover:text-zinc-600']"
                 >
                   Ta赞过的
                 </button>
                 <button 
                   @click="activeBlogTab = 'favorited'"
                   :class="['px-6 py-3 font-bold text-sm transition-all border-b-2', activeBlogTab === 'favorited' ? 'border-[#FF6B6B] text-[#FF6B6B]' : 'border-transparent text-zinc-400 hover:text-zinc-600']"
                 >
                   Ta收藏的
                 </button>
               </div>

               <!-- 帖子网格 -->
               <div v-if="activeBlogTab === 'posts' && userPosts.length === 0" class="text-center py-20 bg-zinc-50 rounded-2xl border border-zinc-100 border-dashed">
                 <p class="text-zinc-400 font-medium">暂无发布记录</p>
               </div>

               <div v-else class="grid grid-cols-2 lg:grid-cols-3 gap-4">
                 <div 
                    v-for="post in (activeBlogTab === 'posts' ? userPosts : activeBlogTab === 'liked' ? likedPosts : favoritedPosts)" 
                    :key="post.id"
                    @click="router.push(`/blog/${post.id}`)"
                    class="bg-white border border-zinc-100 rounded-xl overflow-hidden cursor-pointer hover:shadow-md transition-all group"
                 >
                    <div class="aspect-[4/3] bg-zinc-50 overflow-hidden relative">
                      <img :src="post.coverImage" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                      <div class="absolute bottom-2 left-2 bg-white/90 backdrop-blur px-2 py-0.5 rounded text-[10px] font-bold text-zinc-800">{{ post.category }}</div>
                    </div>
                    <div class="p-3">
                       <h4 class="font-bold text-zinc-800 text-sm line-clamp-1 mb-2">{{ post.title }}</h4>
                       <div class="flex items-center justify-between text-zinc-400 text-xs">
                          <span>{{ formatTime(post.createdAt) }}</span>
                          <div class="flex items-center gap-3">
                             <div class="flex items-center gap-0.5"><Heart class="w-3 h-3" /> {{ post.likes }}</div>
                             <div class="flex items-center gap-0.5"><MessageCircle class="w-3 h-3" /> {{ post.views }}</div>
                          </div>
                       </div>
                    </div>
                 </div>
               </div>

             </div>
        </div>
      </div>
      
      <div v-else class="text-center py-20">
          <p class="text-zinc-400">用户不存在</p>
      </div>
    </div>
  </div>
</template>
