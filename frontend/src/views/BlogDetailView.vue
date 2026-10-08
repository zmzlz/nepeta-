<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { Heart, MessageCircle, ArrowLeft, Send, Share2, Clock, Smile, Image as ImageIcon, X, Star, MoreHorizontal, ThumbsUp, Trash2, ChevronLeft, ChevronRight, Edit } from 'lucide-vue-next'
import type { BlogPost, BlogComment } from '@/types'
import axios from 'axios'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const toast = useToast()

const showMenu = ref(false)

const handleDeletePost = async () => {
  if (!post.value) return
  if (!confirm('确定要删除这篇帖子吗？')) return
  
  try {
    await blogApi.deletePost(post.value.id)
    toast.success('删除成功')
    router.replace('/blog')
  } catch (error) {
    console.error('Delete failed:', error)
    toast.error('删除失败')
  }
}

const sharePost = () => {
  navigator.clipboard.writeText(window.location.href)
  toast.success('链接已复制')
  showMenu.value = false
}


const post = ref<BlogPost | null>(null)
const comments = ref<BlogComment[]>([])
const newComment = ref('')
const loading = ref(true)
const submitting = ref(false)
const showEmojiPicker = ref(false)
const newCommentImage = ref('')
const isUploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const isProcessing = ref(false)

// UI 状态
const expandedComments = ref<Set<number>>(new Set())
const currentImageIndex = ref(0)
const slideDirection = ref('slide-right')

const postImages = computed(() => {
  if (!post.value) return []
  return post.value.images && post.value.images.length > 0 ? post.value.images : (post.value.coverImage ? [post.value.coverImage] : [])
})

const nextImage = () => {
  slideDirection.value = 'slide-right'
  if (currentImageIndex.value < postImages.value.length - 1) {
    currentImageIndex.value++
  } else {
    currentImageIndex.value = 0
  }
}

const prevImage = () => {
  slideDirection.value = 'slide-left'
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  } else {
    currentImageIndex.value = postImages.value.length - 1
  }
}

const toggleExpand = (id: number) => {
  if (expandedComments.value.has(id)) {
    expandedComments.value.delete(id)
  } else {
    expandedComments.value.add(id)
  }
}

// 回复状态
const replyToComment = ref<BlogComment | null>(null)

const openPreview = (url: string) => {
  if (!url) return
  previewImageUrl.value = url
  showImagePreview.value = true
}

const emojis = ['😀', '😂', '😍', '🥰', '😎', '😭', '😡', '👍', '👎', '🙏', '🔥', '❤️', '🍵', '🥮', '🌸', '😋']

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
    const res = await axios.post('http://localhost:8080/api/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    newCommentImage.value = res.data
    toast.success('图片上传成功')
  } catch (error) {
    console.error('Upload failed:', error)
    toast.error('上传失败')
  } finally {
    isUploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

const fetchPost = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    if (!id) return
    const postRes = (await blogApi.getPostById(id, userStore.user?.id)) as unknown as BlogPost
    post.value = postRes

    await fetchComments()
  } catch (error) {
    console.error('Failed to fetch post:', error)
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  if (!post.value) return
  const commentsRes = await blogApi.getComments(post.value.id)
  comments.value = Array.isArray(commentsRes) ? commentsRes : []
}

const commentTree = computed(() => {
  const map = new Map<number, BlogComment>()
  const roots: BlogComment[] = []
  
  // 深拷贝以避免在严格模式下重复修改原始列表
  const list = JSON.parse(JSON.stringify(comments.value)) as BlogComment[]

  // 1. Index all comments
  list.forEach(c => {
    c.replies = []
    map.set(c.id, c)
  })

  // 2. Distribute
  list.forEach(c => {
    // 检查它是否是根评论（没有父级，或 parentId 为空）
    // 注意：后端可能发送 parent 对象。如果 parent 对象存在但为空，则是根评论
    // 根据之前的代码，我们检查 c.parent 或 c.parentId
    // 之前的代码使用了 c.parentId。我们坚持检查 parent 的存在性
    // 然而，如果我们展平了所有内容，我们需要为每个评论找到最终的根
    
    if (!c.parent && !c.parentId) {
      roots.push(c)
    } else {
      // 这是一个回复。找到它的最终根
      let current = c
      let root: BlogComment | undefined = undefined
      let depth = 0
      
      // 我们需要使用映射向上查找，因为 c.parent 可能只是 {id: ...}
      // 如果存在，parentId 属性是可靠的
      
      let pId = c.parent?.id || c.parentId
      
      while (pId && map.has(pId) && depth < 20) {
        const p = map.get(pId)!
        if (!p.parent && !p.parentId) {
           root = p
           break
        }
        pId = p.parent?.id || p.parentId
        depth++
      }
      
      if (root) {
        root.replies!.push(c)
      } else {
        // 后备：如果找不到根（可能部分加载？），视为根或孤儿
        // 目前，如果在列表中找不到父级，就推送到 roots， 
        // 基本上在这个视图上下文中将其视为顶级评论
        // 但通常如果我们获取所有评论，这不应该发生
        // roots.push(c) 
      }
    }
  })
  
  // 3. Sort roots and their replies
  roots.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  
  roots.forEach(r => {
     if (r.replies) {
        r.replies.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
     }
  })
  
  return roots
})

const handleLikePost = async () => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if (!post.value || isProcessing.value) return
  
  isProcessing.value = true
  // 乐观 UI 更新
  const originalState = { isLiked: post.value.isLiked, likes: post.value.likes }
  
  if (post.value.isLiked) {
    post.value.likes = Math.max(0, (post.value.likes || 0) - 1)
    post.value.isLiked = false
  } else {
    post.value.likes = (post.value.likes || 0) + 1
    post.value.isLiked = true
  }

  try {
    await blogApi.likePost(post.value.id, userStore.user.id)
  } catch (error) {
    console.error('Failed to like:', error)
    // 如果失败则恢复状态
    post.value.isLiked = originalState.isLiked
    post.value.likes = originalState.likes
    toast.error('操作失败')
  } finally {
    setTimeout(() => {
        isProcessing.value = false
    }, 500) // 小延迟防止快速点击
  }
}

const handleFavoritePost = async () => {
   if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if (!post.value || isProcessing.value) return
  
  isProcessing.value = true
  // 乐观 UI 更新
  const originalState = { isFavorited: post.value.isFavorited, favorites: post.value.favorites }

  if (post.value.isFavorited) {
    post.value.favorites = Math.max(0, (post.value.favorites || 0) - 1)
    post.value.isFavorited = false
    toast.success('已取消收藏')
  } else {
    post.value.favorites = (post.value.favorites || 0) + 1
    post.value.isFavorited = true
    toast.success('已收藏')
  }

  try {
    await blogApi.favoritePost(post.value.id, userStore.user.id)
  } catch (error) {
    console.error('Failed to favorite:', error)
    // 恢复
    post.value.isFavorited = originalState.isFavorited
    post.value.favorites = originalState.favorites
    toast.error('操作失败')
  } finally {
    setTimeout(() => {
        isProcessing.value = false
    }, 500)
  }
}

const handleLikeComment = async (comment: BlogComment) => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  try {
    await axios.post(`http://localhost:8080/api/blog/comments/${comment.id}/like`) // Use axios directly if api method missing
    comment.likes = (comment.likes || 0) + 1
    comment.isLiked = true
  } catch (error) {
    console.error('Failed to like comment', error)
  }
}

const addEmoji = (emoji: string) => {
  newComment.value += emoji
  showEmojiPicker.value = false
}

const setReplyTo = (comment: BlogComment) => {
  replyToComment.value = comment
  // 滚动到输入框或聚焦
  const textarea = document.querySelector('textarea')
  if (textarea) textarea.focus()
}

const cancelReply = () => {
  replyToComment.value = null
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    return
  }
  if ((!newComment.value.trim() && !newCommentImage.value) || !post.value) return

  submitting.value = true
  try {
    const commentData: any = {
      content: newComment.value,
      imageUrl: newCommentImage.value,
      author: { id: userStore.user.id }
    }
    
    if (replyToComment.value) {
      commentData.parent = { id: replyToComment.value.id }
    }

    await blogApi.addComment(post.value.id, commentData)
    newComment.value = ''
    newCommentImage.value = ''
    replyToComment.value = null
    
    await fetchComments()
    toast.success('评论成功')
  } catch (error) {
    console.error('Failed to comment:', error)
    toast.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchPost()
})
</script>

<template>
  <div class="bg-[#FDFBF7] min-h-screen pb-24 font-sans">
    <!-- 头部 -->
    <div class="sticky top-0 bg-white/80 backdrop-blur-md z-30 border-b border-zinc-100 px-4 py-3 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <button @click="router.back()" class="p-2 hover:bg-zinc-100 rounded-full transition-colors text-zinc-600">
          <ArrowLeft class="w-6 h-6" />
        </button>
        <button @click="router.push('/blog')" class="text-sm font-bold text-zinc-500 hover:text-[#FF6B6B] transition-colors">
          博客广场
        </button>
      </div>
      <div class="font-bold text-zinc-800 text-sm truncate max-w-[200px]" v-if="post">
        {{ post.title }}
      </div>
      <div class="relative">
        <button @click="showMenu = !showMenu" class="p-2 hover:bg-zinc-100 rounded-full transition-colors text-zinc-600">
          <MoreHorizontal class="w-6 h-6" />
        </button>
        <div v-if="showMenu" class="absolute top-full right-0 mt-2 bg-white rounded-xl shadow-xl border border-zinc-100 w-32 py-2 z-50 overflow-hidden">
           <button 
             v-if="post && (userStore.user?.role === 'ADMIN' || userStore.user?.id === post.author?.id)"
             @click="router.push(`/blog/create?id=${post.id}`)"
             class="w-full text-left px-4 py-2 text-sm font-bold text-zinc-500 hover:bg-zinc-50 transition-colors flex items-center gap-2"
           >
             <Edit class="w-4 h-4" /> 编辑帖子
           </button>
           <button 
             v-if="userStore.user?.role === 'ADMIN' || userStore.user?.id === post?.author?.id"
             @click="handleDeletePost"
             class="w-full text-left px-4 py-2 text-sm font-bold text-red-500 hover:bg-red-50 transition-colors flex items-center gap-2"
           >
             <Trash2 class="w-4 h-4" /> 删除帖子
           </button>
           <button 
             class="w-full text-left px-4 py-2 text-sm font-bold text-zinc-500 hover:bg-zinc-50 transition-colors flex items-center gap-2"
             @click="sharePost"
           >
             <Share2 class="w-4 h-4" /> 分享
           </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-10 w-10 border-4 border-zinc-100 border-t-[#FF6B6B]"></div>
    </div>

    <div v-else-if="!post" class="flex justify-center items-center h-64 text-zinc-400 font-bold">
      文章不存在或已被删除
    </div>

    <div v-else class="max-w-3xl mx-auto">
      
      <!-- 图片轮播 -->
      <div class="w-full bg-zinc-100 relative group/carousel aspect-[4/3] md:aspect-video overflow-hidden bg-black/5">
        
        <!-- 当前图片 -->
        <div class="w-full h-full relative overflow-hidden" v-if="postImages.length > 0">
            <transition :name="slideDirection">
                <div 
                    :key="currentImageIndex"
                    class="absolute inset-0 w-full h-full flex items-center justify-center bg-zinc-100"
                >
                    <img 
                        :src="postImages[currentImageIndex]" 
                        class="w-full h-full object-contain backdrop-blur-3xl cursor-pointer" 
                        @click="openPreview(postImages[currentImageIndex])" 
                    />
                </div>
            </transition>
        </div>

        <!-- 导航按钮 -->
        <template v-if="postImages.length > 1">
            <button 
                @click.stop="prevImage"
                class="absolute left-4 top-1/2 -translate-y-1/2 p-2 rounded-full bg-black/20 hover:bg-black/40 text-white/90 backdrop-blur-sm transition-all opacity-0 group-hover/carousel:opacity-100"
            >
                <ChevronLeft class="w-6 h-6" />
            </button>
            <button 
                @click.stop="nextImage"
                class="absolute right-4 top-1/2 -translate-y-1/2 p-2 rounded-full bg-black/20 hover:bg-black/40 text-white/90 backdrop-blur-sm transition-all opacity-0 group-hover/carousel:opacity-100"
            >
                <ChevronRight class="w-6 h-6" />
            </button>
        </template>

        <!-- 指示器 -->
        <div v-if="postImages.length > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-1.5 bg-black/20 backdrop-blur px-2 py-1.5 rounded-full z-10">
            <div 
                v-for="(_, idx) in postImages" 
                :key="idx" 
                class="w-1.5 h-1.5 rounded-full transition-all shadow-sm"
                :class="currentImageIndex === idx ? 'bg-white w-3' : 'bg-white/50 hover:bg-white/80'"
            ></div>
        </div>
      </div>

      <!-- 图片预览弹窗 -->
      <transition
        enter-active-class="transition duration-300 ease-out"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition duration-200 ease-in"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div v-if="showImagePreview" class="fixed inset-0 z-[100] bg-black/95 backdrop-blur-xl flex items-center justify-center p-4" @click="showImagePreview = false">
           <div class="relative max-w-7xl max-h-[90vh] w-full h-full flex items-center justify-center">
             <img :src="previewImageUrl" class="max-w-full max-h-full object-contain rounded-lg shadow-2xl" @click.stop />
             <button class="absolute top-4 right-4 text-white/70 hover:text-white p-2 rounded-full hover:bg-white/10 transition-colors z-10" @click="showImagePreview = false">
                <X class="w-8 h-8" />
             </button>
           </div>
        </div>
      </transition>

      <!-- 内容 -->
      <div class="px-6 py-8 bg-white rounded-b-3xl shadow-sm mb-6">
        <div class="flex items-start justify-between gap-4 mb-4">
           <h1 class="text-2xl font-black text-zinc-900 leading-tight tracking-tight flex-1">{{ post.title }}</h1>
           <div class="flex flex-col items-center gap-1 shrink-0 cursor-pointer" @click="router.push(`/user/${post.author?.id}`)">
              <img :src="post.author?.avatar" class="w-10 h-10 rounded-full object-cover border border-zinc-100 shadow-sm" />
              <span class="text-[10px] font-bold text-zinc-500 max-w-[60px] truncate">{{ post.author?.nickname }}</span>
           </div>
        </div>

        <div class="flex items-center gap-4 text-xs font-bold text-zinc-400 mb-6">
           <span class="bg-[#FF6B6B]/10 text-[#FF6B6B] px-3 py-1 rounded-full">{{ post.category }}</span>
           <span class="flex items-center gap-1"><Clock class="w-3.5 h-3.5" /> {{ formatDate(post.createdAt) }}</span>
           <span class="flex items-center gap-1"><MessageCircle class="w-3.5 h-3.5" /> {{ post.views }} 阅读</span>
        </div>
        
        <p class="text-zinc-700 leading-relaxed whitespace-pre-wrap text-base font-medium mb-8">
          {{ post.content }}
        </p>

        <div class="flex items-center gap-2 mb-8 flex-wrap">
           <div v-for="tag in post.tags" :key="tag" class="text-[#FF6B6B] font-bold text-sm bg-rose-50 px-3 py-1 rounded-full">
             # {{ tag }}
           </div>
        </div>

        <div class="border-t border-zinc-100 pt-6 flex justify-between items-center">
           <div class="text-xs font-bold text-zinc-400">最后编辑于 {{ formatDate(post.createdAt) }}</div>
           <div class="flex gap-4">
              <button @click="handleLikePost" class="flex items-center gap-1.5 px-4 py-2 rounded-full bg-rose-50 hover:bg-rose-100 transition-colors group">
                 <Heart class="w-5 h-5 text-rose-500 transition-transform group-hover:scale-110" :class="{'fill-rose-500': post.isLiked}" />
                 <span class="text-rose-600 font-bold text-sm">{{ post.likes }}</span>
              </button>
              <button @click="handleFavoritePost" class="flex items-center gap-1.5 px-4 py-2 rounded-full bg-amber-50 hover:bg-amber-100 transition-colors group">
                 <Star class="w-5 h-5 text-amber-500 transition-transform group-hover:scale-110" :class="{'fill-amber-500': post.isFavorited}" />
                 <span class="text-amber-600 font-bold text-sm">{{ post.favorites || 0 }}</span>
              </button>
           </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="px-6 pb-24 max-w-2xl mx-auto">
        <h3 class="font-black text-lg mb-8 text-zinc-800 flex items-center gap-2">
           评论 <span class="text-zinc-400 text-sm font-bold">{{ comments.length }}</span>
        </h3>
        
        <div class="space-y-10">
          <div v-for="comment in commentTree" :key="comment.id" class="flex gap-4 group animate-in fade-in slide-in-from-bottom-2">
             <div class="w-10 h-10 rounded-full bg-zinc-100 flex-shrink-0 overflow-hidden border border-zinc-50 cursor-pointer hover:scale-105 transition-transform" @click="router.push(`/user/${comment.author.id}`)">
                <img :src="comment.author?.avatar" class="w-full h-full object-cover" />
             </div>
             <div class="flex-1">
               <div class="mb-2">
                   <div class="flex items-center gap-2 mb-1">
                     <span class="font-bold text-zinc-900 text-sm hover:text-[#FF6B6B] cursor-pointer" @click="router.push(`/user/${comment.author.id}`)">{{ comment.author?.nickname }}</span>
                   </div>
                   <p class="text-zinc-800 text-sm leading-relaxed whitespace-pre-wrap font-medium">{{ comment.content }}</p>
                   <div v-if="comment.imageUrl" class="mt-3">
                     <img :src="comment.imageUrl" @click="openPreview(comment.imageUrl)" class="max-w-[200px] rounded-xl border border-zinc-100 cursor-zoom-in hover:brightness-90 transition-all" />
                   </div>
               </div>

               <!-- 评论元数据与操作 -->
               <div class="flex items-center gap-6 text-xs font-bold text-zinc-400 mb-4">
                  <span>{{ formatDate(comment.createdAt) }}</span>
                  <button @click="handleLikeComment(comment)" class="flex items-center gap-1.5 hover:text-rose-500 transition-colors group">
                     <Heart class="w-3.5 h-3.5 group-hover:scale-110 transition-transform" :class="{'text-rose-500 fill-rose-500': comment.isLiked}" />
                     {{ comment.likes || '赞' }}
                  </button>
                  <button @click="setReplyTo(comment)" class="flex items-center gap-1.5 hover:text-blue-500 transition-colors group">
                     <MessageCircle class="w-3.5 h-3.5 group-hover:scale-110 transition-transform" />
                     {{ comment.replies?.length || '' }} 回复
                  </button>
               </div>

               <!-- 嵌套回复 -->
               <div v-if="comment.replies && comment.replies.length > 0" class="space-y-6 mt-4">
                  <!-- 仅在展开或回复较少时显示 -->
                  <template v-if="expandedComments.has(comment.id) || comment.replies.length <= 1">
                    <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-3 animate-in fade-in slide-in-from-left-2">
                       <div class="w-7 h-7 rounded-full bg-zinc-100 flex-shrink-0 overflow-hidden border border-zinc-50 cursor-pointer" @click="router.push(`/user/${reply.author.id}`)">
                          <img :src="reply.author?.avatar" class="w-full h-full object-cover" />
                       </div>
                       <div class="flex-1">
                          <div class="mb-1">
                            <span class="font-bold text-zinc-800 text-[13px] hover:text-[#FF6B6B] cursor-pointer" @click="router.push(`/user/${reply.author.id}`)">{{ reply.author?.nickname }}</span>
                            <span v-if="reply.parent && reply.parent.id !== comment.id" class="text-zinc-500 text-xs mx-1">回复 <span class="text-blue-500 font-bold">@{{ reply.parent.author?.nickname }}</span></span>
                            <p class="text-zinc-800 text-[13px] leading-relaxed font-medium mt-0.5">{{ reply.content }}</p>
                          </div>
                          <div class="flex items-center gap-5 text-[11px] font-bold text-zinc-400">
                              <span>{{ formatDate(reply.createdAt) }}</span>
                              <button @click="handleLikeComment(reply)" class="flex items-center gap-1.5 hover:text-rose-500 transition-colors group">
                                  <Heart class="w-3 h-3 group-hover:scale-110 transition-transform" :class="{'text-rose-600 fill-rose-600': reply.isLiked}" />
                                  {{ reply.likes || '赞' }}
                              </button>
                              <button @click="setReplyTo(comment)" class="hover:text-blue-500 transition-colors">回复</button>
                          </div>
                       </div>
                    </div>
                  </template>

                  <!-- 展开/折叠链接 -->
                  <button 
                    v-if="comment.replies.length > 1" 
                    @click="toggleExpand(comment.id)"
                    class="text-xs font-black text-blue-500 hover:text-blue-600 flex items-center gap-1 mt-2 pl-10"
                  >
                    <span v-if="!expandedComments.has(comment.id)">展开 {{ comment.replies.length }} 条回复</span>
                    <span v-else>收起回复</span>
                  </button>
               </div>
             </div>
          </div>
          
          <div v-if="comments.length === 0" class="text-center py-20 bg-zinc-50/50 rounded-3xl border-2 border-zinc-100 border-dashed">
             <MessageCircle class="w-12 h-12 text-zinc-200 mx-auto mb-3" />
             <p class="text-zinc-400 font-bold text-sm">暂无评论，快来抢沙发吧~</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部输入栏 -->
    <div class="fixed bottom-0 left-0 right-0 bg-white/90 backdrop-blur-md border-t border-zinc-100 px-4 py-3 z-40 shadow-[0_-4px_20px_rgba(0,0,0,0.05)]">
      <div class="max-w-3xl mx-auto flex items-end gap-3">
        
        <div class="flex-1 bg-zinc-100 rounded-3xl p-1 relative transition-colors focus-within:bg-white focus-within:ring-2 focus-within:ring-[#FF6B6B] focus-within:ring-opacity-50">
           <div v-if="replyToComment" class="flex items-center gap-2 px-3 py-1.5 bg-zinc-200/50 rounded-full w-fit mb-1 mx-1">
              <span class="text-xs font-bold text-zinc-500">回复 @{{ replyToComment.author.nickname }}</span>
              <button @click="cancelReply" class="w-4 h-4 rounded-full bg-zinc-300 text-white flex items-center justify-center hover:bg-zinc-400"><X class="w-3 h-3"/></button>
           </div>
           
           <textarea 
              v-model="newComment"
              :placeholder="replyToComment ? '回复评论...' : '说点什么吧...'" 
              class="w-full bg-transparent border-none outline-none focus:ring-0 p-3 text-sm font-medium resize-none max-h-32 min-h-[44px]"
              rows="1"
           ></textarea>

           <!-- 输入框中的图片预览 -->
           <div v-if="newCommentImage" class="px-3 pb-2">
              <div class="relative w-fit">
                 <img :src="newCommentImage" class="h-16 rounded-lg border border-zinc-200" />
                 <button @click="newCommentImage = ''" class="absolute -top-1.5 -right-1.5 w-5 h-5 bg-zinc-800 text-white rounded-full flex items-center justify-center border-2 border-white"><X class="w-3 h-3"/></button>
              </div>
           </div>
        </div>

        <div class="flex items-center gap-2 pb-1">
           <button @click="showEmojiPicker = !showEmojiPicker" class="p-2.5 hover:bg-zinc-100 rounded-full text-zinc-400 hover:text-[#FF6B6B] transition-colors relative">
             <Smile class="w-6 h-6" />
             <!-- 表情选择器 -->
             <div v-if="showEmojiPicker" class="absolute bottom-full right-0 mb-4 bg-white shadow-xl rounded-2xl p-3 w-72 grid grid-cols-8 gap-1 border border-zinc-100 z-50">
                <button v-for="emoji in emojis" :key="emoji" @click.stop="addEmoji(emoji)" class="text-xl hover:bg-zinc-50 rounded p-1 transition-colors">{{ emoji }}</button>
             </div>
           </button>
           
           <button @click="triggerUpload" class="p-2.5 hover:bg-zinc-100 rounded-full text-zinc-400 hover:text-[#FF6B6B] transition-colors relative">
             <ImageIcon class="w-6 h-6" />
             <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleFileChange" />
             <div v-if="isUploading" class="absolute top-1 right-1 w-2.5 h-2.5 bg-[#FF6B6B] rounded-full animate-ping"></div>
           </button>

           <button 
             @click="submitComment"
             :disabled="submitting || (!newComment.trim() && !newCommentImage)"
             class="p-2.5 bg-[#FF6B6B] text-white rounded-full disabled:opacity-50 disabled:bg-zinc-300 hover:bg-rose-600 hover:scale-105 transition-all shadow-lg shadow-rose-200"
           >
             <Send class="w-5 h-5 ml-0.5" />
           </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slide-right-enter-active,
.slide-right-leave-active,
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1), opacity 0.5s cubic-bezier(0.25, 1, 0.5, 1);
  position: absolute;
  width: 100%;
  height: 100%;
  will-change: transform;
}

.slide-right-enter-from {
  transform: translateX(100%);
  opacity: 0.5;
}
.slide-right-leave-to {
  transform: translateX(-100%);
  opacity: 0.5;
}

.slide-left-enter-from {
  transform: translateX(-100%);
  opacity: 0.5;
}
.slide-left-leave-to {
  transform: translateX(100%);
  opacity: 0.5;
}
</style>