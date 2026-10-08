<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marketApi, favoriteApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import type { Product } from '@/types'
import { useToast } from '@/composables/useToast'
import { ShoppingCart, Heart, ArrowLeft, Star, Truck, ShieldCheck, Clock, Zap } from 'lucide-vue-next'
import CartDrawer from '@/components/CartDrawer.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const toast = useToast()

const product = ref<Product | null>(null)
const loading = ref(false)
const isFavorite = ref(false)
const quantity = ref(1)

const fetchProduct = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    // 并行获取商品和收藏状态
    const [prodRes, favRes] = await Promise.all([
      marketApi.getProductById(id),
      userStore.isLoggedIn ? favoriteApi.checkFavorite(userStore.user!.id, id) : Promise.resolve(false)
    ])
    
    product.value = prodRes as any
    isFavorite.value = favRes as any
  } catch (error) {
    console.error('Failed to load product:', error)
    toast.error('商品加载失败')
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  if (!product.value) return
  
  try {
    await favoriteApi.toggleFavorite(userStore.user!.id, product.value.id)
    isFavorite.value = !isFavorite.value
    toast.success(isFavorite.value ? '已收藏' : '已取消收藏')
  } catch (error) {
    toast.error('操作失败')
  }
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  if (!product.value) return

  try {
    await cartStore.addToCart(userStore.user!.id, product.value.id, quantity.value)
    toast.success('已加入购物车')
  } catch (error) {
    toast.error('加入购物车失败')
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  if (!product.value) return
  
  try {
      await marketApi.createOrder({
          userId: userStore.user!.id,
          productId: product.value.id,
          quantity: quantity.value
      })
      toast.success('下单成功！')
  } catch (e: any) {
      if (e.response?.data?.message?.includes('address')) {
         toast.error('请先在个人中心添加收货地址')
      } else {
         toast.error('下单失败，请重试')
      }
  }
}

const increaseQuantity = () => quantity.value++
const decreaseQuantity = () => {
  if (quantity.value > 1) quantity.value--
}

onMounted(() => {
  fetchProduct()
})
</script>

<template>
  <div class="min-h-screen bg-zinc-50 pb-20">
    <CartDrawer />
    
    <!-- 头部 -->
    <header class="bg-white sticky top-0 z-40 border-b border-zinc-100 shadow-sm">
       <div class="container mx-auto px-4 h-16 flex items-center justify-between">
          <button @click="router.back()" class="p-2 -ml-2 hover:bg-zinc-100 rounded-full transition-colors text-zinc-600">
             <ArrowLeft class="w-5 h-5" />
          </button>
          <h1 class="font-bold text-zinc-800">商品详情</h1>
          <button @click="cartStore.openCart()" class="p-2 -mr-2 hover:bg-zinc-100 rounded-full transition-colors text-zinc-600 relative">
             <ShoppingCart class="w-5 h-5" />
             <span v-if="cartStore.hasItems" class="absolute top-1 right-1 w-2.5 h-2.5 bg-rose-500 rounded-full border-2 border-white"></span>
          </button>
       </div>
    </header>

    <div v-if="loading" class="flex justify-center py-20">
       <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-rose-500"></div>
    </div>

    <div v-else-if="!product" class="text-center py-20 text-zinc-400">
       <p>商品不存在或已下架</p>
    </div>

    <div v-else class="container mx-auto px-4 py-8">
       <div class="bg-white rounded-3xl shadow-sm border border-zinc-100 overflow-hidden flex flex-col md:flex-row">
          <!-- 图片区域 -->
          <div class="md:w-1/2 bg-zinc-50 p-8 flex items-center justify-center relative">
             <img :src="product.imageUrl" class="w-full max-w-md object-contain drop-shadow-xl hover:scale-105 transition-transform duration-500" />
             <div class="absolute top-4 left-4 bg-secondary-green text-white px-3 py-1 rounded-full text-xs font-bold shadow-sm">新品上市</div>
          </div>

          <!-- 信息区域 -->
          <div class="md:w-1/2 p-8 md:p-12 flex flex-col">
             <div class="flex justify-between items-start mb-4">
                <div>
                   <h1 class="text-3xl font-black text-zinc-900 mb-2">{{ product.name }}</h1>
                   <div class="flex items-center gap-2 text-sm text-zinc-500">
                      <span class="bg-zinc-100 px-2 py-0.5 rounded text-zinc-600 font-bold">{{ product.category }}</span>
                      <div class="flex items-center gap-1 text-amber-500">
                         <Star class="w-4 h-4 fill-current" />
                         <span class="font-bold">4.9</span>
                      </div>
                      <span>(200+ 评价)</span>
                   </div>
                </div>
                <button @click="toggleFavorite" 
                        class="p-3 rounded-full border-2 transition-all duration-300 group"
                        :class="isFavorite ? 'bg-rose-50 border-rose-500 text-rose-500 shadow-sm' : 'bg-white border-rose-200 text-rose-300 hover:border-rose-400 hover:text-rose-400'">
                   <Heart class="w-6 h-6 transition-transform active:scale-90" :class="{ 'fill-current': isFavorite }" />
                </button>
             </div>

             <div class="text-3xl font-black text-rose-500 mb-8">¥{{ product.price }}</div>

             <p class="text-zinc-600 leading-relaxed mb-8">
                {{ product.description || '这款商品非常可爱，不仅外观精致，而且材质柔软舒适，是您日常生活中的完美伴侣。无论是自用还是送礼，都是绝佳的选择。' }}
             </p>

             <!-- 数量选择器 -->
             <div class="flex items-center gap-4 mb-8">
                <span class="text-sm font-bold text-zinc-500">数量</span>
                <div class="flex items-center bg-zinc-100 rounded-full p-1">
                   <button @click="decreaseQuantity" class="w-8 h-8 flex items-center justify-center bg-white rounded-full shadow-sm text-zinc-600 hover:text-rose-500 font-bold disabled:opacity-50" :disabled="quantity <= 1">-</button>
                   <span class="w-10 text-center font-bold text-zinc-800">{{ quantity }}</span>
                   <button @click="increaseQuantity" class="w-8 h-8 flex items-center justify-center bg-white rounded-full shadow-sm text-zinc-600 hover:text-rose-500 font-bold">+</button>
                </div>
             </div>

             <!-- 服务保障 -->
             <div class="grid grid-cols-2 gap-4 mb-8">
                <div class="flex items-center gap-2 text-xs font-bold text-zinc-500">
                   <Truck class="w-4 h-4 text-rose-500" /> 全场包邮
                </div>
                <div class="flex items-center gap-2 text-xs font-bold text-zinc-500">
                   <Clock class="w-4 h-4 text-rose-500" /> 24小时发货
                </div>
                <div class="flex items-center gap-2 text-xs font-bold text-zinc-500">
                   <ShieldCheck class="w-4 h-4 text-rose-500" /> 正品保障
                </div>
                <div class="flex items-center gap-2 text-xs font-bold text-zinc-500">
                   <Zap class="w-4 h-4 text-rose-500" /> 急速退款
                </div>
             </div>

             <!-- 操作 -->
             <div class="mt-auto flex gap-4">
                <button @click="addToCart" class="flex-1 py-3.5 bg-amber-400 text-white rounded-xl font-bold hover:bg-amber-500 transition-all shadow-lg shadow-amber-100 flex items-center justify-center gap-2">
                   <ShoppingCart class="w-5 h-5" /> 加入购物车
                </button>
                <button @click="buyNow" class="flex-1 py-3.5 bg-rose-500 text-white rounded-xl font-bold hover:bg-rose-600 transition-all shadow-lg shadow-rose-200 flex items-center justify-center gap-2">
                   <Zap class="w-5 h-5" /> 立即购买
                </button>
             </div>
          </div>
       </div>
    </div>
  </div>
</template>
