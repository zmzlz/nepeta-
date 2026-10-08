<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { blogApi } from '@/api/index'
import { useUserStore } from '@/stores/user'
import { UploadCloud, X, Plus, Image as ImageIcon } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const toast = useToast()

const form = ref({
  title: '',
  content: '',
  category: '生活',
  images: [] as string[],
  tags: [] as string[]
})

const categories = ['茶饮', '烘焙', '花草', '家居', '穿搭', '生活', '美食', '手作']
const beautifulWords = ['美好生活', '治愈瞬间', '人间烟火', '今日份甜', '温柔时光', '快乐碎片', '小确幸', '日落黄昏']
const loading = ref(false)
const isUploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const isEditMode = computed(() => !!route.query.id)

onMounted(async () => {
  const id = route.query.id
  if (id) {
    loading.value = true
    try {
      const post: any = await blogApi.getPostById(Number(id))
      form.value = {
        title: post.title,
        content: post.content,
        category: post.category,
        images: post.images && post.images.length > 0 ? post.images : (post.coverImage ? [post.coverImage] : []),
        tags: post.tags || []
      }
    } catch (error) {
      console.error('Failed to load post:', error)
      toast.error('加载帖子失败')
    } finally {
      loading.value = false
    }
  }
})

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return

  // 如果需要允许选择多个文件，但目前逐个选择即可，或使用循环
  // 如果输入支持多个，则遍历它们
  for (let i = 0; i < input.files.length; i++) {
    const file = input.files[i]
    if (!file.type.startsWith('image/')) continue
    
    await uploadFile(file)
  }
  
  // 重置
  input.value = ''
}

const uploadFile = async (file: File) => {
  isUploading.value = true
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await axios.post('http://localhost:8080/api/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // 添加到图片列表
    form.value.images.push(res.data.url) 
  } catch (error) {
    console.error('Upload failed:', error)
    toast.error('图片上传失败')
  } finally {
    isUploading.value = false
  }
}

const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
}

const toggleTag = (tag: string) => {
  const idx = form.value.tags.indexOf(tag)
  if (idx === -1) {
    if (form.value.tags.length >= 3) {
      toast.warning('最多选择3个标签')
      return
    }
    form.value.tags.push(tag)
  } else {
    form.value.tags.splice(idx, 1)
  }
}

const submitPost = async () => {
  if (!userStore.isLoggedIn) {
    toast.error('请先登录')
    router.push('/login')
    return
  }

  if (form.value.images.length === 0) {
    toast.warning('请至少上传一张图片')
    return
  }

  // 根据角色处理标签
  let finalTags = [...form.value.tags]
  if (isAdmin.value) {
    finalTags = ['官方']
  } else {
    if (finalTags.length === 0) {
      toast.warning('请选择至少一个标签')
      return
    }
  }

  loading.value = true
  try {
    const postData = {
      title: form.value.title,
      content: form.value.content,
      category: form.value.category,
      coverImage: form.value.images.length > 0 ? form.value.images[0] : null,
      images: form.value.images,
      tags: finalTags,
      author: { id: userStore.user.id } as any
    }

    if (isEditMode.value) {
        await blogApi.updatePost(Number(route.query.id), postData)
        toast.success('修改成功！')
    } else {
        await blogApi.createPost(postData)
        toast.success('发布成功！')
    }
    
    router.push('/blog')
  } catch (error) {
    console.error('Failed to save post:', error)
    toast.error(isEditMode.value ? '修改失败' : '发布失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#FDFBF7] py-12 px-4 sm:px-6 lg:px-8 font-sans">
    <div class="max-w-3xl mx-auto bg-white rounded-3xl shadow-xl shadow-rose-50/50 p-8 border border-zinc-50">
      <div class="mb-8 border-b border-zinc-100 pb-6">
        <h1 class="text-3xl font-black text-zinc-800 tracking-tight">{{ isEditMode ? '编辑帖子' : '发布新帖子' }}</h1>
        <p class="text-zinc-400 mt-2 font-medium">分享你的潮汕生活点滴，记录美好时光</p>
      </div>

      <form @submit.prevent="submitPost" class="space-y-8">
        <!-- 标题 -->
        <div>
          <label class="block text-sm font-bold text-zinc-600 mb-2">标题</label>
          <input 
            v-model="form.title" 
            type="text" 
            required
            class="w-full px-5 py-3 bg-zinc-50 border border-zinc-100 rounded-xl focus:ring-2 focus:ring-[#FF6B6B] focus:border-[#FF6B6B] outline-none transition-all font-bold text-zinc-700 placeholder-zinc-300"
            placeholder="给你的帖子起个好听的名字"
          />
        </div>

        <!-- 分类 -->
        <div>
          <label class="block text-sm font-bold text-zinc-600 mb-2">分类</label>
          <div class="flex flex-wrap gap-2">
            <button 
              type="button"
              v-for="cat in categories" 
              :key="cat" 
              @click="form.category = cat"
              :class="[
                'px-4 py-2 rounded-xl text-sm font-bold transition-all border',
                form.category === cat 
                  ? 'bg-[#FF6B6B] text-white border-[#FF6B6B] shadow-md shadow-rose-200' 
                  : 'bg-white text-zinc-500 border-zinc-200 hover:bg-zinc-50'
              ]"
            >
              {{ cat }}
            </button>
          </div>
        </div>

        <!-- 标签（基于角色） -->
        <div>
          <label class="block text-sm font-bold text-zinc-600 mb-2">标签</label>
          
          <div v-if="isAdmin" class="flex items-center gap-2">
             <span class="px-4 py-2 bg-red-600 text-white text-xs font-bold rounded-xl shadow-md"># 官方</span>
             <span class="text-xs text-zinc-400 font-medium">管理员默认使用官方标签</span>
          </div>

          <div v-else class="flex flex-wrap gap-2">
             <button 
              type="button"
              v-for="word in beautifulWords" 
              :key="word" 
              @click="toggleTag(word)"
              :class="[
                'px-4 py-2 rounded-xl text-xs font-bold transition-all border flex items-center gap-1',
                form.tags.includes(word) 
                  ? 'bg-rose-50 text-rose-500 border-rose-200' 
                  : 'bg-white text-zinc-500 border-zinc-200 hover:border-rose-200'
              ]"
            >
              <span v-if="form.tags.includes(word)" class="text-rose-500">✓</span>
              {{ word }}
            </button>
          </div>
          <p v-if="!isAdmin" class="text-xs text-zinc-400 mt-2 font-medium">选择适合你心情的美好词汇（最多3个）</p>
        </div>

        <!-- 内容 -->
        <div>
          <label class="block text-sm font-bold text-zinc-600 mb-2">内容</label>
          <textarea 
            v-model="form.content" 
            rows="6" 
            required
            class="w-full px-5 py-4 bg-zinc-50 border border-zinc-100 rounded-xl focus:ring-2 focus:ring-[#FF6B6B] focus:border-[#FF6B6B] outline-none transition-all font-medium text-zinc-700 placeholder-zinc-300 resize-none leading-relaxed"
            placeholder="写下你想说的话..."
          ></textarea>
        </div>

        <!-- 图片上传（轮播/网格） -->
        <div>
          <label class="block text-sm font-bold text-zinc-600 mb-2">图片 ({{ form.images.length }})</label>
          
          <input 
            type="file" 
            ref="fileInput" 
            class="hidden" 
            accept="image/*"
            multiple
            @change="handleFileChange"
          />

          <div class="grid grid-cols-3 gap-4">
             <!-- 已上传图片 -->
             <div v-for="(img, idx) in form.images" :key="idx" class="relative group aspect-square rounded-2xl overflow-hidden border border-zinc-100 bg-zinc-50 shadow-sm">
                <img :src="img" class="w-full h-full object-cover" />
                <button 
                  @click="removeImage(idx)" 
                  class="absolute top-2 right-2 p-1.5 bg-black/40 hover:bg-rose-500 backdrop-blur rounded-full text-white transition-all opacity-0 group-hover:opacity-100"
                >
                  <X class="w-3 h-3" />
                </button>
                <div v-if="idx === 0" class="absolute bottom-2 left-2 px-2 py-0.5 bg-[#FF6B6B] text-white text-[10px] font-bold rounded-md shadow-sm">封面</div>
             </div>

             <!-- 添加按钮 -->
             <div 
               @click="triggerUpload"
               class="aspect-square border-2 border-dashed border-zinc-200 rounded-2xl flex flex-col items-center justify-center cursor-pointer hover:border-[#FF6B6B] hover:bg-rose-50 transition-all group"
             >
                <div v-if="isUploading" class="animate-spin rounded-full h-6 w-6 border-b-2 border-[#FF6B6B]"></div>
                <template v-else>
                   <Plus class="w-8 h-8 text-zinc-300 group-hover:text-[#FF6B6B] transition-colors mb-1" />
                   <span class="text-xs font-bold text-zinc-400 group-hover:text-[#FF6B6B]">添加图片</span>
                </template>
             </div>
          </div>
          <p class="text-xs text-zinc-400 mt-2 font-medium">第一张图片将作为封面，支持上传多张图片展示轮播效果</p>
        </div>

        <!-- 操作 -->
        <div class="flex justify-end gap-4 pt-6 border-t border-zinc-50">
          <button 
            type="button" 
            @click="router.back()"
            class="px-8 py-3 border border-zinc-200 rounded-xl font-bold text-zinc-500 hover:bg-zinc-50 transition-colors"
          >
            取消
          </button>
          <button 
            type="submit" 
            :disabled="loading"
            class="px-10 py-3 bg-[#FF6B6B] text-white rounded-xl font-bold shadow-lg shadow-rose-200 hover:bg-rose-600 hover:shadow-xl hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:shadow-none flex items-center gap-2"
          >
            <span v-if="loading" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></span>
            {{ isEditMode ? '保存修改' : '发布帖子' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
