<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Package, MapPin, Clock, Truck, X, AlertCircle, MessageCircle } from 'lucide-vue-next'
import { marketApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import ChatBox from '@/components/ChatBox.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const toast = useToast()

const orderId = parseInt(route.params.id as string)
const order = ref<any>(null)
const loading = ref(false)
const showChat = ref(false)
const mapContainer = ref<HTMLElement | null>(null)
const mapInstance = ref<any>(null)

// 退款状态
const showRefundModal = ref(false)
const selectedReason = ref('')
const selectedRefundType = ref('ONLY_REFUND')
const refundLoading = ref(false)
const refundReasons = ['质量问题', '商品描述不符', '少件/漏发', '包装破损', '拍错/多拍', '不想要了', '其他']
const refundTypes = [
  { value: 'ONLY_REFUND', label: '仅退款' },
  { value: 'RETURN_AND_REFUND', label: '退货退款' }
]

const returnLogisticsForm = ref({
  company: '',
  trackingNumber: ''
})
const returnLoading = ref(false)

const statusSteps = ['PENDING', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED']
const statusLabels: Record<string, string> = {
  PENDING: '已支付',
  PAID: '待发货',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const currentStepIndex = computed(() => {
  if (!order.value) return 0
  if (order.value.status === 'CANCELLED') return -1
  return statusSteps.indexOf(order.value.status)
})

const maskedAddress = computed(() => {
  const addr = order.value?.shippingAddress || order.value?.addressSnapshot || ''
  if (!addr || addr.length < 8) return addr
  // 保留前6个字符（通常是省/市/区）和后4个字符
  const start = addr.substring(0, 6)
  const end = addr.substring(addr.length - 4)
  return `${start}****${end}`
})

const canRefund = computed(() => {
  if (!order.value) return false
  // 如果已支付、已发货或已完成，并且尚未申请/退款，则允许退款
  const status = order.value.status
  const refundStatus = order.value.refundStatus
  return (['PAID', 'SHIPPED', 'COMPLETED'].includes(status)) && (!refundStatus || refundStatus === 'NONE')
})

const fetchOrder = async () => {
  if (!userStore.user?.id) return
  loading.value = true
  try {
    const orders = (await marketApi.getUserOrders(userStore.user.id)) as unknown as any[]
    order.value = orders.find((o: any) => o.id === orderId)
    
    // 自动使用订单ID填充运单号
    if (order.value) {
      returnLogisticsForm.value.trackingNumber = order.value.id.toString()
    }

    if (order.value && (order.value.status === 'SHIPPED' || order.value.status === 'COMPLETED')) {
      setTimeout(initMap, 500)
    }
  } catch (error) {
    console.error('Failed to fetch order:', error)
    toast.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const handleApplyRefund = async () => {
  if (!selectedReason.value) {
    toast.error('请选择退款理由')
    return
  }
  
  refundLoading.value = true
  try {
    await marketApi.applyRefund(orderId, selectedReason.value, selectedRefundType.value)
    toast.success('申请提交成功，请等待商家处理')
    showRefundModal.value = false
    fetchOrder() // 刷新订单以查看更新的状态
  } catch (error) {
    console.error('Apply refund failed:', error)
    toast.error('申请退款失败')
  } finally {
    refundLoading.value = false
  }
}

const handleSubmitReturn = async () => {
  if (!returnLogisticsForm.value.company || !returnLogisticsForm.value.trackingNumber) {
    toast.error('请填写完整物流信息')
    return
  }
  returnLoading.value = true
  try {
    await marketApi.submitReturnLogistics(orderId, returnLogisticsForm.value.company, returnLogisticsForm.value.trackingNumber)
    toast.success('寄件信息提交成功')
    fetchOrder()
  } catch (error) {
    console.error('Return logistics failed:', error)
    toast.error('提交失败')
  } finally {
    returnLoading.value = false
  }
}

const initMap = () => {
  if (typeof window === 'undefined') return
  
  ;(window as any)._AMapSecurityConfig = {
    securityJsCode: '258b0ed29bd0ef1adf0da47cdfab8fc6',
  }

  if (!(window as any).AMap) {
    const script = document.createElement('script')
    // 加载驾驶和地理编码插件
    script.src = 'https://webapi.amap.com/maps?v=2.0&key=f2c31c66dc5abf9d6c986a6308d9b3fd&plugin=AMap.Driving,AMap.Geocoder'
    script.async = true
    script.onload = () => {
      createMap()
    }
    document.head.appendChild(script)
  } else {
    createMap()
  }
}

const createMap = () => {
  if (!mapContainer.value || !(window as any).AMap) return

  try {
    const AMap = (window as any).AMap
    mapInstance.value = new AMap.Map(mapContainer.value, {
      zoom: 11,
      center: [114.057868, 22.543099], // 默认中心 (深圳)
      viewMode: '3D'
    })

    const geocoder = new AMap.Geocoder({
      city: "全国",
    });

    const address = order.value.shippingAddress || order.value.addressSnapshot;
    
    if (address) {
      geocoder.getLocation(address, (status: string, result: any) => {
        if (status === 'complete' && result.geocodes.length) {
          const lnglat = result.geocodes[0].location;
          const endPoint = [lnglat.lng, lnglat.lat];
          const startPoint = [114.057868, 22.543099]; // 深圳
          
          // 添加起点城市标签（深圳）
          const startMarker = new AMap.Text({
            text: '深圳 (发货地)',
            anchor: 'center',
            style: {
              'padding': '5px 10px',
              'background-color': '#3B82F6',
              'border-radius': '4px',
              'border': '1px solid #2563EB',
              'color': 'white',
              'font-size': '12px',
              'font-weight': 'bold'
            },
            position: startPoint
          });
          startMarker.setMap(mapInstance.value);

          // 添加终点城市标签（买家地址）
          const endMarker = new AMap.Text({
            text: `${result.geocodes[0].addressComponent.city || result.geocodes[0].addressComponent.province} (收货地)`,
            anchor: 'center',
            style: {
              'padding': '5px 10px',
              'background-color': '#F43F5E',
              'border-radius': '4px',
              'border': '1px solid #E11D48',
              'color': 'white',
              'font-size': '12px',
              'font-weight': 'bold'
            },
            position: endPoint
          });
          endMarker.setMap(mapInstance.value);

          if (order.value.status === 'SHIPPED') {
            // 绘制驾驶路线
            const driving = new AMap.Driving({
              map: mapInstance.value,
              hideMarkers: true, // 隐藏默认标记以使用自定义文本标记
              showTraffic: false,
              isOutline: true,
              outlineColor: '#ffeeff',
              autoFitView: true
            });

            driving.search(startPoint, endPoint, (status: string, result: any) => {
               if (status === 'complete') {
                  // 路线已绘制 automatically
               } else {
                 console.error('Driving route failed', result)
               }
            });
             
          } else if (order.value.status === 'COMPLETED') {
             // 在目的地显示带有“已签收”标签的标记
             const marker = new AMap.Marker({
               position: endPoint,
               map: mapInstance.value,
               title: '已签收'
             });
             
             marker.setLabel({
                offset: new AMap.Pixel(0, -30), 
                content: "<div style='padding:5px 10px; background-color: #10B981; color: white; border-radius: 4px; font-weight: bold;'>已签收</div>", 
                direction: 'center'
             });
             
             mapInstance.value.setCenter(endPoint);
             mapInstance.value.setZoom(15);
          }

        } else {
          console.error('Geocoding failed');
          // 如果地理编码失败的后备逻辑（例如使用默认地图）
        }
      });
    }

  } catch (e) {
    console.error('Map init error:', e)
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<template>
  <div class="min-h-screen bg-zinc-50 pb-20">
    <!-- 头部 -->
    <div class="bg-white sticky top-0 z-40 border-b border-zinc-100 px-4 py-4 flex items-center gap-4">
      <button @click="router.back()" class="p-2 hover:bg-zinc-100 rounded-full transition-colors text-zinc-600">
        <ArrowLeft class="w-6 h-6" />
      </button>
      <h1 class="text-xl font-black text-zinc-800">订单详情</h1>
      <div class="ml-auto">
        <button @click="showChat = true" class="flex items-center gap-2 px-4 py-2 bg-rose-500 text-white rounded-full text-sm font-bold shadow-lg shadow-rose-200 hover:bg-rose-600 hover:scale-105 transition-all active:scale-95">
          <MessageCircle class="w-5 h-5" /> 联系商家
        </button>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-rose-500"></div>
    </div>

    <div v-else-if="!order" class="flex flex-col items-center justify-center py-20 text-zinc-400">
      <Package class="w-16 h-16 mb-4 opacity-50" />
      <p>未找到订单信息</p>
    </div>

    <div v-else class="container mx-auto max-w-3xl p-4 space-y-6">
      <!-- 状态卡片 -->
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 overflow-hidden relative">
        <div class="flex items-center justify-between mb-8 relative z-10">
          <div>
            <div class="text-sm text-zinc-400 font-bold mb-1">订单状态</div>
            <div class="text-3xl font-black" :class="{
              'text-rose-500': order.status === 'PENDING',
              'text-blue-500': order.status === 'SHIPPED',
              'text-green-500': order.status === 'COMPLETED',
              'text-zinc-500': order.status === 'CANCELLED'
            }">{{ statusLabels[order.status] || order.status }}</div>
          </div>
          <div class="text-right">
             <div class="text-sm text-zinc-400 font-bold mb-1">订单编号</div>
             <div class="font-mono text-zinc-600 font-medium">#{{ order.id.toString().padStart(8, '0') }}</div>
          </div>
        </div>

        <!-- 进度条 -->
        <div v-if="order.status !== 'CANCELLED'" class="relative z-10">
          <div class="flex items-center justify-between text-xs font-bold text-zinc-400 mb-2 px-2">
            <span :class="{'text-rose-500': currentStepIndex >= 0}">已支付</span>
            <span :class="{'text-rose-500': currentStepIndex >= 1}">待发货</span>
            <span :class="{'text-rose-500': currentStepIndex >= 2}">已发货</span>
            <span :class="{'text-rose-500': currentStepIndex >= 3}">已完成</span>
          </div>
          <div class="h-2 bg-zinc-100 rounded-full overflow-hidden">
            <div class="h-full bg-rose-500 transition-all duration-500 ease-out" :style="{ width: `${(currentStepIndex / 3) * 100}%` }"></div>
          </div>
        </div>
        
        <!-- 退款状态信息 -->
        <div v-if="order.refundStatus && order.refundStatus !== 'NONE'" class="mt-4 p-3 bg-orange-50 rounded-xl border border-orange-100 relative z-10">
          <div class="flex items-start gap-3">
            <AlertCircle class="w-5 h-5 text-orange-500 shrink-0 mt-0.5" />
            <div class="flex-1">
              <div class="font-bold text-orange-800 flex items-center gap-2">
                <span>售后状态：{{ 
                  order.status === 'CANCELLED' ? '已退款/取消' :
                  order.refundStatus === 'REFUND_APPLIED' ? '售后中' : 
                  order.refundStatus === 'REFUND_APPROVED' ? (order.refundType === 'RETURN_AND_REFUND' ? '退货退款中' : '退款中') :
                  order.refundStatus === 'REFUND_SUCCESS' ? '退款成功' :
                  order.refundStatus === 'REFUND_REJECTED' ? '退款已拒绝' : '售后处理中' 
                }}</span>
                <span v-if="order.refundType" class="text-xs bg-white/50 px-2 py-0.5 rounded text-orange-600 border border-orange-200">
                  {{ order.refundType === 'RETURN_AND_REFUND' ? '退货退款' : '仅退款' }}
                </span>
              </div>
              <div class="text-sm text-orange-600 mt-1">退款原因：{{ order.refundReason }}</div>
              
              <!-- 退货物流表单 -->
              <div v-if="order.refundStatus === 'REFUND_APPROVED' && order.refundType === 'RETURN_AND_REFUND'" class="mt-4 pt-4 border-t border-orange-200/50">
                <div v-if="!order.returnStatus || order.returnStatus === 'WAITING_SHIPMENT'">
                   <h4 class="font-bold text-orange-800 mb-2 text-sm flex items-center gap-2">
                     <Truck class="w-4 h-4" /> 请填写退货物流信息
                   </h4>
                   <div class="space-y-3">
                     <select v-model="returnLogisticsForm.company" class="w-full px-3 py-2 text-sm border border-orange-200 rounded-lg focus:outline-none focus:border-orange-400 bg-white text-zinc-700">
                        <option value="" disabled>请选择物流公司</option>
                        <option value="顺丰速运">顺丰速运</option>
                        <option value="中通快递">中通快递</option>
                        <option value="圆通速递">圆通速递</option>
                        <option value="申通快递">申通快递</option>
                        <option value="韵达快递">韵达快递</option>
                        <option value="邮政EMS">邮政EMS</option>
                        <option value="京东物流">京东物流</option>
                        <option value="极兔速递">极兔速递</option>
                     </select>
                     <input v-model="returnLogisticsForm.trackingNumber" placeholder="快递单号" class="w-full px-3 py-2 text-sm border border-orange-200 rounded-lg focus:outline-none focus:border-orange-400 bg-white" />
                     <button 
                       @click="handleSubmitReturn" 
                       :disabled="returnLoading"
                       class="w-full py-2 bg-orange-500 hover:bg-orange-600 text-white font-bold rounded-lg text-sm transition-colors shadow-sm shadow-orange-200"
                     >
                       {{ returnLoading ? '提交中...' : '确认寄件' }}
                     </button>
                   </div>
                </div>
                <div v-else class="text-sm text-orange-700 bg-orange-100/50 p-3 rounded-lg">
                   <div class="font-bold mb-1 flex items-center gap-2">
                     <Truck class="w-4 h-4" /> 退货物流信息
                   </div>
                   <p>物流公司：{{ order.returnLogisticsCompany }}</p>
                   <p>快递单号：{{ order.returnTrackingNumber }}</p>
                   <div class="mt-2 font-bold" :class="order.returnStatus === 'RECEIVED' ? 'text-green-600' : 'text-blue-600'">
                     状态：{{ order.returnStatus === 'RECEIVED' ? '商家已收货' : '已寄出，等待商家收货' }}
                   </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 申请退款按钮 -->
        <div v-if="canRefund" class="mt-4 flex justify-end relative z-10">
          <button @click="showRefundModal = true" class="px-4 py-2 bg-zinc-100 hover:bg-zinc-200 text-zinc-700 font-bold rounded-full text-sm transition-colors">
            申请售后
          </button>
        </div>
        
        <!-- 背景装饰 -->
        <div class="absolute -right-10 -top-10 w-40 h-40 bg-zinc-50 rounded-full blur-3xl opacity-60"></div>
      </div>

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

      <!-- 物流地图 -->
      <div v-if="order.status === 'SHIPPED' || order.status === 'COMPLETED'" class="bg-white rounded-3xl p-1 shadow-sm border border-zinc-100 overflow-hidden">
         <div class="p-4 border-b border-zinc-50 flex items-center gap-2">
            <Truck class="w-5 h-5 text-blue-500" />
            <span class="font-bold text-zinc-800">物流动态</span>
         </div>
         <div class="h-64 w-full bg-zinc-100 relative" ref="mapContainer">
            <div v-if="!mapInstance" class="absolute inset-0 flex items-center justify-center text-zinc-400 text-sm">
               <div class="flex items-center gap-2">
                  <div class="animate-spin w-4 h-4 border-2 border-zinc-300 border-t-zinc-500 rounded-full"></div>
                  正在加载地图...
               </div>
            </div>
         </div>
         <div class="p-4 bg-blue-50/50 text-xs text-blue-600 font-medium leading-relaxed">
            <p v-if="order.status === 'SHIPPED'">您的包裹正在飞速赶往目的地，配送员：王师傅 138****8000</p>
            <p v-else>您的包裹已签收，感谢您的购买！</p>
         </div>
      </div>

      <!-- 地址 -->
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 flex items-start gap-4">
        <div class="bg-orange-100 p-3 rounded-full text-orange-500 mt-1">
          <MapPin class="w-5 h-5" />
        </div>
        <div>
          <div class="text-sm text-zinc-400 font-bold mb-1">收货信息</div>
          <div class="font-bold text-zinc-800 text-lg mb-1">{{ order.contactName || userStore.user?.username || '用户' }} <span class="text-zinc-400 text-sm font-normal ml-2">{{ order.contactPhone || '138****8888' }}</span></div>
          <p class="text-zinc-500 leading-relaxed">{{ maskedAddress }}</p>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100">
        <div class="flex items-center gap-2 mb-6 pb-4 border-b border-zinc-50">
          <Package class="w-5 h-5 text-rose-500" />
          <span class="font-bold text-zinc-800">商品清单</span>
        </div>
        
        <div v-if="order.items && order.items.length > 0" class="space-y-4">
           <div v-for="item in order.items" :key="item.id" class="flex gap-4">
             <div class="w-20 h-20 bg-zinc-50 rounded-xl overflow-hidden shrink-0 border border-zinc-100">
                <img :src="item.product.imageUrl" class="w-full h-full object-cover" />
             </div>
             <div class="flex-1 min-w-0">
                <h3 class="font-bold text-zinc-800 truncate mb-1">{{ item.product.name }}</h3>
                <p class="text-xs text-zinc-400 mb-2">{{ item.product.category || '全部分类' }}</p>
                <div class="flex justify-between items-center">
                   <span class="text-rose-500 font-black text-lg">¥{{ item.price }}</span>
                   <span class="text-zinc-400 text-sm font-bold">x{{ item.quantity }}</span>
                </div>
             </div>
           </div>
        </div>
        <div v-else class="text-center py-4 text-zinc-400 text-sm">
           暂无商品信息
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-zinc-100 space-y-3">
        <div class="flex justify-between text-sm">
          <span class="text-zinc-400">商品总额</span>
          <span class="font-bold text-zinc-800">¥{{ order.totalAmount }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-zinc-400">运费</span>
          <span class="font-bold text-zinc-800">¥0.00</span>
        </div>
        <div class="border-t border-dashed border-zinc-200 my-2 pt-2 flex justify-between items-center">
          <span class="font-bold text-zinc-800">实付款</span>
          <span class="text-2xl font-black text-rose-500">¥{{ order.totalAmount }}</span>
        </div>
        
        <div class="pt-4 space-y-2 text-xs text-zinc-400">
           <div class="flex justify-between">
              <span>下单时间</span>
              <span>{{ new Date(order.createdAt).toLocaleString('zh-CN') }}</span>
           </div>
           <div class="flex justify-between">
              <span>支付方式</span>
              <span>在线支付</span>
           </div>
        </div>
      </div>

    </div>

    <!-- 聊天弹窗 -->
    <div v-if="showChat" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-200">
      <div class="w-full max-w-md h-[600px] max-h-[90vh]">
         <ChatBox :order-id="order?.id" :order="order" @close="showChat = false" />
      </div>
    </div>
  </div>
</template>
