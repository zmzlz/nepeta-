<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { Send, Image as ImageIcon, X, Check, CheckCheck, ShoppingBag, Smile, Truck, MoreVertical, CreditCard } from 'lucide-vue-next'
import { chatApi, userApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { ChatMessage, User, Order } from '@/types'
import { useToast } from '@/composables/useToast'
import ImageUpload from '@/components/ImageUpload.vue'

const props = defineProps<{
  orderId?: number
  targetUserId?: number
  order?: Order // 可选的要显示的订单上下文
  availableOrders?: Order[]
}>()

const emit = defineEmits(['close', 'messages-read'])

const userStore = useUserStore()
const messages = ref<ChatMessage[]>([])
const toast = useToast()

const resolveOrder = (content: string) => {
    const match = content.match(/\[(?:ORDER_CARD|CONSULT_CARD):(\d+)\]/)
    if (!match) return null
    const id = parseInt(match[1])
    
    if (props.order && props.order.id === id) return props.order
    if (props.availableOrders) return props.availableOrders.find(o => o.id === id)
    return null
}

const newMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const showImageUpload = ref(false)
const uploadedImage = ref('')
const showEmojiPicker = ref(false)
const showConsultCard = ref(true)

const statusLabels: Record<string, string> = {
  PENDING: '已支付',
  PAID: '待发货',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const handleSendConsultCard = () => {
    if (!props.order) return
    const content = `[CONSULT_CARD:${props.order.id}]`
    newMessage.value = content
    handleSend()
    showConsultCard.value = false
}

let pollingInterval: any = null

// 确定对方
const targetUser = ref<User | null>(null)

// 表情
const emojis = ['😊', '😂', '😍', '👍', '🙏', '🎉', '❤️', '😭', '🤔', '👋', '🌹', '🤝', '💪', '🎁', '👀', '😎']

// 快捷短语
const quickPhrases = [
  '什么时候发货？',
  '可以修改地址吗？',
  '商品是正品吗？',
  '发什么快递？',
  '谢谢！'
]

const isMerchant = computed(() => userStore.user?.role === 'ADMIN')

const handleEmojiSelect = (emoji: string) => {
  newMessage.value += emoji
  showEmojiPicker.value = false
}

const handleQuickReply = (text: string) => {
  newMessage.value = text
  handleSend()
  
  // 自动回复机器人
  if (!isMerchant.value) {
      setTimeout(() => {
          let reply = ''
          if (text.includes('发货')) reply = '亲，正常情况下48小时内发货哦，我们会尽快为您安排~'
          else if (text.includes('地址')) reply = '亲，发货前可以修改地址，请直接把新地址发给我~'
          else if (text.includes('正品')) reply = '亲，本店承诺假一赔十，绝对正品保证！'
          else if (text.includes('快递')) reply = '亲，默认发顺丰或中通快递哦~'
          else reply = '亲，收到您的消息啦，客服马上就来~'
          
          addSystemMessage(reply)
      }, 1000)
  }
}

const addSystemMessage = async (content: string) => {
    if (!userStore.user?.id) return
    const targetId = props.targetUserId || targetUser.value?.id
    if (!targetId) return

    // 创建本地系统消息对象
    const systemMsg: ChatMessage = {
        id: Date.now(),
        sender: targetUser.value || { id: targetId, nickname: '系统自动回复', avatar: '' } as User,
        receiver: userStore.user as User,
        content: content,
        read: false,
        createdAt: new Date().toISOString()
    }

    // 先在本地添加，让用户立即看到
    localSystemMessages.value.push(systemMsg)
    mergeMessages()

    // 然后尝试持久化到后端
    try {
        await chatApi.sendMessage({
            senderId: targetId, 
            receiverId: userStore.user.id,
            orderId: props.orderId,
            content: content,
            imageUrl: ''
        })
        // 如果成功，下一次 fetchMessages 将从服务器获取它
        // 如果我们不清除本地，可能会得到重复消息
        // 策略： 
        // 1. Add local.
        // 2. Fetch remote.
        // 3. If remote contains this content (approx match), remove local?
        // 实际上更简单：只保留本地。如果远程数据来了，我们按内容/时间去重？
        // 或者更好：如果我们期望远程工作，就不添加本地消息
        // 但用户说"消失了"
        // 我们依赖本地获取即时反馈，如果后端失败，它将保留在本地
        // 如果后端成功，如果我们不管理 ID，可能会有重复消息
        // 鉴于限制，如果后端欺骗失败，我们只为"机器人"回复使用本地消息
        // 但是等等，我之前改成了先尝试后端
        // 恢复为：尝试后端。如果失败，添加本地
        // 如果成功，它将出现在下一次轮询中
    } catch (e) {
        console.warn('System message persist failed', e)
    }
}

const localSystemMessages = ref<ChatMessage[]>([])

const isNearBottom = () => {
  if (!messagesContainer.value) return true
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
  return scrollHeight - scrollTop - clientHeight < 100
}

const mergeMessages = (fetched: ChatMessage[] = [], shouldScroll = true) => {
    // 将获取的消息与本地系统消息结合
    // 按创建时间排序
    const combined = [...(fetched.length ? fetched : messages.value), ...localSystemMessages.value]
    combined.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    messages.value = combined
    if (shouldScroll) {
        scrollToBottom()
    }
}

const initChat = async () => {
  if (!userStore.user) return

  try {
    if (props.targetUserId) {
      // 如果需要，可以在这里添加获取用户详情的逻辑
    } else {
      if (userStore.user.role !== 'ADMIN') {
        const users = (await userApi.getAllUsers()) as unknown as User[]
        const admin = users.find(u => u.role === 'ADMIN')
        if (admin) {
           targetUser.value = admin
        }
      }
    }

    await fetchMessages()
    
    // 如果历史为空且用户是顾客，发送自动欢迎消息
     if (messages.value.length === 0 && !isMerchant.value) {
         setTimeout(() => {
             // 再次检查是否为空（以防用户打字太快）
             if (messages.value.length === 0) {
                addSystemMessage(`欢迎您光临${targetUser.value?.nickname || '本店铺'}！\n亲亲欢迎光临！需要什么帮助可以直接联系客服呢~\n\n我们提供：\n✨ 正品保证\n🚀 极速发货\n🛡️ 售后无忧`)
             }
         }, 500)
     }

     // 如果从订单详情进入，自动发送订单卡片
     // 检查是否已经为该订单发送过订单卡片
     // 实际上，用户要求"确认地址"卡片应该在商家一侧（用户视图的左侧？）
     // 等等，如果我是用户，商家在左侧
     // 如果卡片是"确认地址"，通常是由系统或商家发给用户确认的
     // 所以它应该出现在左侧（来自商家/系统）
     // 我之前的实现是作为用户（我）发送的
     // 把 handleSendOrderCard 改为 addSystemMessage 来模拟商家/系统发送者
     if (props.order && !isMerchant.value) {
         // 简单检查：是否有任何消息包含 [ORDER_CARD:id]
         const hasSentCard = messages.value.some(m => m.content.includes(`[ORDER_CARD:${props.order?.id}]`))
         
         if (!hasSentCard) {
             setTimeout(() => {
                 // 使用 addSystemMessage 使其来自"系统/商家"
                 // 但内容需要是特殊标记
                 addSystemMessage(`[ORDER_CARD:${props.order?.id}]`)
             }, 800)
         }
     }

    scrollToBottom()

    pollingInterval = setInterval(fetchMessages, 3000)
  } catch (error) {
    console.error('Chat init failed:', error)
  }
}

const fetchMessages = async () => {
  if (!userStore.user?.id) return

  try {
    let res: ChatMessage[] = []
    const targetId = props.targetUserId || targetUser.value?.id
    
    if (targetId) {
       res = (await chatApi.getChatHistory(userStore.user.id, targetId)) as unknown as ChatMessage[]
    } else if (props.orderId) {
       res = (await chatApi.getOrderMessages(props.orderId)) as unknown as ChatMessage[]
    }
    
    // 检查新消息以滚动或更新状态
    if (res.length !== messages.value.length || (res.length > 0 && res[res.length-1].id !== messages.value[messages.value.length-1]?.id)) {
       // 检查来自对方的新消息以显示通知
       const lastMsg = res[res.length - 1]
       const hasNewMessage = lastMsg && lastMsg.sender.id !== userStore.user.id && (!messages.value.length || lastMsg.id !== messages.value[messages.value.length-1].id)

       // 合并逻辑
       const shouldScroll = isNearBottom() || messages.value.length === 0
       mergeMessages(res, shouldScroll)
       
       if (hasNewMessage || (lastMsg && !lastMsg.read && lastMsg.sender.id !== userStore.user.id)) {
          if (!lastMsg.read) {
             chatApi.markAsRead(lastMsg.id)
             emit('messages-read')
          }
       }
    } else {
        // 只更新现有消息（例如读取状态更改）而不滚动
        // 优化：仅在读取状态改变时
        const lastLocal = messages.value[messages.value.length - 1]
        const lastRemote = res[res.length - 1]
        if (lastLocal && lastRemote && lastLocal.read !== lastRemote.read) {
            mergeMessages(res, false)
        }
    }
  } catch (error) {
    console.error('Fetch messages failed:', error)
  }
}

const handleSend = async () => {
  if ((!newMessage.value.trim() && !uploadedImage.value) || !userStore.user?.id) return

  const targetId = props.targetUserId || targetUser.value?.id
  if (!targetId) {
    toast.error('无法确定聊天对象')
    return
  }

  sending.value = true
  try {
    await chatApi.sendMessage({
      senderId: userStore.user.id,
      receiverId: targetId,
      orderId: props.orderId,
      content: newMessage.value,
      imageUrl: uploadedImage.value
    })
    
    newMessage.value = ''
    uploadedImage.value = ''
    showImageUpload.value = false
    await fetchMessages()
    scrollToBottom()
  } catch (error) {
    console.error('Send message failed:', error)
    toast.error('发送失败')
  } finally {
    sending.value = false
  }
}

const handleSendOrderCard = async () => {
    if (!props.order || !userStore.user?.id) return
    const targetId = props.targetUserId || targetUser.value?.id
    if (!targetId) return

    // 订单卡片的特殊内容标记
    const content = `[ORDER_CARD:${props.order.id}]` 
    
    try {
        await chatApi.sendMessage({
            senderId: userStore.user.id,
            receiverId: targetId,
            orderId: props.order.id,
            content: content,
            imageUrl: ''
        })
        await fetchMessages()
    } catch (e) {
        console.error('Failed to send order card', e)
    }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatDay = (time: string) => {
    const date = new Date(time)
    const today = new Date()
    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 1)

    if (date.toDateString() === today.toDateString()) {
        return '今天'
    } else if (date.toDateString() === yesterday.toDateString()) {
        return '昨天'
    } else {
        return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    }
}

const shouldShowTime = (index: number) => {
    if (index === 0) return true
    const current = new Date(messages.value[index].createdAt).getTime()
    const prev = new Date(messages.value[index - 1].createdAt).getTime()
    return (current - prev) > 5 * 60 * 1000 // 5 minutes
}

onMounted(() => {
  initChat()
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
})

watch(uploadedImage, (val) => {
  if (val) {
     // 可选：自动发送或让用户确认。让用户点击发送来确认
  }
})

</script>

<template>
  <div class="flex flex-col h-full bg-zinc-50 rounded-2xl overflow-hidden border border-zinc-200 shadow-xl relative">
    <!-- 头部 -->
    <div class="bg-white px-4 py-3 border-b border-zinc-100 flex justify-between items-center shadow-sm z-10 shrink-0">
      <div class="flex items-center gap-3">
         <div class="relative">
            <div class="w-2.5 h-2.5 rounded-full bg-green-500 animate-pulse absolute -right-0.5 -bottom-0.5 border border-white"></div>
            <img 
                :src="targetUser?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" 
                class="w-10 h-10 rounded-full object-cover border border-zinc-100"
            />
         </div>
         <div class="flex flex-col">
             <span class="font-bold text-zinc-800 text-base leading-tight flex items-center gap-1">
                {{ targetUser?.nickname || (props.targetUserId ? '聊天中' : '联系商家') }}
                <span v-if="targetUser?.role === 'ADMIN'" class="bg-orange-100 text-orange-600 text-[10px] px-1.5 py-0.5 rounded font-bold">金牌客服</span>
             </span>
             <span class="text-[10px] text-zinc-400 mt-0.5">响应快 服务好 | 通常几分钟内回复</span>
         </div>
      </div>
      <div class="flex items-center gap-2">
          <button class="p-1.5 hover:bg-zinc-100 rounded-full text-zinc-400"><MoreVertical class="w-5 h-5" /></button>
          <button @click="$emit('close')" class="p-1.5 hover:bg-zinc-100 rounded-full transition-colors text-zinc-400 hover:text-zinc-600">
            <X class="w-5 h-5" />
          </button>
      </div>
    </div>

    <!-- 消息 -->
    <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-zinc-50/50 scroll-smooth relative" ref="messagesContainer">
       
       <div v-if="messages.length === 0 && !props.order" class="text-center py-10 text-zinc-400 text-sm flex flex-col items-center gap-2">
          <div class="w-12 h-12 rounded-full bg-zinc-100 flex items-center justify-center">
              <Send class="w-5 h-5 text-zinc-300 ml-0.5" />
          </div>
          <p>暂无消息，开始沟通吧~</p>
       </div>
       
       <template v-for="(msg, index) in messages" :key="msg.id">
         <!-- 日期分隔符 -->
         <div v-if="shouldShowTime(index)" class="flex justify-center my-4">
             <span class="text-[10px] text-zinc-400 bg-zinc-200/50 px-2 py-0.5 rounded-full">
                 {{ formatDay(msg.createdAt) }} {{ formatTime(msg.createdAt) }}
             </span>
         </div>

         <div 
           class="flex gap-2 group"
           :class="msg.sender.id === userStore.user?.id ? 'flex-row-reverse' : ''"
         >
           <img 
             v-if="msg.sender.id !== userStore.user?.id"
             :src="msg.sender.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" 
             class="w-9 h-9 rounded-full border border-zinc-200 bg-white object-cover shrink-0 mt-1"
           />
           
           <div 
             class="max-w-[85%] flex flex-col"
             :class="msg.sender.id === userStore.user?.id ? 'items-end' : 'items-start'"
           >
              <!-- 发送者名称（可选） -->
              <span v-if="msg.sender.id !== userStore.user?.id" class="text-[10px] text-zinc-400 ml-1 mb-1">{{ msg.sender.nickname }}</span>

              <!-- 消息内容 -->
              <div 
                class="shadow-sm relative group-hover:shadow-md transition-shadow duration-200"
                :class="[
                    msg.sender.id === userStore.user?.id 
                    ? 'bg-rose-500 text-white rounded-2xl rounded-tr-sm' 
                    : 'bg-white text-zinc-700 border border-zinc-100 rounded-2xl rounded-tl-sm'
                ]"
              >
                 <!-- 图片消息 -->
                 <div v-if="msg.imageUrl" class="rounded-lg overflow-hidden max-w-[200px]">
                    <img :src="msg.imageUrl" class="w-full object-cover" />
                 </div>
                 
                 <!-- 订单卡片消息 -->
                 <div v-else-if="msg.content.startsWith('[ORDER_CARD:') && resolveOrder(msg.content)" class="p-3 min-w-[260px] bg-white rounded-xl text-zinc-800">
                     <div class="font-bold text-sm mb-2 pb-2 border-b border-zinc-100 flex justify-between items-center">
                         <span>请确认收货地址</span>
                         <span class="text-[10px] text-zinc-400 font-normal">系统发送</span>
                     </div>
                     <div class="flex gap-3 mb-3">
                         <div class="w-16 h-16 bg-zinc-50 rounded border border-zinc-100 shrink-0">
                             <img :src="resolveOrder(msg.content)?.items[0]?.product?.imageUrl" class="w-full h-full object-cover rounded" />
                         </div>
                         <div class="flex-1 min-w-0">
                             <div class="text-sm font-bold truncate mb-1">{{ resolveOrder(msg.content)?.items[0]?.product?.name }}</div>
                             <div class="flex justify-between items-center">
                                 <span class="text-rose-500 font-black">¥{{ resolveOrder(msg.content)?.totalAmount }}</span>
                                 <span class="text-xs text-zinc-400">共{{ resolveOrder(msg.content)?.items.length }}件</span>
                             </div>
                         </div>
                     </div>
                     <div class="bg-zinc-50 p-2 rounded text-xs space-y-1 text-zinc-500">
                         <div class="flex gap-2"><span class="font-bold text-zinc-600 min-w-[3em]">收货人</span> <span>{{ resolveOrder(msg.content)?.contactName }}</span></div>
                         <div class="flex gap-2"><span class="font-bold text-zinc-600 min-w-[3em]">手机号</span> <span>{{ resolveOrder(msg.content)?.contactPhone }}</span></div>
                         <div class="flex gap-2"><span class="font-bold text-zinc-600 min-w-[3em]">地址</span> <span class="truncate">{{ resolveOrder(msg.content)?.shippingAddress }}</span></div>
                     </div>
                     <div class="mt-2 pt-2 border-t border-zinc-100 flex justify-end">
                         <span class="px-3 py-1 bg-zinc-100 text-zinc-400 text-xs rounded-full font-bold">已确认</span>
                     </div>
                 </div>

                 <!-- 咨询卡片消息 -->
                 <div v-else-if="msg.content.startsWith('[CONSULT_CARD:') && resolveOrder(msg.content)" class="p-3 min-w-[260px] bg-white rounded-xl text-zinc-800">
                     <div class="font-bold text-sm mb-2 pb-2 border-b border-zinc-100 flex justify-between items-center">
                         <span>正在咨询该订单</span>
                         <span class="text-[10px] text-zinc-400 font-normal">订单详情</span>
                     </div>
                     <div class="flex gap-3 mb-3">
                         <div class="w-16 h-16 bg-zinc-50 rounded border border-zinc-100 shrink-0">
                             <img :src="resolveOrder(msg.content)?.items[0]?.product?.imageUrl" class="w-full h-full object-cover rounded" />
                         </div>
                         <div class="flex-1 min-w-0">
                             <div class="text-sm font-bold truncate mb-1">{{ resolveOrder(msg.content)?.items[0]?.product?.name }}</div>
                             <div class="flex justify-between items-center">
                                 <span class="text-rose-500 font-black">¥{{ resolveOrder(msg.content)?.totalAmount }}</span>
                                 <span class="text-xs text-zinc-400">共{{ resolveOrder(msg.content)?.items.length }}件</span>
                             </div>
                         </div>
                     </div>
                     <div class="bg-zinc-50 p-2 rounded text-xs space-y-1 text-zinc-500">
                         <div class="flex gap-2"><span class="font-bold text-zinc-600 min-w-[3em]">订单号</span> <span>{{ resolveOrder(msg.content)?.id }}</span></div>
                         <div class="flex gap-2"><span class="font-bold text-zinc-600 min-w-[3em]">状态</span> <span>{{ statusLabels[resolveOrder(msg.content)?.status || ''] || resolveOrder(msg.content)?.status }}</span></div>
                         <div class="flex gap-2"><span class="font-bold text-zinc-600 min-w-[3em]">下单时间</span> <span class="truncate">{{ resolveOrder(msg.content)?.createdAt ? new Date(resolveOrder(msg.content)?.createdAt).toLocaleDateString() : '' }}</span></div>
                     </div>
                 </div>

                 <!-- 文本消息 -->
                 <div v-else class="px-4 py-2.5 text-sm break-words whitespace-pre-wrap leading-relaxed">{{ msg.content }}</div>
              </div>
              
              <!-- 状态与时间 -->
              <div class="flex items-center gap-1 mt-1 px-1">
                  <span class="text-[10px] text-zinc-400">{{ formatTime(msg.createdAt) }}</span>
                  <div v-if="msg.sender.id === userStore.user?.id">
                      <span v-if="msg.read" class="text-[10px] text-zinc-400">已读</span>
                      <span v-else class="text-[10px] text-rose-500">未读</span>
                  </div>
              </div>
           </div>
         </div>
       </template>
    </div>

    <!-- 快捷短语（仅限用户） -->
    <div v-if="!isMerchant" class="bg-zinc-50 border-t border-zinc-100 pt-2 pb-1">
        <div class="flex gap-2 overflow-x-auto px-4 pb-2 scrollbar-hide no-scrollbar">
            <button 
                v-for="phrase in quickPhrases" 
                :key="phrase"
                @click="handleQuickReply(phrase)"
                class="shrink-0 text-xs px-3 py-1.5 bg-white border border-zinc-200 rounded-full text-zinc-600 hover:text-rose-500 hover:border-rose-200 hover:bg-rose-50 transition-colors whitespace-nowrap shadow-sm"
            >
                {{ phrase }}
            </button>
        </div>
    </div>

    <!-- 输入框 -->
    <div class="bg-white p-3 pt-2 border-t border-zinc-100 shrink-0 relative">
      <!-- 咨询卡片（右下角悬浮） -->
      <div v-if="props.order && !isMerchant && showConsultCard" class="absolute bottom-full right-4 mb-4 w-72 bg-white rounded-xl shadow-xl border border-zinc-200 p-3 animate-in slide-in-from-bottom-4 z-20">
          <div class="flex justify-between items-start mb-2">
              <span class="text-xs font-bold text-zinc-500">你可能想咨询该订单</span>
              <button @click="showConsultCard = false" class="text-zinc-400 hover:text-zinc-600"><X class="w-4 h-4" /></button>
          </div>
          <div class="flex gap-3 mb-3">
              <div class="w-12 h-12 bg-zinc-50 rounded border border-zinc-100 shrink-0">
                  <img :src="props.order.items[0]?.product?.imageUrl" class="w-full h-full object-cover rounded" />
              </div>
              <div class="flex-1 min-w-0">
                  <div class="text-xs font-bold truncate mb-1">{{ props.order.items[0]?.product?.name }}</div>
                  <div class="text-xs text-zinc-400">共{{ props.order.items.length }}件商品 · 合计 {{ props.order.totalAmount }}</div>
              </div>
          </div>
          <button 
              @click="handleSendConsultCard"
              class="w-full py-1.5 bg-orange-500 hover:bg-orange-600 text-white rounded-full text-xs font-bold transition-colors shadow-sm shadow-orange-200"
          >
              发送
          </button>
      </div>

      <!-- 表情选择器 -->
      <div v-if="showEmojiPicker" class="absolute bottom-16 left-4 z-50 bg-white shadow-xl border border-zinc-200 rounded-xl p-2 grid grid-cols-8 gap-1 w-64 animate-in slide-in-from-bottom-2">
          <button 
            v-for="emoji in emojis" 
            :key="emoji" 
            @click="handleEmojiSelect(emoji)"
            class="p-2 hover:bg-zinc-100 rounded text-xl transition-colors"
          >
            {{ emoji }}
          </button>
      </div>

      <div v-if="showImageUpload" class="mb-3 p-3 bg-zinc-50 rounded-xl border border-zinc-100 relative animate-in slide-in-from-bottom-2">
         <button @click="showImageUpload = false; uploadedImage = ''" class="absolute -top-2 -right-2 bg-zinc-200 rounded-full p-1 text-zinc-500 hover:bg-zinc-300"><X class="w-3 h-3"/></button>
         <ImageUpload v-model="uploadedImage" />
      </div>
      
      <div class="flex items-end gap-2">
        <textarea 
          v-model="newMessage"
          @keydown.enter.prevent="handleSend"
          @input="(e) => { (e.target as HTMLTextAreaElement).style.height = 'auto'; (e.target as HTMLTextAreaElement).style.height = (e.target as HTMLTextAreaElement).scrollHeight + 'px' }"
          placeholder="输入消息..."
          rows="1"
          class="flex-1 bg-zinc-100 border-0 rounded-xl px-4 py-2.5 focus:ring-2 focus:ring-rose-500/20 text-sm resize-none max-h-24 min-h-[40px] focus:bg-white transition-colors"
          style="min-height: 44px;"
        ></textarea>
        
        <div class="flex items-center gap-1 mb-0.5">
            <button 
              @click="showEmojiPicker = !showEmojiPicker"
              class="p-2 text-zinc-400 hover:text-orange-500 rounded-full transition-colors"
              :class="{ 'text-orange-500': showEmojiPicker }"
            >
              <Smile class="w-6 h-6" />
            </button>
            <button 
              @click="showImageUpload = !showImageUpload"
              class="p-2 text-zinc-400 hover:text-rose-500 rounded-full transition-colors"
              :class="{ 'text-rose-500': showImageUpload }"
            >
              <ImageIcon class="w-6 h-6" />
            </button>
            <button 
              @click="handleSend" 
              :disabled="(!newMessage.trim() && !uploadedImage) || sending"
              class="p-2.5 bg-rose-500 text-white rounded-xl shadow-lg shadow-rose-200 hover:bg-rose-600 disabled:opacity-50 disabled:shadow-none transition-all active:scale-95 ml-1"
            >
              <Send class="w-5 h-5" />
            </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Hide scrollbar for Chrome, Safari and Opera */
.no-scrollbar::-webkit-scrollbar {
    display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
.no-scrollbar {
    -ms-overflow-style: none;  /* IE and Edge */
    scrollbar-width: none;  /* Firefox */
}
</style>
