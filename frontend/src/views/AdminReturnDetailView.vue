<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Package, MapPin, Truck, User, Phone, CheckCircle } from 'lucide-vue-next'
import { marketApi } from '@/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const orderId = parseInt(route.params.id as string)
const order = ref<any>(null)
const loading = ref(false)
const mapContainer = ref<HTMLElement | null>(null)
const mapInstance = ref<any>(null)

const fetchOrder = async () => {
  loading.value = true
  try {
    // 假设我们可以从 admin API 或 market API 通过 ID 获取订单
    // 因为 adminApi.getOrders() 返回全部，我们可能需要过滤或如果有 getOrderById
    // 如果没有直接的 ID 接口，目前使用 marketApi.getAllOrders 查找作为后备
    const orders = (await marketApi.getAllOrders()) as unknown as any[]
    order.value = orders.find((o: any) => o.id === orderId)
    
    if (order.value) {
      setTimeout(initMap, 500)
    }
  } catch (error) {
    console.error('Failed to fetch order:', error)
    toast.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const handleConfirmReturn = async () => {
  if (!confirm('确认已收到退货商品并无误吗？确认后将自动退款。')) return
  try {
    await marketApi.confirmReturnReceipt(orderId)
    toast.success('确认收货成功，退款已完成')
    router.back()
  } catch (error) {
    console.error('Confirm return failed:', error)
    toast.error('操作失败')
  }
}

const initMap = () => {
  if (typeof window === 'undefined') return
  
  ;(window as any)._AMapSecurityConfig = {
    securityJsCode: '258b0ed29bd0ef1adf0da47cdfab8fc6',
  }

  if (!(window as any).AMap) {
    const script = document.createElement('script')
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
  if (!mapContainer.value || !(window as any).AMap || !order.value) return

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

    // 在退货流程中：买家地址 (收货地址) 是起点 (发件人)，深圳 (卖家) 是终点 (收件人)
    const buyerAddress = order.value.shippingAddress || order.value.addressSnapshot;
    
    if (buyerAddress) {
      geocoder.getLocation(buyerAddress, (status: string, result: any) => {
        if (status === 'complete' && result.geocodes.length) {
          const lnglat = result.geocodes[0].location;
          const startPoint = [lnglat.lng, lnglat.lat]; // 买家是发件人
          const endPoint = [114.057868, 22.543099]; // 深圳是收件人
          
          // 添加起点城市标签 (买家/发件人)
          const startMarker = new AMap.Text({
            text: `${result.geocodes[0].addressComponent.city || result.geocodes[0].addressComponent.province} (发货地/买家)`,
            anchor: 'center',
            style: {
              'padding': '5px 10px',
              'background-color': '#F43F5E', // 买家用玫瑰色
              'border-radius': '4px',
              'border': '1px solid #E11D48',
              'color': 'white',
              'font-size': '12px',
              'font-weight': 'bold'
            },
            position: startPoint
          });
          startMarker.setMap(mapInstance.value);

          // 添加终点城市标签 (卖家/收件人)
          const endMarker = new AMap.Text({
            text: '深圳 (收货地/商家)',
            anchor: 'center',
            style: {
              'padding': '5px 10px',
              'background-color': '#3B82F6', // 卖家用蓝色
              'border-radius': '4px',
              'border': '1px solid #2563EB',
              'color': 'white',
              'font-size': '12px',
              'font-weight': 'bold'
            },
            position: endPoint
          });
          endMarker.setMap(mapInstance.value);

          // 绘制驾驶路线
          const driving = new AMap.Driving({
            map: mapInstance.value,
            hideMarkers: true,
            showTraffic: false,
            isOutline: true,
            outlineColor: '#ffeeff',
            autoFitView: true
          });

          driving.search(startPoint, endPoint, (status: string, result: any) => {
             if (status === 'complete') {
                // 路线已绘制
             } else {
               console.error('Driving route failed', result)
             }
          });

        } else {
          console.error('Geocoding failed');
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
  <div class="min-h-screen bg-[#FFF8E1] pb-20 font-sans">
    <!-- 头部 -->
    <div class="bg-white sticky top-0 z-40 border-b border-[#EFEBE9] px-6 py-4 flex items-center gap-4 shadow-sm">
      <button @click="router.back()" class="p-2 hover:bg-[#FFF3E0] rounded-full transition-colors text-[#8D6E63]">
        <ArrowLeft class="w-6 h-6" />
      </button>
      <h1 class="text-xl font-black text-[#3E2723]">退货物流详情</h1>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-rose-500"></div>
    </div>

    <div v-else-if="!order" class="flex flex-col items-center justify-center py-20 text-[#8D6E63]">
      <Package class="w-16 h-16 mb-4 opacity-50" />
      <p>未找到订单信息</p>
    </div>

    <div v-else class="container mx-auto max-w-4xl p-6 space-y-6">
      
      <!-- 物流信息卡片 -->
      <div class="bg-white rounded-3xl p-6 shadow-lg border border-[#EFEBE9] flex flex-col md:flex-row gap-6">
         <div class="flex-1 space-y-4">
            <div class="flex items-center gap-2 mb-2">
               <Truck class="w-6 h-6 text-blue-500" />
               <h2 class="text-lg font-bold text-[#3E2723]">物流信息</h2>
            </div>
            <div class="bg-blue-50 p-4 rounded-xl border border-blue-100 space-y-2">
               <div class="flex justify-between">
                  <span class="text-blue-400 text-sm font-bold">物流公司</span>
                  <span class="text-blue-800 font-bold">{{ order.returnLogisticsCompany || '未知' }}</span>
               </div>
               <div class="flex justify-between">
                  <span class="text-blue-400 text-sm font-bold">快递单号</span>
                  <span class="text-blue-800 font-mono font-bold">{{ order.returnTrackingNumber || '未知' }}</span>
               </div>
               <div class="pt-2 border-t border-blue-100 text-xs text-blue-500 leading-relaxed">
                  物流状态：{{ order.returnStatus === 'RECEIVED' ? '已签收' : '运输中' }}
               </div>
            </div>
         </div>
         
         <!-- 买家信息 -->
         <div class="flex-1 space-y-4">
            <div class="flex items-center gap-2 mb-2">
               <User class="w-6 h-6 text-rose-500" />
               <h2 class="text-lg font-bold text-[#3E2723]">买家信息</h2>
            </div>
            <div class="bg-[#FFF3E0] p-4 rounded-xl border border-rose-100 space-y-2">
               <div class="flex items-center gap-3 mb-2">
                  <img :src="order.user?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-10 h-10 rounded-full border border-white shadow-sm" />
                  <div>
                     <div class="font-bold text-[#3E2723]">{{ order.user?.nickname }}</div>
                     <div class="text-xs text-[#8D6E63]">@{{ order.user?.username }}</div>
                  </div>
               </div>
               <div class="flex items-center gap-2 text-sm text-[#5D4037]">
                  <Phone class="w-4 h-4 text-[#8D6E63]" />
                  <span>{{ order.contactPhone || '无联系电话' }}</span>
               </div>
               <div class="flex items-start gap-2 text-sm text-[#5D4037]">
                  <MapPin class="w-4 h-4 text-[#8D6E63] shrink-0 mt-0.5" />
                  <span class="line-clamp-2">{{ order.shippingAddress || '无地址信息' }}</span>
               </div>
            </div>
         </div>
      </div>

      <!-- 地图轨迹 -->
      <div class="bg-white rounded-3xl p-1 shadow-lg border border-[#EFEBE9] overflow-hidden">
         <div class="p-4 border-b border-[#EFEBE9] flex items-center gap-2">
            <MapPin class="w-5 h-5 text-green-600" />
            <span class="font-bold text-[#3E2723]">退货轨迹 (买家 -> 商家)</span>
         </div>
         <div class="h-96 w-full bg-zinc-100 relative" ref="mapContainer">
            <div v-if="!mapInstance" class="absolute inset-0 flex items-center justify-center text-zinc-400 text-sm">
               <div class="flex items-center gap-2">
                  <div class="animate-spin w-4 h-4 border-2 border-zinc-300 border-t-zinc-500 rounded-full"></div>
                  正在加载地图...
               </div>
            </div>
         </div>
      </div>

      <!-- 商品信息 -->
      <div class="bg-white rounded-3xl p-6 shadow-lg border border-[#EFEBE9]">
         <div class="flex items-center gap-2 mb-6 pb-4 border-b border-[#EFEBE9]">
           <Package class="w-5 h-5 text-rose-500" />
           <span class="font-bold text-[#3E2723]">退货商品</span>
         </div>
         
         <div v-if="order.items && order.items.length > 0" class="space-y-4">
            <div v-for="item in order.items" :key="item.id" class="flex gap-4">
              <div class="w-20 h-20 bg-[#FAFAFA] rounded-xl overflow-hidden shrink-0 border border-[#EFEBE9]">
                 <img :src="item.product.imageUrl" class="w-full h-full object-cover" />
              </div>
              <div class="flex-1 min-w-0">
                 <h3 class="font-bold text-[#3E2723] truncate mb-1">{{ item.product.name }}</h3>
                 <p class="text-xs text-[#8D6E63] mb-2">{{ item.product.category || '全部分类' }}</p>
                 <div class="flex justify-between items-center">
                    <span class="text-rose-500 font-black text-lg">¥{{ item.price }}</span>
                    <span class="text-[#8D6E63] text-sm font-bold">x{{ item.quantity }}</span>
                 </div>
              </div>
            </div>
         </div>
      </div>

      <!-- 操作栏 -->
      <div class="sticky bottom-6 z-30">
        <div class="bg-white/90 backdrop-blur rounded-2xl p-4 shadow-2xl border border-rose-100 flex items-center justify-between">
           <div class="text-sm font-bold text-[#8D6E63]">
              当前状态: 
              <span class="text-rose-500 ml-2">
                {{ order.returnStatus === 'RECEIVED' ? '已确认收货' : '等待确认收货' }}
              </span>
           </div>
           <button 
             v-if="order.returnStatus !== 'RECEIVED'"
             @click="handleConfirmReturn" 
             class="px-8 py-3 bg-rose-500 hover:bg-rose-600 text-white font-bold rounded-xl shadow-lg shadow-rose-200 transition-all flex items-center gap-2"
           >
             <CheckCircle class="w-5 h-5" /> 确认收到退货
           </button>
           <button 
             v-else
             disabled
             class="px-8 py-3 bg-green-500 text-white font-bold rounded-xl shadow-lg shadow-green-200 transition-all flex items-center gap-2 opacity-80 cursor-not-allowed"
           >
             <CheckCircle class="w-5 h-5" /> 已完成退货
           </button>
        </div>
      </div>

    </div>
  </div>
</template>
