<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Heart, ArrowRight, Truck, ShieldCheck, Clock, RefreshCcw, Star, Zap } from 'lucide-vue-next'
import { marketApi, favoriteApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import type { Product } from '@/types'
import CartDrawer from '@/components/CartDrawer.vue'
import { useToast } from '@/composables/useToast'
import PaymentModal from '@/components/PaymentModal.vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const toast = useToast()
const products = ref<Product[]>([])
const loading = ref(false)
const favorites = ref<Set<number>>(new Set())
const showPaymentModal = ref(false)
const currentProduct = ref<Product | null>(null)

watch(() => userStore.isLoggedIn, (newVal) => {
  if (newVal) {
    fetchFavorites()
  } else {
    favorites.value = new Set()
  }
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await marketApi.getProducts()
    products.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('Failed to fetch products:', error)
    toast.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const fetchFavorites = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const favs = (await favoriteApi.getFavorites(userStore.user.id)) as unknown as any[]
    favorites.value = new Set(favs.map((f: any) => Number(f.productId)))
  } catch (error) {
    console.error('Failed to fetch favorites:', error)
  }
}

const isFavorite = (productId: number) => favorites.value.has(Number(productId))

const addToCart = async (product: Product) => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  try {
    await cartStore.addToCart(userStore.user.id, product.id, 1)
    toast.success('已加入购物车')
  } catch (error) {
    console.error('Failed to add to cart:', error)
    toast.error('加入购物车失败')
  }
}

const toggleFavorite = async (product: Product) => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  try {
    const id = Number(product.id)
    await favoriteApi.toggleFavorite(userStore.user.id, id)
    const newFavorites = new Set(favorites.value)
    if (newFavorites.has(id)) {
      newFavorites.delete(id)
      toast.success('已取消收藏')
    } else {
      newFavorites.add(id)
      toast.success('收藏成功')
    }
    favorites.value = newFavorites
  } catch (e) {
    toast.error('操作失败')
  }
}

const handleBuyNow = (product: Product) => {
  if (!userStore.isLoggedIn) {
    userStore.openAuthModal()
    return
  }
  currentProduct.value = product
  showPaymentModal.value = true
}

const onPaymentSuccess = async (paymentData: any) => {
  if (!currentProduct.value) return
  try {
    await marketApi.createOrder({
      userId: userStore.user.id,
      productId: currentProduct.value.id,
      quantity: 1,
      addressId: paymentData.addressId,
      addressSnapshot: paymentData.addressSnapshot,
      contactName: paymentData.contactName,
      contactPhone: paymentData.contactPhone
    })
    toast.success('支付成功，下单完成！')
    showPaymentModal.value = false
  } catch (e: any) {
    if (e.response?.data?.message?.includes('address')) {
      toast.error('请先在个人中心添加收货地址')
    } else {
      toast.error('下单失败，请重试')
    }
  }
}

const goToDetail = (id: number) => {
  router.push({ name: 'product-detail', params: { id } })
}

onMounted(() => {
  fetchProducts()
  fetchFavorites()
})
</script>

<template>
  <div class="bg-market-gradient min-h-screen pb-20 w-full relative">
    <CartDrawer />
    <PaymentModal 
      :is-open="showPaymentModal" 
      :amount="currentProduct?.price || 0"
      @close="showPaymentModal = false"
      @success="onPaymentSuccess"
    />
    
    <!-- 悬浮购物车按钮 -->
    <button 
      @click="cartStore.openCart()"
      class="fixed bottom-24 right-6 z-40 bg-primary-500 text-white p-4 rounded-full shadow-lg shadow-primary-200 hover:scale-110 transition-transform"
    >
      <ShoppingCart class="w-6 h-6" />
      <span v-if="cartStore.hasItems" class="absolute top-0 right-0 w-3.5 h-3.5 bg-red-500 rounded-full border-2 border-white"></span>
    </button>

    <!-- 英雄横幅 -->
    <div class="container mx-auto px-4 pt-10 mb-12">
      <div class="bg-white/40 backdrop-blur-sm rounded-[2.5rem] p-8 md:p-12 flex flex-col md:flex-row items-center justify-between relative overflow-hidden shadow-lg border border-white/50">
        <div class="z-10 md:w-1/2 pl-4">
          <div class="inline-flex items-center gap-1 bg-white text-primary-500 px-4 py-1.5 rounded-full text-xs font-extrabold mb-6 shadow-sm">
            <Heart class="w-3 h-3 fill-current" /> 超可爱
          </div>
          <h1 class="text-4xl md:text-6xl font-black text-dark-900 mb-4 leading-[1.1] tracking-tight">
            桃粿源文创<br/>春季上新！
          </h1>
          <p class="text-dark-800 mb-8 font-bold text-lg">潮汕非遗 × 软萌IP · 让传统文化治愈你的每一天</p>
          <button class="px-10 py-3.5 bg-primary-500 hover:bg-primary-600 text-white rounded-full font-bold shadow-lg shadow-primary-200 transition-transform hover:-translate-y-1">
            立即选购
          </button>
        </div>
        <div class="md:w-1/2 flex justify-center mt-8 md:mt-0 z-10">
           <img src="https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20white%20rabbit%20holding%20shopping%20bags%203d%20style&image_size=square" class="w-72 h-72 object-contain drop-shadow-2xl" />
        </div>
        
        <!-- 装饰气泡 -->
        <div class="absolute top-10 right-10 w-20 h-20 bg-pink-200/50 rounded-full blur-2xl"></div>
        <div class="absolute bottom-10 left-10 w-32 h-32 bg-blue-200/50 rounded-full blur-2xl"></div>
      </div>
    </div>

    <!-- 分类系列 -->
    <div class="container mx-auto px-6 mb-16">
      <div class="flex justify-between items-end mb-6 px-2">
        <h2 class="text-2xl font-extrabold text-dark-900">甄选系列</h2>
        <a href="#" class="text-primary-500 text-sm font-bold hover:underline flex items-center gap-1 transition-colors">更多系列 <ArrowRight class="w-4 h-4"/></a>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 卡片 1 -->
        <div class="bg-[#FFEAA7] rounded-[2rem] p-8 flex justify-between items-center hover:shadow-lg transition-all cursor-pointer border border-white/30">
          <div>
            <div class="bg-white w-12 h-12 rounded-full flex items-center justify-center mb-5 shadow-sm text-primary-500">
               <Heart class="w-6 h-6 fill-current" />
            </div>
            <h3 class="text-2xl font-extrabold text-dark-900 mb-1">红桃粿家族</h3>
            <p class="text-dark-800 text-sm mb-6 font-medium">把福气带在身边</p>
            <button class="bg-white text-dark-900 px-6 py-2 rounded-full text-xs font-extrabold hover:bg-white/90 shadow-sm">进入</button>
          </div>
          <img src="https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20pink%20plush%20toy%20icon%203d&image_size=square" class="w-40 h-40 object-contain drop-shadow-lg" />
        </div>

        <!-- 卡片 2 -->
        <div class="bg-[#74B9FF]/30 rounded-[2rem] p-8 flex justify-between items-center hover:shadow-lg transition-all cursor-pointer border border-white/30">
          <div>
            <div class="bg-white w-12 h-12 rounded-full flex items-center justify-center mb-5 shadow-sm text-secondary-yellow">
               <Star class="w-6 h-6 fill-current" />
            </div>
            <h3 class="text-2xl font-extrabold text-dark-900 mb-1">做粿小工具</h3>
            <p class="text-dark-800 text-sm mb-6 font-medium">传统老手艺模具</p>
            <button class="bg-white text-dark-900 px-6 py-2 rounded-full text-xs font-extrabold hover:bg-white/90 shadow-sm">进入</button>
          </div>
          <img src="https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cute%20cartoon%20keychain%20icon%203d&image_size=square" class="w-40 h-40 object-contain drop-shadow-lg" />
        </div>
      </div>
    </div>

    <!-- 热销商品 -->
    <div class="container mx-auto px-6 mb-16">
      <div class="flex justify-between items-end mb-6 px-2">
        <h2 class="text-2xl font-extrabold text-dark-900">热门商品</h2>
        <a href="#" class="text-primary-500 text-sm font-bold hover:underline flex items-center gap-1 transition-colors">查看更多 <ArrowRight class="w-4 h-4"/></a>
      </div>
      
      <div v-if="loading" class="flex justify-center py-10">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-500"></div>
      </div>
      
      <div v-else-if="products.length === 0" class="text-center py-20 text-zinc-400">
        <p>暂无商品上架</p>
      </div>

      <div v-else class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-5 px-2">
        <div v-for="product in products" :key="product.id" class="bg-white rounded-[1.5rem] p-4 shadow-card hover:shadow-soft transition-all duration-300 group border border-white/50 flex flex-col h-full cursor-pointer" @click="goToDetail(product.id)">
          <div class="relative bg-zinc-50 rounded-2xl mb-4 aspect-square overflow-hidden">
             <div class="absolute top-3 left-3 bg-red-500 text-white px-2.5 py-1 rounded-full text-[10px] font-bold z-10 shadow-sm">热销</div>
             <img :src="product.imageUrl" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
          </div>
          <h3 class="font-bold text-dark-900 text-sm mb-1 truncate px-1" :title="product.name">{{ product.name }}</h3>
          <p class="text-xs text-zinc-400 mb-4 px-1 font-medium truncate">{{ product.category }}</p>
          <div class="flex items-center justify-between px-1 mt-auto mb-3">
            <span class="text-secondary-orange font-extrabold text-lg">¥{{ product.price }}</span>
          </div>
          
          <!-- 操作 -->
          <div class="grid grid-cols-3 gap-2 mt-auto" @click.stop>
             <button @click="toggleFavorite(product)" 
                     class="flex items-center justify-center p-2 rounded-xl border-2 transition-all duration-300"
                     :class="isFavorite(product.id) ? 'bg-rose-50 border-rose-500 text-rose-500 shadow-sm' : 'bg-white border-rose-200 text-rose-300 hover:border-rose-400 hover:text-rose-400'">
                <Heart class="w-5 h-5 transition-transform active:scale-90" :class="{ 'fill-current': isFavorite(product.id) }" />
             </button>
             <button @click="addToCart(product)" class="flex items-center justify-center p-2 rounded-xl bg-secondary-yellow text-white hover:bg-yellow-500 transition-colors shadow-sm">
                <ShoppingCart class="w-5 h-5" />
             </button>
             <button @click="handleBuyNow(product)" class="flex items-center justify-center p-2 rounded-xl bg-rose-500 text-white hover:bg-rose-600 transition-colors shadow-sm" title="立即购买">
                <Zap class="w-5 h-5 fill-current" />
             </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 限时优惠 -->
    <div class="container mx-auto px-6 mb-16">
      <div class="bg-gradient-to-r from-primary-400 to-primary-300 rounded-[2.5rem] p-8 md:p-10 flex items-center justify-between shadow-lg shadow-primary-200 text-white relative overflow-hidden">
        <div class="z-10">
          <div class="inline-block bg-white/20 px-3 py-1 rounded-full text-xs font-bold mb-3 border border-white/30">Limited Offer</div>
          <h2 class="text-3xl md:text-4xl font-black mb-2 tracking-tight">粿品尝鲜秒杀</h2>
          <p class="mb-8 opacity-95 font-medium text-lg">全场文创及食具满 ¥199 减 ¥50</p>
          <button class="bg-white text-primary-500 px-8 py-3 rounded-full font-bold hover:bg-white/90 transition-colors shadow-md">去抢购</button>
        </div>
        <div class="hidden md:block z-10 pr-8">
           <img src="https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=gift%20box%20cartoon%20star%20smiley%20face&image_size=square" class="w-40 h-40 object-contain drop-shadow-2xl" />
        </div>
        
        <!-- 背景形状 -->
        <div class="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
      </div>
    </div>


    <!-- 新品上市 -->
    <div class="container mx-auto px-6 mb-16">
      <div class="flex justify-between items-end mb-6 px-2">
        <h2 class="text-2xl font-extrabold text-dark-900">新品上架</h2>
        <a href="#" class="text-primary-500 text-sm font-bold hover:underline flex items-center gap-1 transition-colors">逛逛全部 <ArrowRight class="w-4 h-4"/></a>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
        <div v-for="product in products.slice(0, 4)" :key="product.id" class="bg-white rounded-[1.5rem] p-4 shadow-card hover:shadow-soft transition-all duration-300 group border border-white/50 flex flex-col h-full cursor-pointer" @click="goToDetail(product.id)">
          <div class="relative bg-zinc-50 rounded-2xl mb-4 aspect-square overflow-hidden">
             <div class="absolute top-3 left-3 bg-secondary-green text-white px-2.5 py-1 rounded-full text-[10px] font-bold z-10 shadow-sm">新品</div>
             <img :src="product.imageUrl" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
          </div>
          <h3 class="font-bold text-dark-900 text-sm mb-1 truncate px-1" :title="product.name">{{ product.name }}</h3>
          <p class="text-xs text-zinc-400 mb-4 px-1 font-medium truncate">{{ product.category }}</p>
          <div class="flex items-center justify-between px-1 mt-auto mb-3">
            <span class="text-secondary-orange font-extrabold text-lg">¥{{ product.price }}</span>
          </div>
          <!-- 操作 -->
          <div class="grid grid-cols-3 gap-2 mt-auto" @click.stop>
             <button @click="toggleFavorite(product)" 
                     class="flex items-center justify-center p-2 rounded-xl border-2 transition-all duration-300"
                     :class="isFavorite(product.id) ? 'bg-rose-50 border-rose-500 text-rose-500 shadow-sm' : 'bg-white border-rose-200 text-rose-300 hover:border-rose-400 hover:text-rose-400'">
                <Heart class="w-5 h-5 transition-transform active:scale-90" :class="{ 'fill-current': isFavorite(product.id) }" />
             </button>
             <button @click="addToCart(product)" class="flex items-center justify-center p-2 rounded-xl bg-secondary-yellow text-white hover:bg-yellow-500 transition-colors shadow-sm">
                <ShoppingCart class="w-5 h-5" />
             </button>
             <button @click="handleBuyNow(product)" class="flex items-center justify-center p-2 rounded-xl bg-rose-500 text-white hover:bg-rose-600 transition-colors shadow-sm" title="立即购买">
                <Zap class="w-5 h-5 fill-current" />
             </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 优势展示条 -->
    <div class="container mx-auto px-6 py-10 border-t border-white/50">
      <div class="flex flex-wrap justify-between gap-6">
        <div class="flex items-center gap-3 text-dark-500 text-xs font-bold bg-white/50 px-4 py-2 rounded-full">
          <Truck class="w-5 h-5 text-primary-500" /> 包邮（满额）
        </div>
        <div class="flex items-center gap-3 text-dark-500 text-xs font-bold bg-white/50 px-4 py-2 rounded-full">
          <Clock class="w-5 h-5 text-primary-500" /> 快速发货
        </div>
        <div class="flex items-center gap-3 text-dark-500 text-xs font-bold bg-white/50 px-4 py-2 rounded-full">
          <RefreshCcw class="w-5 h-5 text-primary-500" /> 7天无理由
        </div>
        <div class="flex items-center gap-3 text-dark-500 text-xs font-bold bg-white/50 px-4 py-2 rounded-full">
          <ShieldCheck class="w-5 h-5 text-primary-500" /> 售后保障
        </div>
      </div>
    </div>
  </div>
</template>
