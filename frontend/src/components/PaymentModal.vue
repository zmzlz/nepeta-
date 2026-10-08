<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { X, CheckCircle2, ScanLine, Loader2, MapPin, ChevronRight, Plus } from 'lucide-vue-next'
import QrcodeVue from 'qrcode.vue'
import { addressApi } from '@/api'
import { useUserStore } from '@/stores/user'
import AddressModal from '@/components/AddressModal.vue'

const props = defineProps<{
  isOpen: boolean
  amount: number
}>()

const emit = defineEmits(['close', 'success'])
const userStore = useUserStore()

const step = ref('address') // 地址，扫码，处理中，成功
const loading = ref(false)
const addresses = ref<any[]>([])
const selectedAddress = ref<any>(null)
const showAddressModal = ref(false)

const loadAddresses = async () => {
  if (!userStore.user?.id) return
  try {
    const res: any = await addressApi.getUserAddresses(userStore.user.id)
    addresses.value = Array.isArray(res) ? res : []
    if (addresses.value.length > 0) {
      // 默认选择第一个标记为默认的，或直接选第一个
      selectedAddress.value = addresses.value.find((a: any) => a.isDefault) || addresses.value[0]
    }
  } catch (e) {
    console.error(e)
  }
}

watch(() => props.isOpen, (val) => {
  if (val) {
    step.value = 'address'
    loading.value = false
    loadAddresses()
  }
})

const handleAddressConfirm = () => {
  if (!selectedAddress.value) return
  step.value = 'scan'
}

const handlePaid = () => {
  loading.value = true
  step.value = 'processing'
  
  // 模拟支付处理
  setTimeout(() => {
    loading.value = false
    step.value = 'success'
    setTimeout(() => {
      // 携带地址信息触发成功事件
      emit('success', {
        addressId: selectedAddress.value.id,
        addressSnapshot: `${selectedAddress.value.province}${selectedAddress.value.city}${selectedAddress.value.district}${selectedAddress.value.detailAddress}`,
        contactName: selectedAddress.value.recipientName,
        contactPhone: selectedAddress.value.phone
      })
    }, 1500)
  }, 2000)
}

const onAddressAdded = () => {
  showAddressModal.value = false
  loadAddresses()
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-[70] flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>
    
    <div class="bg-white rounded-[2rem] w-full max-w-sm shadow-2xl relative overflow-hidden animate-in zoom-in duration-200">
      <div class="p-6 text-center relative">
        <button @click="$emit('close')" class="absolute top-4 right-4 p-2 hover:bg-zinc-100 rounded-full transition-colors text-zinc-400 z-10">
          <X class="w-5 h-5" />
        </button>
        
        <!-- 地址选择步骤 -->
        <div v-if="step === 'address'" class="space-y-6 pt-2 text-left">
           <h3 class="text-xl font-black text-zinc-800 text-center">确认收货地址</h3>
           
           <div v-if="addresses.length === 0" class="text-center py-8">
              <p class="text-zinc-400 mb-4">暂无收货地址</p>
              <button @click="showAddressModal = true" class="px-6 py-2 bg-zinc-100 text-zinc-600 rounded-xl font-bold hover:bg-zinc-200 transition-colors flex items-center gap-2 mx-auto">
                 <Plus class="w-4 h-4" /> 添加地址
              </button>
           </div>
           
           <div v-else class="space-y-3 max-h-60 overflow-y-auto pr-1 custom-scrollbar">
              <div 
                v-for="addr in addresses" 
                :key="addr.id"
                @click="selectedAddress = addr"
                :class="['p-4 rounded-2xl border cursor-pointer transition-all relative', selectedAddress?.id === addr.id ? 'border-rose-500 bg-rose-50/50' : 'border-zinc-100 hover:border-zinc-300']"
              >
                 <div class="flex items-center gap-2 mb-1">
                    <span class="font-bold text-zinc-800">{{ addr.recipientName }}</span>
                    <span class="text-xs text-zinc-500">{{ addr.phone }}</span>
                    <span v-if="addr.isDefault" class="bg-rose-100 text-rose-600 text-[10px] px-1.5 py-0.5 rounded font-bold">默认</span>
                 </div>
                 <p class="text-xs text-zinc-500 leading-relaxed line-clamp-2">
                    {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                 </p>
                 <div v-if="selectedAddress?.id === addr.id" class="absolute top-4 right-4 text-rose-500">
                    <CheckCircle2 class="w-5 h-5 fill-current" />
                 </div>
              </div>
              <button @click="showAddressModal = true" class="w-full py-2 text-xs text-zinc-400 font-bold hover:text-zinc-600 flex items-center justify-center gap-1">
                 <Plus class="w-3 h-3" /> 使用新地址
              </button>
           </div>
           
           <button 
             @click="handleAddressConfirm" 
             :disabled="!selectedAddress"
             class="w-full py-3 bg-rose-500 text-white rounded-xl font-bold hover:bg-rose-600 shadow-lg shadow-rose-200 transition-all active:scale-95 disabled:opacity-50 disabled:active:scale-100 mt-4"
           >
             确认支付 ¥{{ amount.toFixed(2) }}
           </button>
        </div>

        <!-- 扫码步骤 -->
        <div v-else-if="step === 'scan'" class="space-y-6 pt-4">
          <div>
            <h3 class="text-xl font-black text-zinc-800 mb-1">扫码支付</h3>
            <p class="text-zinc-500 text-sm font-medium">请使用手机扫一扫完成支付</p>
          </div>
          
          <div class="bg-zinc-50 p-6 rounded-2xl inline-block border border-zinc-100 shadow-inner relative group">
            <qrcode-vue :value="`pay://trae-shop/${amount}`" :size="180" level="H" class="rounded-lg opacity-90 group-hover:opacity-100 transition-opacity" />
            <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
               <ScanLine class="w-12 h-12 text-rose-500 animate-pulse" />
            </div>
          </div>
          
          <div class="text-3xl font-black text-rose-500 font-mono">
            <span class="text-lg">¥</span>{{ amount.toFixed(2) }}
          </div>
          
          <button @click="handlePaid" class="w-full py-3 bg-rose-500 text-white rounded-xl font-bold hover:bg-rose-600 shadow-lg shadow-rose-200 transition-all active:scale-95">
            我已支付
          </button>
        </div>
        
        <div v-else-if="step === 'processing'" class="py-12 space-y-4">
           <Loader2 class="w-12 h-12 text-rose-500 animate-spin mx-auto" />
           <p class="font-bold text-zinc-600">正在确认支付结果...</p>
        </div>
        
        <div v-else class="py-10 space-y-4">
           <div class="w-16 h-16 bg-green-100 text-green-500 rounded-full flex items-center justify-center mx-auto mb-4 animate-in zoom-in">
              <CheckCircle2 class="w-8 h-8" />
           </div>
           <h3 class="text-2xl font-black text-zinc-800">支付成功!</h3>
           <p class="text-zinc-500 font-medium">商家已收到您的订单</p>
        </div>
        
      </div>
      
      <!-- 底部装饰 -->
      <div class="h-2 bg-gradient-to-r from-rose-400 via-orange-400 to-yellow-400"></div>
    </div>
    
    <!-- 嵌套地址弹窗 -->
    <AddressModal 
      :is-open="showAddressModal" 
      @close="showAddressModal = false"
      @refresh="onAddressAdded"
    />
  </div>
</template>
