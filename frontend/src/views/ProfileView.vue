<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { User as UserIcon, MapPin, Package, LogOut, Edit2, Trash2, Plus, Phone, Heart, FileText, Store, BookOpen, Repeat, X, MessageCircle, Star } from 'lucide-vue-next'
import { addressApi, favoriteApi, marketApi, blogApi, userApi } from '@/api'
import { useToast } from '@/composables/useToast'
import AddressModal from '@/components/AddressModal.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import type { BlogPost } from '@/types'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()

const currentProfileMode = ref<'market'|'blog'>('market')
const activeTab = ref('orders') // 市集：订单、地址、收藏；博客：帖子（内部处理子标签）

// 博客模式标签
const activeBlogTab = ref('my_posts') // 'my_posts', 'liked_posts', 'favorited_posts'

// 数据
const addresses = ref<any[]>([])
const favorites = ref<any[]>([])
const orders = ref<any[]>([])
const myPosts = ref<BlogPost[]>([])
const likedPosts = ref<BlogPost[]>([])
const favoritedPosts = ref<BlogPost[]>([])
const loading = ref(false)

const showAddressModal = ref(false)
const editingAddress = ref<any>(null)

// 资料编辑
const isEditingProfile = ref(false)
const profileForm = ref({
  nickname: '',
  avatar: '',
  bio: '',
  age: 18,
  location: ''
})

const startEditProfile = () => {
  if (userStore.user) {
    profileForm.value = {
      nickname: userStore.user.nickname,
      avatar: userStore.user.avatar,
      bio: userStore.user.bio || '',
      age: userStore.user.age || 18,
      location: userStore.user.location || ''
    }
    isEditingProfile.value = true
  }
}

const saveProfile = async () => {
  if (!userStore.user?.id) return
  try {
    const updatedUser = await userApi.updateUser(userStore.user.id, profileForm.value) as unknown as import('@/types').User
    // 更新本地 store
    userStore.user.nickname = updatedUser.nickname
    userStore.user.avatar = updatedUser.avatar
    userStore.user.bio = updatedUser.bio
    userStore.user.age = updatedUser.age
    userStore.user.location = updatedUser.location
    localStorage.setItem('user', JSON.stringify(userStore.user))
    
    toast.success('个人资料已更新')
    isEditingProfile.value = false
  } catch (error) {
    console.error('Update profile failed:', error)
    toast.error('更新失败')
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/')
    return
  }

  if (route.query.mode === 'blog') {
    currentProfileMode.value = 'blog'
    activeTab.value = 'posts'
  } else if (route.query.mode === 'market') {
    currentProfileMode.value = 'market'
    activeTab.value = 'orders'
  }

  loadData()
})

watch([currentProfileMode, activeTab], () => {
  loadData()
})

watch(activeBlogTab, () => {
    // 在真实应用中，获取特定列表。这里我们仅使用 loadPosts 中加载的模拟/过滤列表
})

const showRefundModal = ref(false)
const selectedReason = ref('')
const selectedRefundType = ref('ONLY_REFUND')
const refundLoading = ref(false)
const currentRefundOrderId = ref<number | null>(null)
const refundReasons = ['质量问题', '商品描述不符', '少件/漏发', '包装破损', '拍错/多拍', '不想要了', '其他']
const refundTypes = [
  { value: 'ONLY_REFUND', label: '仅退款' },
  { value: 'RETURN_AND_REFUND', label: '退货退款' }
]

const openRefundModal = (order: any) => {
  currentRefundOrderId.value = order.id
  selectedReason.value = ''
  selectedRefundType.value = 'ONLY_REFUND'
  showRefundModal.value = true
}

const handleApplyRefund = async () => {
  if (!currentRefundOrderId.value) return
  if (!selectedReason.value) {
    toast.error('请选择退款理由')
    return
  }
  
  refundLoading.value = true
  try {
    await marketApi.applyRefund(currentRefundOrderId.value, selectedReason.value, selectedRefundType.value)
    toast.success('申请提交成功，请等待商家处理')
    showRefundModal.value = false
    loadOrders() // 刷新列表
  } catch (error) {
    console.error('Apply refund failed:', error)
    toast.error('申请退款失败')
  } finally {
    refundLoading.value = false
  }
}

const cancelOrder = async (order: any) => {
  if (!confirm('确定要取消该订单并退款吗？')) return
  try {
    await marketApi.applyRefund(order.id, '买家反悔/取消订单', 'ONLY_REFUND')
    toast.success('已提交取消/退款申请')
    loadOrders()
  } catch (error) {
    toast.error('操作失败')
  }
}

const deleteOrder = async (orderId: number) => {
  if (!confirm('确定要删除该订单吗？删除后将无法恢复。')) return
  try {
    await marketApi.deleteOrder(orderId)
    toast.success('订单已删除')
    loadOrders()
  } catch (error) {
    toast.error('删除订单失败')
  }
}

const loadData = async () => {
  if (!userStore.user?.id) return
  loading.value = true
  try {
    if (currentProfileMode.value === 'market') {
      if (activeTab.value === 'address') await loadAddresses()
      if (activeTab.value === 'favorites') await loadFavorites()
      if (activeTab.value === 'orders') await loadOrders()
    } else {
      // 博客模式始终加载帖子逻辑
      await loadPosts()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadAddresses = async () => {
  const res: any = await addressApi.getUserAddresses(userStore.user!.id)
  addresses.value = Array.isArray(res) ? res : []
}

const loadFavorites = async () => {
  const res: any = await favoriteApi.getFavorites(userStore.user!.id)
  favorites.value = Array.isArray(res) ? res : []
}

const loadOrders = async () => {
  const res: any = await marketApi.getUserOrders(userStore.user!.id)
  orders.value = Array.isArray(res) ? res : []
}

const loadPosts = async () => {
  try {
      const allPosts: BlogPost[] = await blogApi.getPosts() as any
      if (Array.isArray(allPosts)) {
          // 过滤我的帖子
          myPosts.value = allPosts.filter(p => p.author.id === userStore.user?.id)
          
          // 模拟演示用喜欢的和收藏的帖子（随机选择）
          // 在真实后端中，我们会调用 blogApi.getLikedPosts() 等
          likedPosts.value = allPosts.filter(() => Math.random() > 0.7)
          favoritedPosts.value = allPosts.filter(() => Math.random() > 0.8)
      }
  } catch (e) {
      console.error("Failed to load posts", e)
      myPosts.value = []
  }
}

const openAddAddress = () => {
  editingAddress.value = null
  showAddressModal.value = true
}

const openEditAddress = (address: any) => {
  editingAddress.value = address
  showAddressModal.value = true
}

const deleteAddress = async (id: number) => {
  if (!confirm('确定要删除该地址吗？')) return
  try {
    await addressApi.deleteAddress(id)
    toast.success('地址已删除')
    loadAddresses()
  } catch (error) {
    toast.error('删除失败')
  }
}

const removeFavorite = async (productId: number) => {
   try {
     await favoriteApi.toggleFavorite(userStore.user!.id, productId)
     toast.success('已取消收藏')
     loadFavorites()
   } catch (e) {
     toast.error('操作失败')
   }
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

const formatTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString()
}
</script>

<template>
  <div class="min-h-screen bg-zinc-50 py-12 transition-colors duration-500" :class="currentProfileMode === 'market' ? 'bg-amber-50/30' : 'bg-[#FAFAFA]'">
    <div class="container mx-auto px-4 md:px-8">
      
      <!-- 顶部切换器 -->
      <div class="flex justify-center mb-8">
        <div class="bg-white p-1 rounded-full shadow-sm border border-zinc-100 flex items-center">
           <button 
             @click="currentProfileMode = 'market'; activeTab = 'orders'"
             :class="['px-6 py-2 rounded-full font-bold text-sm flex items-center gap-2 transition-all', currentProfileMode === 'market' ? 'bg-amber-400 text-white shadow-md' : 'text-zinc-500 hover:bg-zinc-50']"
           >
             <Store class="w-4 h-4" /> 市集中心
           </button>
           <button 
             @click="currentProfileMode = 'blog'; activeTab = 'posts'"
             :class="['px-6 py-2 rounded-full font-bold text-sm flex items-center gap-2 transition-all', currentProfileMode === 'blog' ? 'bg-[#FF6B6B] text-white shadow-md' : 'text-zinc-500 hover:bg-zinc-50']"
           >
             <BookOpen class="w-4 h-4" /> 博客空间
           </button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- 侧边栏 -->
        <div class="md:col-span-1">
          <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 sticky top-24 transition-all duration-300" :class="currentProfileMode === 'market' ? 'shadow-amber-100' : 'shadow-rose-100'">
            <div class="flex flex-col items-center mb-6 relative">
              <div class="relative mb-4 group">
                <img :src="userStore.user?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20avatar%20small&image_size=square'" class="w-24 h-24 rounded-full border-4 object-cover transition-colors" :class="currentProfileMode === 'market' ? 'border-amber-200' : 'border-rose-200'" />
                <button @click="startEditProfile" class="absolute bottom-0 right-0 p-1.5 bg-white rounded-full shadow border border-zinc-100 text-zinc-400 hover:text-rose-500 transition-colors">
                   <Edit2 class="w-4 h-4" />
                </button>
              </div>
              <h2 class="text-xl font-black text-zinc-800">{{ userStore.user?.nickname }}</h2>
              <p class="text-xs text-zinc-400 mt-1">@{{ userStore.user?.username }}</p>
            </div>

            <!-- 博客模式个人资料详情 -->
            <template v-if="currentProfileMode === 'blog'">
               <div class="text-center mb-6 px-2">
                 <p class="text-sm text-zinc-600 font-medium italic">{{ userStore.user?.bio || '这个人很懒，什么都没写~' }}</p>
               </div>
               
               <div class="flex justify-center gap-3 mb-6 flex-wrap">
                 <span v-if="userStore.user?.age" class="px-2 py-1 bg-blue-50 text-blue-500 text-xs font-bold rounded-lg">{{ userStore.user?.age }}岁</span>
                 <span v-if="userStore.user?.location" class="px-2 py-1 bg-green-50 text-green-600 text-xs font-bold rounded-lg flex items-center gap-1"><MapPin class="w-3 h-3"/> {{ userStore.user?.location }}</span>
               </div>

               <div class="grid grid-cols-3 gap-2 mb-6 text-center">
                 <div class="bg-zinc-50 rounded-xl p-2">
                   <div class="text-lg font-black text-zinc-800">{{ userStore.user?.followersCount || 0 }}</div>
                   <div class="text-[10px] text-zinc-400 font-bold">粉丝</div>
                 </div>
                 <div class="bg-zinc-50 rounded-xl p-2">
                   <div class="text-lg font-black text-zinc-800">{{ userStore.user?.followingCount || 0 }}</div>
                   <div class="text-[10px] text-zinc-400 font-bold">关注</div>
                 </div>
                 <div class="bg-zinc-50 rounded-xl p-2">
                   <div class="text-lg font-black text-zinc-800">{{ userStore.user?.totalLikesReceived || 0 }}</div>
                   <div class="text-[10px] text-zinc-400 font-bold">获赞</div>
                 </div>
               </div>
            </template>

            <nav class="space-y-2">
              <template v-if="currentProfileMode === 'market'">
                <button 
                  @click="activeTab = 'orders'"
                  :class="['w-full flex items-center gap-3 px-4 py-3 rounded-xl font-bold transition-all', activeTab === 'orders' ? 'bg-amber-50 text-amber-600' : 'text-zinc-500 hover:bg-zinc-50']"
                >
                  <Package class="w-5 h-5" /> 我的订单
                </button>
                <button 
                  @click="activeTab = 'address'"
                  :class="['w-full flex items-center gap-3 px-4 py-3 rounded-xl font-bold transition-all', activeTab === 'address' ? 'bg-amber-50 text-amber-600' : 'text-zinc-500 hover:bg-zinc-50']"
                >
                  <MapPin class="w-5 h-5" /> 地址管理
                </button>
                <button 
                  @click="activeTab = 'favorites'"
                  :class="['w-full flex items-center gap-3 px-4 py-3 rounded-xl font-bold transition-all', activeTab === 'favorites' ? 'bg-amber-50 text-amber-600' : 'text-zinc-500 hover:bg-zinc-50']"
                >
                  <Heart class="w-5 h-5" /> 我的收藏
                </button>
              </template>

              <!-- 博客模式没有额外的导航，标签在主要内容中 -->

              <div class="border-t border-zinc-100 my-2 pt-2">
                <button @click="handleLogout" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl font-bold text-zinc-400 hover:text-red-500 hover:bg-red-50 transition-all">
                  <LogOut class="w-5 h-5" /> 退出登录
                </button>
              </div>
            </nav>
          </div>
        </div>

        <!-- 主要内容 -->
        <div class="md:col-span-3">
          
          <!-- 市集模式 -->
          <template v-if="currentProfileMode === 'market'">
             <!-- 订单标签页 -->
             <div v-if="activeTab === 'orders'" class="bg-white rounded-3xl p-8 shadow-sm border border-zinc-100 animate-in fade-in slide-in-from-bottom-4">
                <h2 class="text-2xl font-black text-zinc-800 mb-6">我的订单</h2>
                <div v-if="orders.length === 0" class="text-center py-20 bg-zinc-50 rounded-2xl border border-zinc-100 border-dashed">
                  <Package class="w-12 h-12 text-zinc-300 mx-auto mb-3" />
                  <p class="text-zinc-400 font-medium">暂无订单记录</p>
                </div>
                <div v-else class="space-y-4">
                  <div v-for="order in orders" :key="order.id" @click="router.push(`/market/order/${order.id}`)" class="border border-zinc-100 rounded-2xl p-5 hover:shadow-md transition-all cursor-pointer group bg-white">
                     <div class="flex justify-between items-center mb-4 border-b border-zinc-50 pb-3">
                        <span class="text-xs font-bold text-zinc-400 group-hover:text-rose-500 transition-colors">订单号 #{{ order.id }}</span>
                        <span class="px-2.5 py-1 bg-amber-100 text-amber-600 rounded-lg text-xs font-bold">{{ order.status === 'PENDING' ? '待发货' : order.status === 'SHIPPED' ? '已发货' : order.status === 'COMPLETED' ? '已完成' : order.status }}</span>
                     </div>
                      <div class="space-y-3">
                         <div v-for="item in order.items" :key="item.id" class="flex gap-4">
                            <img :src="item.product.imageUrl" class="w-16 h-16 rounded-lg bg-zinc-50 object-cover"/>
                            <div>
                               <h4 class="font-bold text-zinc-800 text-sm">{{ item.product.name }}</h4>
                               <p class="text-xs text-zinc-500 mt-1">x {{ item.quantity }}</p>
                            </div>
                         </div>
                      </div>
                      <div class="flex justify-between items-center mt-4 pt-3 border-t border-zinc-50">
                         <div class="flex gap-2">
                            <span v-if="order.refundStatus && order.refundStatus !== 'NONE'" class="text-xs font-bold text-orange-500 bg-orange-50 px-2 py-1 rounded-md flex items-center">
                                {{ 
                                  order.status === 'CANCELLED' ? '已退款/取消' :
                                  order.refundStatus === 'REFUND_APPLIED' ? '售后中' : 
                                  order.refundStatus === 'REFUND_APPROVED' ? (order.refundType === 'RETURN_AND_REFUND' ? '退货退款中' : '退款中') :
                                  order.refundStatus === 'REFUND_SUCCESS' ? '退款成功' :
                                  order.refundStatus === 'REFUND_REJECTED' ? '退款已拒绝' : '售后处理中' 
                                }}
                            </span>
                            <template v-else>
                                <button 
                                    v-if="order.status === 'PENDING' || order.status === 'PAID'"
                                    @click.stop="cancelOrder(order)"
                                    class="px-3 py-1 bg-rose-50 hover:bg-rose-100 text-rose-500 text-xs font-bold rounded-full transition-colors"
                                >
                                    退货/取消
                                </button>
                                <button 
                                    v-else-if="['SHIPPED', 'COMPLETED'].includes(order.status)"
                                    @click.stop="openRefundModal(order)"
                                    class="px-3 py-1 bg-zinc-100 hover:bg-zinc-200 text-zinc-600 text-xs font-bold rounded-full transition-colors"
                                >
                                    申请售后
                                </button>
                            </template>
                            
                            <button 
                                @click.stop="deleteOrder(order.id)"
                                class="px-3 py-1 bg-zinc-50 hover:bg-red-50 text-zinc-400 hover:text-red-500 text-xs font-bold rounded-full transition-colors"
                            >
                                删除
                            </button>
                         </div>
                         <div class="flex items-center">
                            <span class="text-sm font-bold text-zinc-500 mr-2">合计:</span>
                            <span class="text-lg font-black text-amber-500">¥{{ order.totalAmount }}</span>
                         </div>
                      </div>
                   </div>
                </div>
             </div>

             <!-- 地址标签页 -->
             <div v-if="activeTab === 'address'" class="bg-white rounded-3xl p-8 shadow-sm border border-zinc-100 animate-in fade-in slide-in-from-bottom-4">
                <div class="flex justify-between items-center mb-8">
                  <h2 class="text-2xl font-black text-zinc-800">地址管理</h2>
                  <button @click="openAddAddress" class="px-5 py-2.5 bg-amber-400 text-white rounded-xl font-bold shadow-lg shadow-amber-200 hover:bg-amber-500 transition-all flex items-center gap-2">
                    <Plus class="w-4 h-4" /> 新增地址
                  </button>
                </div>

                <div v-if="addresses.length === 0" class="text-center py-20 bg-zinc-50 rounded-2xl border border-zinc-100 border-dashed">
                  <MapPin class="w-12 h-12 text-zinc-300 mx-auto mb-3" />
                  <p class="text-zinc-400 font-medium">暂无收货地址，快去添加一个吧</p>
                </div>

                <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div v-for="addr in addresses" :key="addr.id" class="border border-zinc-100 rounded-2xl p-5 hover:shadow-md transition-all group relative overflow-hidden">
                    <div v-if="addr.isDefault" class="absolute top-0 right-0 bg-amber-400 text-white text-[10px] font-bold px-2 py-1 rounded-bl-xl z-10">默认</div>
                    
                    <div class="flex items-start gap-4 mb-3">
                      <div class="p-3 bg-amber-50 rounded-full text-amber-500">
                        <MapPin class="w-5 h-5" />
                      </div>
                      <div>
                        <div class="font-bold text-zinc-800 text-lg">{{ addr.recipientName }}</div>
                        <div class="text-zinc-500 text-sm font-medium flex items-center gap-1 mt-0.5">
                          <Phone class="w-3 h-3" /> {{ addr.phone }}
                        </div>
                      </div>
                    </div>
                    
                    <div class="text-zinc-600 text-sm leading-relaxed mb-4 pl-14">
                      {{ addr.province }} {{ addr.city }} {{ addr.district }}<br/>
                      {{ addr.detailAddress }}
                    </div>

                    <div class="flex justify-end gap-2 border-t border-zinc-50 pt-3">
                      <button @click="openEditAddress(addr)" class="px-3 py-1.5 text-zinc-500 hover:bg-zinc-50 hover:text-blue-500 rounded-lg text-xs font-bold transition-colors flex items-center gap-1">
                        <Edit2 class="w-3 h-3" /> 编辑
                      </button>
                      <button @click="deleteAddress(addr.id)" class="px-3 py-1.5 text-zinc-500 hover:bg-rose-50 hover:text-rose-500 rounded-lg text-xs font-bold transition-colors flex items-center gap-1">
                        <Trash2 class="w-3 h-3" /> 删除
                      </button>
                    </div>
                  </div>
                </div>
             </div>

             <!-- Favorites Tab -->
             <div v-if="activeTab === 'favorites'" class="bg-white rounded-3xl p-8 shadow-sm border border-zinc-100 animate-in fade-in slide-in-from-bottom-4">
                <h2 class="text-2xl font-black text-zinc-800 mb-6">我的收藏</h2>
                <div v-if="favorites.length === 0" class="text-center py-20 bg-zinc-50 rounded-2xl border border-zinc-100 border-dashed">
                  <Heart class="w-12 h-12 text-zinc-300 mx-auto mb-3" />
                  <p class="text-zinc-400 font-medium">还没有收藏喜欢的商品哦</p>
                </div>
                <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-4">
                   <div v-for="fav in favorites" :key="fav.id" class="border border-zinc-100 rounded-2xl p-4 hover:shadow-md transition-all relative group">
                      <button @click="removeFavorite(fav.product.id)" class="absolute top-2 right-2 p-1.5 bg-white/80 rounded-full text-rose-500 hover:bg-rose-500 hover:text-white transition-colors z-10">
                         <Heart class="w-4 h-4 fill-current" />
                      </button>
                      <div class="aspect-square bg-zinc-50 rounded-xl mb-3 overflow-hidden">
                         <img :src="fav.product.imageUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                      </div>
                      <h4 class="font-bold text-zinc-800 text-sm truncate">{{ fav.product.name }}</h4>
                      <p class="text-amber-500 font-extrabold mt-1">¥{{ fav.product.price }}</p>
                   </div>
                </div>
             </div>
          </template>

          <!-- 博客模式 -->
          <template v-else>
             <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 min-h-[500px]">
               <!-- 标签页 -->
               <div class="flex border-b border-zinc-100 mb-6">
                 <button 
                   @click="activeBlogTab = 'my_posts'"
                   :class="['px-6 py-3 font-bold text-sm transition-all border-b-2', activeBlogTab === 'my_posts' ? 'border-[#FF6B6B] text-[#FF6B6B]' : 'border-transparent text-zinc-400 hover:text-zinc-600']"
                 >
                   我的帖子
                 </button>
                 <button 
                   @click="activeBlogTab = 'liked_posts'"
                   :class="['px-6 py-3 font-bold text-sm transition-all border-b-2', activeBlogTab === 'liked_posts' ? 'border-[#FF6B6B] text-[#FF6B6B]' : 'border-transparent text-zinc-400 hover:text-zinc-600']"
                 >
                   点赞的帖子
                 </button>
                 <button 
                   @click="activeBlogTab = 'favorited_posts'"
                   :class="['px-6 py-3 font-bold text-sm transition-all border-b-2', activeBlogTab === 'favorited_posts' ? 'border-[#FF6B6B] text-[#FF6B6B]' : 'border-transparent text-zinc-400 hover:text-zinc-600']"
                 >
                   收藏的帖子
                 </button>
               </div>

               <!-- 帖子网格 -->
               <div v-if="activeBlogTab === 'my_posts' && myPosts.length === 0" class="text-center py-20 bg-zinc-50 rounded-2xl border border-zinc-100 border-dashed">
                 <FileText class="w-12 h-12 text-zinc-300 mx-auto mb-3" />
                 <p class="text-zinc-400 font-medium">暂无发布记录</p>
                 <button @click="router.push('/blog/create')" class="mt-4 px-6 py-2 bg-[#FF6B6B] text-white rounded-full font-bold text-sm shadow hover:bg-rose-600 transition-colors">去发布</button>
               </div>

               <div v-else class="grid grid-cols-2 lg:grid-cols-3 gap-4">
                 <div 
                    v-for="post in (activeBlogTab === 'my_posts' ? myPosts : activeBlogTab === 'liked_posts' ? likedPosts : favoritedPosts)" 
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
          </template>

        </div>
      </div>
    </div>

    <!-- 地址弹窗 -->
    <AddressModal 
      :is-open="showAddressModal" 
      :editing-address="editingAddress"
      @close="showAddressModal = false"
      @refresh="loadAddresses"
    />

    <!-- 退款弹窗 -->
    <div v-if="showRefundModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
      <div class="bg-white rounded-3xl w-full max-w-md p-6 space-y-6 shadow-2xl animate-in fade-in zoom-in duration-200">
        <div class="flex items-center justify-between">
          <h3 class="text-xl font-black text-zinc-800">申请售后</h3>
          <button @click="showRefundModal = false" class="p-2 hover:bg-zinc-100 rounded-full transition-colors">
            <X class="w-5 h-5 text-zinc-400" />
          </button>
        </div>
        
        <div>
          <label class="block text-sm font-bold text-zinc-500 mb-3">售后类型</label>
          <div class="flex gap-2 mb-4">
            <button 
              v-for="type in refundTypes" 
              :key="type.value"
              @click="selectedRefundType = type.value"
              class="flex-1 py-2 rounded-xl text-sm font-bold border transition-all"
              :class="selectedRefundType === type.value 
                ? 'bg-rose-50 border-rose-500 text-rose-600' 
                : 'bg-white border-zinc-200 text-zinc-600 hover:border-zinc-300'"
            >
              {{ type.label }}
            </button>
          </div>
          
          <label class="block text-sm font-bold text-zinc-500 mb-3">请选择退款原因</label>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="reason in refundReasons" 
              :key="reason"
              @click="selectedReason = reason"
              class="px-3 py-1.5 rounded-full text-sm font-medium border transition-all"
              :class="selectedReason === reason 
                ? 'bg-rose-50 border-rose-500 text-rose-600' 
                : 'bg-white border-zinc-200 text-zinc-600 hover:border-zinc-300'"
            >
              {{ reason }}
            </button>
          </div>
        </div>

        <div class="flex gap-3 pt-2">
          <button @click="showRefundModal = false" class="flex-1 py-3 font-bold text-zinc-500 hover:bg-zinc-50 rounded-xl transition-colors">取消</button>
          <button 
            @click="handleApplyRefund" 
            :disabled="refundLoading || !selectedReason"
            class="flex-1 py-3 font-bold text-white bg-rose-500 hover:bg-rose-600 rounded-xl shadow-lg shadow-rose-200 transition-all disabled:opacity-50 disabled:shadow-none"
          >
            {{ refundLoading ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <div v-if="isEditingProfile" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
      <div class="bg-white rounded-3xl w-full max-w-md p-6 space-y-6 shadow-2xl animate-in fade-in zoom-in duration-200">
        <div class="flex items-center justify-between">
          <h3 class="text-xl font-black text-zinc-800">编辑个人资料</h3>
          <button @click="isEditingProfile = false" class="p-2 hover:bg-zinc-100 rounded-full transition-colors">
            <X class="w-5 h-5 text-zinc-400" />
          </button>
        </div>
        
        <div class="space-y-4 max-h-[70vh] overflow-y-auto pr-2">
           <div class="flex justify-center">
             <div class="w-32">
                <label class="block text-xs font-bold text-zinc-400 mb-2 text-center uppercase">头像</label>
                <ImageUpload v-model="profileForm.avatar" />
             </div>
           </div>
           
           <div>
             <label class="block text-sm font-bold text-zinc-500 mb-2">昵称</label>
             <input v-model="profileForm.nickname" type="text" class="w-full px-4 py-3 bg-zinc-50 border border-zinc-100 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-400 outline-none transition-all font-bold text-zinc-700" />
           </div>

           <div>
             <label class="block text-sm font-bold text-zinc-500 mb-2">个性签名</label>
             <textarea v-model="profileForm.bio" rows="2" class="w-full px-4 py-3 bg-zinc-50 border border-zinc-100 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-400 outline-none transition-all font-bold text-zinc-700 resize-none"></textarea>
           </div>

           <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-bold text-zinc-500 mb-2">年龄</label>
                <input v-model="profileForm.age" type="number" class="w-full px-4 py-3 bg-zinc-50 border border-zinc-100 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-400 outline-none transition-all font-bold text-zinc-700" />
              </div>
              <div>
                <label class="block text-sm font-bold text-zinc-500 mb-2">地区</label>
                <input v-model="profileForm.location" type="text" class="w-full px-4 py-3 bg-zinc-50 border border-zinc-100 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-400 outline-none transition-all font-bold text-zinc-700" />
              </div>
           </div>
        </div>

        <div class="flex gap-3 pt-4">
          <button @click="isEditingProfile = false" class="flex-1 py-3 font-bold text-zinc-500 hover:bg-zinc-50 rounded-xl transition-colors">取消</button>
          <button 
            @click="saveProfile" 
            class="flex-1 py-3 font-bold text-white bg-rose-500 hover:bg-rose-600 rounded-xl shadow-lg shadow-rose-200 transition-all"
          >
            保存修改
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
