<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { X, Trash2, ShoppingBag, CheckCircle, Circle } from 'lucide-vue-next'
import { marketApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useToast } from '@/composables/useToast'
import PaymentModal from '@/components/PaymentModal.vue'

// 如果我们使用 store 控制可见性，则不需要 props
const userStore = useUserStore()
const cartStore = useCartStore()
const toast = useToast()

const loading = ref(false)
const processing = ref(false)
const selectedItemIds = ref<Set<number>>(new Set())
const showPaymentModal = ref(false)

// 打开抽屉时获取购物车数据
watch(() => cartStore.isOpen, async (isOpen) => {
  if (isOpen && userStore.user?.id) {
    loading.value = true
    await cartStore.fetchCart(userStore.user.id)
    loading.value = false
    // 默认全选？还是保留之前的选择？
    // 为了更好的用户体验，默认全选
    selectAll()
  }
})

const toggleSelection = (id: number) => {
  if (selectedItemIds.value.has(id)) {
    selectedItemIds.value.delete(id)
  } else {
    selectedItemIds.value.add(id)
  }
}

const selectAll = () => {
  cartStore.items.forEach(item => selectedItemIds.value.add(item.id))
}

const deselectAll = () => {
  selectedItemIds.value.clear()
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    deselectAll()
  } else {
    selectAll()
  }
}

const isAllSelected = computed(() => {
  return cartStore.items.length > 0 && selectedItemIds.value.size === cartStore.items.length
})

const totalAmount = computed(() => {
  return cartStore.items
    .filter(item => selectedItemIds.value.has(item.id))
    .reduce((sum, item) => sum + (item.product.price * item.quantity), 0)
})

const checkout = async () => {
  if (!userStore.user?.id || selectedItemIds.value.size === 0) return
  showPaymentModal.value = true
}

const onPaymentSuccess = async (paymentData: any) => {
  if (!userStore.user?.id || selectedItemIds.value.size === 0) return
  
  processing.value = true
  try {
    // 将选择的 ID 传递给后端
    await marketApi.createOrder({
      userId: userStore.user.id,
      cartItemIds: Array.from(selectedItemIds.value),
      addressId: paymentData.addressId,
      addressSnapshot: paymentData.addressSnapshot,
      contactName: paymentData.contactName,
      contactPhone: paymentData.contactPhone
    })
    toast.success('下单成功！')
    showPaymentModal.value = false
    await cartStore.fetchCart(userStore.user.id)
    cartStore.closeCart()
    // 清空选择
    selectedItemIds.value.clear()
  } catch (error: any) {
    console.error('Checkout failed:', error)
    if (error.response?.data?.message?.includes('address')) {
        toast.error('请先在个人中心添加收货地址')
    } else {
        toast.error('下单失败，请稍后重试')
    }
  } finally {
    processing.value = false
  }
}

const removeFromCart = async (id: number) => {
  if (!userStore.user?.id) return
  try {
    await cartStore.removeFromCart(id, userStore.user.id)
    selectedItemIds.value.delete(id)
    toast.info('商品已移出')
  } catch (error) {
    toast.error('移除失败')
  }
}
</script>

<template>
  <div v-if="cartStore.isOpen" class="fixed inset-0 z-50 flex justify-end">
    <!-- 背景遮罩 -->
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm transition-opacity" @click="cartStore.closeCart()"></div>
    
    <PaymentModal 
      :is-open="showPaymentModal" 
      :amount="totalAmount"
      @close="showPaymentModal = false"
      @success="onPaymentSuccess"
    />

    <!-- 抽屉 -->
    <div class="relative w-full max-w-md bg-white h-full shadow-2xl flex flex-col animate-slide-in">
      <div class="p-6 border-b border-zinc-100 flex items-center justify-between bg-zinc-50">
        <h2 class="text-xl font-bold text-zinc-900 flex items-center gap-2">
          <ShoppingBag class="w-5 h-5 text-rose-500" /> 购物车
        </h2>
        <button @click="cartStore.closeCart()" class="p-2 hover:bg-zinc-200 rounded-full transition-colors">
          <X class="w-5 h-5 text-zinc-500" />
        </button>
      </div>
      
      <div class="flex-1 overflow-y-auto p-6 space-y-6">
        <!-- 购物车商品 -->
        <div>
          <div class="flex items-center justify-between mb-4">
             <h3 class="font-bold text-zinc-800 flex items-center gap-2">
               <span class="w-1.5 h-4 bg-rose-500 rounded-full"></span> 商品清单
             </h3>
             <button @click="toggleSelectAll" class="text-sm font-bold text-zinc-500 hover:text-rose-500 flex items-center gap-1">
                <CheckCircle v-if="isAllSelected" class="w-4 h-4 text-rose-500" />
                <Circle v-else class="w-4 h-4" />
                全选
             </button>
          </div>
          
          <div v-if="loading" class="flex justify-center py-10">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-rose-500"></div>
          </div>
          
          <div v-else-if="cartStore.items.length === 0" class="text-center py-10 text-zinc-400 bg-zinc-50 rounded-xl border border-zinc-100 border-dashed">
            <ShoppingBag class="w-12 h-12 mx-auto mb-2 opacity-20" />
            <p>购物车还是空的哦</p>
          </div>
          
          <div v-else class="space-y-3">
            <div v-for="item in cartStore.items" :key="item.id" class="flex gap-3 bg-white p-3 rounded-xl border border-zinc-100 shadow-sm hover:shadow-md transition-shadow items-center">
              <!-- 复选框 -->
              <button @click="toggleSelection(item.id)" class="shrink-0 text-zinc-300 hover:text-rose-500 transition-colors">
                 <CheckCircle v-if="selectedItemIds.has(item.id)" class="w-5 h-5 text-rose-500 fill-rose-50" />
                 <Circle v-else class="w-5 h-5" />
              </button>

              <img :src="item.product.imageUrl" class="w-20 h-20 object-cover rounded-lg bg-zinc-100" />
              <div class="flex-1 flex flex-col justify-between self-stretch">
                <div>
                  <h3 class="font-bold text-zinc-900 line-clamp-1">{{ item.product.name }}</h3>
                  <p class="text-xs text-zinc-500 mt-1 bg-zinc-100 inline-block px-2 py-0.5 rounded">{{ item.product.category }}</p>
                </div>
                <div class="flex items-center justify-between mt-2">
                  <span class="text-rose-500 font-extrabold">¥{{ item.product.price }} <span class="text-zinc-400 text-xs font-normal">x {{ item.quantity }}</span></span>
                  <button @click="removeFromCart(item.id)" class="text-zinc-400 hover:text-red-500 p-1.5 hover:bg-red-50 rounded-full transition-colors">
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="p-6 border-t border-zinc-100 bg-white shadow-[0_-4px_20px_rgba(0,0,0,0.05)] z-10">
        <div class="flex justify-between items-end mb-4">
          <span class="text-zinc-500 font-medium text-sm">已选 {{ selectedItemIds.size }} 件，合计</span>
          <span class="text-3xl font-black text-rose-500 leading-none">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <button 
          @click="checkout" 
          :disabled="selectedItemIds.size === 0 || processing"
          class="w-full py-3.5 bg-gradient-to-r from-rose-500 to-orange-500 text-white rounded-xl font-bold hover:from-rose-600 hover:to-orange-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg shadow-rose-200 flex items-center justify-center gap-2 transform active:scale-[0.98]"
        >
          <span v-if="processing" class="animate-spin w-5 h-5 border-2 border-white/30 border-t-white rounded-full"></span>
          <span v-else>立即结算</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-slide-in {
  animation: slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}
</style>
