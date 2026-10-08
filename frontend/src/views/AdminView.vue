<script setup lang="ts">
import { ref, onMounted, computed, provide } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { LayoutDashboard, ShoppingBag, FileText, Users, Trash2, Edit, Plus, X, Truck, LogOut, MessageSquare, Image as ImageIcon, AlertCircle, CheckCircle, XCircle, MessageCircle } from 'lucide-vue-next'
import { userApi, marketApi, blogApi, adminApi, chatApi } from '@/api'
import type { User, Product, BlogPost, Order, BlogComment, ChatContact } from '@/types'
import { useToast } from '@/composables/useToast'
import ImageUpload from '@/components/ImageUpload.vue'
import ChatBox from '@/components/ChatBox.vue'
import Pagination from '@/components/Pagination.vue'

// ECharts图表
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import VChart, { THEME_KEY } from 'vue-echarts'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])
provide(THEME_KEY, 'light') // 改为浅色主题

const userStore = useUserStore()
const router = useRouter()
const toast = useToast()

const activeTab = ref('dashboard')

// 数据
const users = ref<User[]>([])
const products = ref<Product[]>([])
const posts = ref<BlogPost[]>([])
const orders = ref<Order[]>([])
const comments = ref<BlogComment[]>([])
const chatContacts = ref<ChatContact[]>([])
const selectedContact = ref<ChatContact | null>(null)
let chatPollingInterval: any = null

onMounted(() => {
  if (chatPollingInterval) clearInterval(chatPollingInterval)
})

// 帖子过滤
const activePostFilter = ref('ALL')
const postPage = ref(1)
const postPageSize = ref(15)

const filteredPosts = computed(() => {
  let res = posts.value
  if (activePostFilter.value === 'ADMIN') {
    res = posts.value.filter(post => post.author?.role === 'ADMIN')
  } else if (activePostFilter.value === 'USER') {
    res = posts.value.filter(post => post.author?.role !== 'ADMIN')
  }
  return res
})

const paginatedPosts = computed(() => {
  const start = (postPage.value - 1) * postPageSize.value
  const end = start + postPageSize.value
  return filteredPosts.value.slice(start, end)
})

const totalPostPages = computed(() => {
  return Math.ceil(filteredPosts.value.length / postPageSize.value)
})

// 订单分页与过滤
const activeOrderFilter = ref('ALL')
const orderPage = ref(1)
const orderPageSize = ref(15)

const filteredOrders = computed(() => {
  if (activeOrderFilter.value === 'ALL') {
    return orders.value
  }
  return orders.value.filter(order => order.status === activeOrderFilter.value)
})

const paginatedOrders = computed(() => {
  const start = (orderPage.value - 1) * orderPageSize.value
  const end = start + orderPageSize.value
  return filteredOrders.value.slice(start, end)
})

const totalOrderPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / orderPageSize.value)
})

// 退款订单（显示已申请和已批准/处理中）
const refundPage = ref(1)
const refundPageSize = ref(15)

const refundOrders = computed(() => {
  return orders.value.filter(o => o.refundStatus && o.refundStatus !== 'NONE')
})

const paginatedRefunds = computed(() => {
  const start = (refundPage.value - 1) * refundPageSize.value
  const end = start + refundPageSize.value
  return refundOrders.value.slice(start, end)
})

const totalRefundPages = computed(() => {
  return Math.ceil(refundOrders.value.length / refundPageSize.value)
})

// 用户分页
const userPage = ref(1)
const userPageSize = ref(15)

const paginatedUsers = computed(() => {
  const start = (userPage.value - 1) * userPageSize.value
  const end = start + userPageSize.value
  return users.value.slice(start, end)
})

const totalUserPages = computed(() => {
  return Math.ceil(users.value.length / userPageSize.value)
})

// 商品分页
const productPage = ref(1)
const productPageSize = ref(15)

const paginatedProducts = computed(() => {
  const start = (productPage.value - 1) * productPageSize.value
  const end = start + productPageSize.value
  return products.value.slice(start, end)
})

const totalProductPages = computed(() => {
  return Math.ceil(products.value.length / productPageSize.value)
})

// 评论分页
const commentPage = ref(1)
const commentPageSize = ref(15)

const paginatedComments = computed(() => {
  const start = (commentPage.value - 1) * commentPageSize.value
  const end = start + commentPageSize.value
  return comments.value.slice(start, end)
})

const totalCommentPages = computed(() => {
  return Math.ceil(comments.value.length / commentPageSize.value)
})

const setPostFilter = (filter: string) => {
  activePostFilter.value = filter
  postPage.value = 1
}

const setOrderFilter = (filter: string) => {
  activeOrderFilter.value = filter
  orderPage.value = 1
}

// 加载状态
const loading = ref(false)

// 商品弹窗
const showProductModal = ref(false)
const editingProduct = ref<Product | null>(null)
const productForm = ref({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  category: '工具',
  imageUrl: ''
})

// 帖子弹窗
const showPostModal = ref(false)
const editingPost = ref<BlogPost | null>(null)
const postForm = ref({
  title: '',
  content: '',
  category: '全部',
  coverImage: ''
})

// 批量删除选择
const selectedIds = ref<Set<number>>(new Set())

const toggleSelection = (id: number) => {
  if (selectedIds.value.has(id)) {
    selectedIds.value.delete(id)
  } else {
    selectedIds.value.add(id)
  }
}

const toggleAllSelection = (items: any[]) => {
  if (selectedIds.value.size === items.length && items.length > 0) {
    selectedIds.value.clear()
  } else {
    items.forEach(item => selectedIds.value.add(item.id))
  }
}

const isAllSelected = (items: any[]) => {
  return items.length > 0 && items.every(item => selectedIds.value.has(item.id))
}

const clearSelection = () => {
  selectedIds.value.clear()
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  loadData('dashboard')
})

const loadData = async (tab: string) => {
  loading.value = true
  try {
    // 始终加载仪表盘的基本统计数据
    if (tab === 'dashboard') {
        const [usersRes, ordersRes, statsRes] = await Promise.all([
            userApi.getAllUsers(),
            marketApi.getAllOrders(),
            adminApi.getStats()
        ])
        users.value = Array.isArray(usersRes) ? usersRes : []
        orders.value = Array.isArray(ordersRes) ? ordersRes : []
        
        if (statsRes) {
           chartOption.value.xAxis[0].data = statsRes.dates
           chartOption.value.series[0].data = statsRes.orders
           chartOption.value.series[1].data = statsRes.users
           chartOption.value.series[2].data = statsRes.interactions
        }
    } else if (tab === 'users') {
      const res = await userApi.getAllUsers()
      users.value = Array.isArray(res) ? res : []
    } else if (tab === 'products') {
      const res = await marketApi.getProducts()
      products.value = Array.isArray(res) ? res : []
    } else if (tab === 'posts') {
      const res = await blogApi.getPosts()
      posts.value = Array.isArray(res) ? res : []
    } else if (tab === 'orders' || tab === 'refunds') {
      const res = await marketApi.getAllOrders()
      orders.value = Array.isArray(res) ? res : []
    } else if (tab === 'comments') {
      const res = await blogApi.getAllComments()
      comments.value = Array.isArray(res) ? res : []
    } else if (tab === 'chat') {
      if (userStore.user?.id) {
        const [contactsRes, ordersRes] = await Promise.all([
            chatApi.getRecentContacts(userStore.user.id),
            marketApi.getAllOrders()
        ])
        chatContacts.value = Array.isArray(contactsRes) ? contactsRes : []
        orders.value = Array.isArray(ordersRes) ? ordersRes : []
        
        if (chatContacts.value.length > 0 && !selectedContact.value) {
            selectedContact.value = chatContacts.value[0]
        }
      }
    }
  } catch (error) {
    console.error(`Failed to load ${tab}:`, error)
    toast.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleMessagesRead = (contactId: number) => {
  const index = chatContacts.value.findIndex(c => c.id === contactId)
  if (index !== -1) {
    chatContacts.value[index].unreadCount = 0
  }
}

const switchTab = (tab: string) => {
  activeTab.value = tab
  clearSelection()
  loadData(tab)
  
  if (tab === 'chat') {
      if (!chatPollingInterval) {
          chatPollingInterval = setInterval(async () => {
              if (activeTab.value === 'chat' && userStore.user?.id) {
                  try {
                      const res = await chatApi.getRecentContacts(userStore.user.id)
                      const newContacts = Array.isArray(res) ? res : []
                      
                      // 合并逻辑 to preserve local unreadCount updates
                      chatContacts.value = newContacts.map(nc => {
                          // 如果这是当前选择的联系人，强制未读数为 0
                          if (selectedContact.value?.id === nc.id) {
                              nc.unreadCount = 0
                          }
                          return nc
                      })
                  } catch (e) {
                      console.error('Polling contacts failed', e)
                  }
              }
          }, 5000)
      }
  } else {
      if (chatPollingInterval) {
          clearInterval(chatPollingInterval)
          chatPollingInterval = null
      }
  }
}

const selectContact = (contact: ChatContact) => {
  selectedContact.value = contact
  // 乐观更新 UI 以移除未读徽章
  const index = chatContacts.value.findIndex(c => c.id === contact.id)
  if (index !== -1) {
    chatContacts.value[index].unreadCount = 0
  }
}

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

// 用户操作
const deleteUser = async (id: number) => {
  if (!confirm('确定要删除该用户吗？')) return
  try {
    await userApi.deleteUser(id)
    users.value = users.value.filter(u => u.id !== id)
    selectedIds.value.delete(id)
    toast.success('用户已删除')
  } catch (error) {
    console.error('Failed to delete user:', error)
    toast.error('删除失败')
  }
}

const batchDeleteUsers = async () => {
  if (selectedIds.value.size === 0) return
  if (!confirm(`确定要批量删除这 ${selectedIds.value.size} 个用户吗？`)) return
  try {
    const promises = Array.from(selectedIds.value).map(id => userApi.deleteUser(id))
    await Promise.all(promises)
    users.value = users.value.filter(u => !selectedIds.value.has(u.id))
    clearSelection()
    toast.success('批量删除成功')
  } catch (error) {
    toast.error('批量删除出现部分错误')
  }
}

// 商品操作
const openProductModal = (product?: Product) => {
  if (product) {
    editingProduct.value = product
    productForm.value = { ...product }
  } else {
    editingProduct.value = null
    productForm.value = {
      name: '',
      description: '',
      price: 0,
      stock: 100,
      category: '工具',
      imageUrl: 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=product%20placeholder&image_size=square'
    }
  }
  showProductModal.value = true
}

const saveProduct = async () => {
  try {
    if (editingProduct.value) {
      await marketApi.updateProduct(editingProduct.value.id, productForm.value)
      toast.success('商品更新成功')
    } else {
      await marketApi.addProduct(productForm.value)
      toast.success('商品添加成功')
    }
    showProductModal.value = false
    loadData('products')
  } catch (error) {
    console.error('Failed to save product:', error)
    toast.error('保存失败')
  }
}

const deleteProduct = async (id: number) => {
  if (!confirm('确定要下架该商品吗？')) return
  try {
    await marketApi.deleteProduct(id)
    products.value = products.value.filter(p => p.id !== id)
    selectedIds.value.delete(id)
    toast.success('商品已下架')
  } catch (error) {
    console.error('Failed to delete product:', error)
    toast.error('删除失败')
  }
}

const batchDeleteProducts = async () => {
  if (selectedIds.value.size === 0) return
  if (!confirm(`确定要批量下架这 ${selectedIds.value.size} 个商品吗？`)) return
  try {
    const promises = Array.from(selectedIds.value).map(id => marketApi.deleteProduct(id))
    await Promise.all(promises)
    products.value = products.value.filter(p => !selectedIds.value.has(p.id))
    clearSelection()
    toast.success('批量下架成功')
  } catch (error) {
    toast.error('批量下架出现部分错误')
  }
}

// 帖子操作
const openPostModal = (post?: BlogPost) => {
  if (post) {
    editingPost.value = post
    postForm.value = {
      title: post.title,
      content: post.content,
      category: post.category,
      coverImage: post.coverImage || ''
    }
  } else {
    editingPost.value = null
    postForm.value = {
      title: '',
      content: '',
      category: '全部',
      coverImage: 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=blog%20post%20cover&image_size=landscape_4_3'
    }
  }
  showPostModal.value = true
}

const savePost = async () => {
  if (!userStore.user?.id) {
    toast.error('用户未登录')
    return
  }
  try {
    const postData = {
      ...postForm.value,
      author: { id: userStore.user.id } // 确保设置了作者
    }
    
    if (editingPost.value) {
      await blogApi.updatePost(editingPost.value.id, postData)
      toast.success('文章更新成功')
    } else {
      await blogApi.createPost(postData)
      toast.success('文章发布成功')
    }
    showPostModal.value = false
    loadData('posts')
  } catch (error) {
    console.error('Failed to save post:', error)
    toast.error('保存失败')
  }
}

const deletePost = async (id: number) => {
  if (!confirm('确定要删除这篇帖子吗？')) return
  try {
    await blogApi.deletePost(id)
    posts.value = posts.value.filter(p => p.id !== id)
    selectedIds.value.delete(id)
    toast.success('文章已删除')
  } catch (error) {
    console.error('Failed to delete post:', error)
    toast.error('删除失败')
  }
}

const batchDeletePosts = async () => {
  if (selectedIds.value.size === 0) return
  if (!confirm(`确定要批量删除这 ${selectedIds.value.size} 篇帖子吗？`)) return
  try {
    const promises = Array.from(selectedIds.value).map(id => blogApi.deletePost(id))
    await Promise.all(promises)
    posts.value = posts.value.filter(p => !selectedIds.value.has(p.id))
    clearSelection()
    toast.success('批量删除成功')
  } catch (error) {
    toast.error('批量删除出现部分错误')
  }
}

// 评论操作
const deleteComment = async (id: number) => {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    await blogApi.deleteComment(id)
    comments.value = comments.value.filter(c => c.id !== id)
    selectedIds.value.delete(id)
    toast.success('评论已删除')
  } catch (error) {
    console.error('Failed to delete comment:', error)
    toast.error('删除失败')
  }
}

const batchDeleteComments = async () => {
  if (selectedIds.value.size === 0) return
  if (!confirm(`确定要批量删除这 ${selectedIds.value.size} 条评论吗？`)) return
  try {
    const promises = Array.from(selectedIds.value).map(id => blogApi.deleteComment(id))
    await Promise.all(promises)
    comments.value = comments.value.filter(c => !selectedIds.value.has(c.id))
    clearSelection()
    toast.success('批量删除成功')
  } catch (error) {
    toast.error('批量删除出现部分错误')
  }
}

// 订单操作
const updateOrderStatus = async (id: number, status: string) => {
  try {
    await marketApi.updateOrderStatus(id, status)
    const order = orders.value.find(o => o.id === id)
    if (order) order.status = status
    toast.success('订单状态已更新')
  } catch (error) {
    console.error('Failed to update order:', error)
    toast.error('更新失败')
  }
}

const deleteOrder = async (id: number) => {
  if (!confirm('确定要删除该订单吗？')) return
  try {
    await marketApi.deleteOrder(id)
    orders.value = orders.value.filter(o => o.id !== id)
    selectedIds.value.delete(id)
    toast.success('订单已删除')
  } catch (error) {
    console.error('Failed to delete order:', error)
    toast.error('删除订单失败')
  }
}

const batchDeleteOrders = async () => {
  if (selectedIds.value.size === 0) return
  if (!confirm(`确定要批量删除这 ${selectedIds.value.size} 个订单吗？`)) return
  try {
    const promises = Array.from(selectedIds.value).map(id => marketApi.deleteOrder(id))
    await Promise.all(promises)
    orders.value = orders.value.filter(o => !selectedIds.value.has(o.id))
    clearSelection()
    toast.success('批量删除成功')
  } catch (error) {
    toast.error('批量删除出现部分错误')
  }
}

// 退款操作
const handleRefund = async (id: number, approved: boolean) => {
  if (!confirm(approved ? '确定同意售后申请吗？' : '确定拒绝售后申请吗？')) return
  try {
    await marketApi.handleRefund(id, approved)
    toast.success(approved ? '已同意申请' : '已拒绝申请')
    loadData('refunds') // 重新加载数据
  } catch (error) {
    console.error('Refund action failed:', error)
    toast.error('操作失败')
  }
}

const handleConfirmReturn = async (id: number) => {
  if (!confirm('确认已收到退货商品并无误吗？确认后将自动退款。')) return
  try {
    await marketApi.confirmReturnReceipt(id)
    toast.success('确认收货成功，退款已完成')
    loadData('refunds')
  } catch (error) {
    console.error('Confirm return failed:', error)
    toast.error('操作失败')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 更新的图表选项（浅色/暖色主题）
const chartOption = ref({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross', label: { backgroundColor: '#FFB74D' } }
  },
  legend: {
    data: ['订单数', '新增用户', '互动量'],
    textStyle: { color: '#795548' } // 棕色
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: [
    {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#D7CCC8' } },
      axisLabel: { color: '#5D4037' }
    }
  ],
  yAxis: [
    {
      type: 'value',
      axisLine: { lineStyle: { color: '#D7CCC8' } },
      splitLine: { lineStyle: { color: '#EFEBE9' } },
      axisLabel: { color: '#5D4037' }
    }
  ],
  series: [
    {
      name: '订单数',
      type: 'line',
      stack: 'Total',
      smooth: true,
      lineStyle: { width: 0 },
      showSymbol: false,
      areaStyle: { opacity: 0.8, color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#42A5F5' }, { offset: 1, color: '#BBDEFB' }] } },
      emphasis: { focus: 'series' },
      data: [220, 182, 191, 234, 290, 330, 310]
    },
    {
      name: '新增用户',
      type: 'line',
      stack: 'Total',
      smooth: true,
      lineStyle: { width: 0 },
      showSymbol: false,
      areaStyle: { opacity: 0.8, color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#66BB6A' }, { offset: 1, color: '#C8E6C9' }] } },
      emphasis: { focus: 'series' },
      data: [150, 232, 201, 154, 190, 330, 410]
    },
    {
      name: '互动量',
      type: 'line',
      stack: 'Total',
      smooth: true,
      lineStyle: { width: 0 },
      showSymbol: false,
      areaStyle: { opacity: 0.8, color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#FFA726' }, { offset: 1, color: '#FFE0B2' }] } },
      emphasis: { focus: 'series' },
      data: [320, 332, 301, 334, 390, 330, 320]
    }
  ]
})
</script>

<template>
  <div class="flex h-screen bg-[#FFF8E1] text-[#5D4037] font-sans">
    <!-- 侧边栏 -->
    <div class="w-64 bg-white shadow-xl flex flex-col z-10 border-r border-[#EFEBE9]">
      <div class="p-6 border-b border-[#EFEBE9]">
        <h1 class="text-2xl font-black text-rose-500 flex items-center gap-2 tracking-tighter">
          <div class="w-8 h-8 rounded-lg overflow-hidden bg-white border border-rose-100 flex items-center justify-center">
            <img src="/ico (2).png" class="w-full h-full object-contain scale-110" />
          </div> 桃粿源·后台
        </h1>
        <p class="text-xs text-[#8D6E63] mt-1 uppercase tracking-widest font-bold">Admin Console</p>
      </div>
      <nav class="flex-1 p-4 space-y-2">
        <button 
          v-for="tab in ['dashboard', 'users', 'products', 'posts', 'comments', 'orders', 'refunds', 'chat']"
          :key="tab"
          @click="switchTab(tab)"
          :class="[
            'w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-bold text-sm',
            activeTab === tab ? 'bg-rose-500 text-white shadow-lg shadow-rose-200' : 'text-[#8D6E63] hover:bg-[#FFF3E0] hover:text-rose-600'
          ]"
        >
          <component 
            :is="tab === 'dashboard' ? LayoutDashboard : tab === 'users' ? Users : tab === 'products' ? ShoppingBag : tab === 'orders' ? Truck : tab === 'comments' ? MessageSquare : tab === 'refunds' ? AlertCircle : tab === 'chat' ? MessageCircle : FileText" 
            class="w-5 h-5"
          />
          {{ tab === 'dashboard' ? '数据概览' : tab === 'users' ? '用户管理' : tab === 'products' ? '商品管理' : tab === 'orders' ? '订单管理' : tab === 'comments' ? '评论管理' : tab === 'refunds' ? '售后处理' : tab === 'chat' ? '消息中心' : '内容管理' }}
        </button>
      </nav>
      <div class="p-4 border-t border-[#EFEBE9] bg-[#FAFAFA]">
        <div class="flex items-center gap-3 px-4 py-3">
          <img :src="userStore.user?.avatar" class="w-10 h-10 rounded-full border-2 border-zinc-100" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-bold text-[#5D4037] truncate">{{ userStore.user?.nickname }}</p>
            <p class="text-xs text-rose-500 font-mono">ADMIN</p>
          </div>
          <button @click="handleLogout" class="text-[#8D6E63] hover:text-rose-500 transition-colors p-2 rounded-full hover:bg-[#FFF3E0]" title="退出登录">
            <LogOut class="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="flex-1 overflow-y-auto p-8 relative">
      <header class="flex justify-between items-center mb-8">
        <div>
          <h2 class="text-3xl font-black text-[#3E2723] tracking-tight">
            {{ activeTab === 'dashboard' ? '数据视界' : activeTab === 'users' ? '用户管理' : activeTab === 'products' ? '商品库存' : activeTab === 'orders' ? '全域订单' : activeTab === 'comments' ? '评论监控' : activeTab === 'refunds' ? '售后中心' : activeTab === 'chat' ? '消息中心' : '内容发布' }}
          </h2>
          <p class="text-[#8D6E63] text-sm font-bold mt-1">{{ new Date().toLocaleDateString() }} · System Active</p>
        </div>
      </header>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex justify-center items-center h-96">
        <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-rose-500"></div>
      </div>

      <div v-else class="animate-in fade-in slide-in-from-bottom-4 duration-500">
        <!-- 数据面板标签页 -->
        <div v-if="activeTab === 'dashboard'" class="space-y-6">
          <!-- 宏观数据流图表 -->
          <div class="bg-white border border-[#EFEBE9] rounded-3xl p-6 shadow-xl relative overflow-hidden group">
            <div class="flex justify-between items-center mb-6 relative z-10">
              <h3 class="text-xl font-bold text-[#5D4037]">实时电商数据流</h3>
              <div class="flex gap-2">
                <span class="w-3 h-3 rounded-full bg-rose-500 animate-pulse"></span>
                <span class="text-xs font-mono text-rose-500">LIVE</span>
              </div>
            </div>
            <div class="h-[400px] w-full">
              <v-chart class="chart" :option="chartOption" autoresize />
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
             <div class="bg-white border border-[#EFEBE9] p-6 rounded-2xl shadow-lg hover:border-rose-200 transition-colors group">
                <div class="flex justify-between items-start mb-4">
                   <div class="p-3 bg-[#FFF3E0] rounded-xl text-rose-500 group-hover:scale-110 transition-transform"><ShoppingBag class="w-6 h-6"/></div>
                   <span class="text-xs font-bold bg-green-100 text-green-600 px-2 py-1 rounded">实时</span>
                </div>
                <div class="text-[#8D6E63] text-sm font-bold">总交易额 (GMV)</div>
                <div class="text-3xl font-black text-[#3E2723] mt-1">¥ {{ orders.reduce((acc, cur) => acc + (cur.status !== 'CANCELLED' ? cur.totalAmount : 0), 0).toFixed(2) }}</div>
             </div>
             <div class="bg-white border border-[#EFEBE9] p-6 rounded-2xl shadow-lg hover:border-blue-200 transition-colors group">
                <div class="flex justify-between items-start mb-4">
                   <div class="p-3 bg-blue-50 rounded-xl text-blue-500 group-hover:scale-110 transition-transform"><Users class="w-6 h-6"/></div>
                   <span class="text-xs font-bold bg-green-100 text-green-600 px-2 py-1 rounded">实时</span>
                </div>
                <div class="text-[#8D6E63] text-sm font-bold">注册用户</div>
                <div class="text-3xl font-black text-[#3E2723] mt-1">{{ users.length }}</div>
             </div>
             <div class="bg-white border border-[#EFEBE9] p-6 rounded-2xl shadow-lg hover:border-purple-200 transition-colors group">
                <div class="flex justify-between items-start mb-4">
                   <div class="p-3 bg-purple-50 rounded-xl text-purple-500 group-hover:scale-110 transition-transform"><Truck class="w-6 h-6"/></div>
                   <span class="text-xs font-bold bg-yellow-100 text-yellow-600 px-2 py-1 rounded">待处理</span>
                </div>
                <div class="text-[#8D6E63] text-sm font-bold">待发货订单</div>
                <div class="text-3xl font-black text-[#3E2723] mt-1">{{ orders.filter(o => o.status === 'PENDING').length }}</div>
             </div>
          </div>
        </div>

        <!-- 用户标签页 -->
        <div v-if="activeTab === 'users'">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-xl font-bold text-[#3E2723]">用户列表</h3>
            <button 
              v-if="selectedIds.size > 0"
              @click="batchDeleteUsers"
              class="bg-rose-500 text-white px-4 py-2 rounded-lg font-bold shadow-sm hover:bg-rose-600 transition-all flex items-center gap-2 text-sm"
            >
              <Trash2 class="w-4 h-4" /> 批量删除 ({{ selectedIds.size }})
            </button>
          </div>
          <div class="bg-white rounded-3xl shadow-lg border border-[#EFEBE9] overflow-hidden">
             <div class="overflow-x-auto">
               <table class="w-full text-left text-sm text-[#5D4037]">
                 <thead class="bg-[#FFF8E1] border-b border-[#EFEBE9] font-bold uppercase tracking-wider">
                   <tr>
                     <th class="px-6 py-4 w-12">
                       <input type="checkbox" :checked="isAllSelected(paginatedUsers)" @change="toggleAllSelection(paginatedUsers)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                     </th>
                     <th class="px-6 py-4">ID</th>
                     <th class="px-6 py-4">用户</th>
                     <th class="px-6 py-4">用户名</th>
                     <th class="px-6 py-4">角色</th>
                     <th class="px-6 py-4">加入时间</th>
                     <th class="px-6 py-4 text-right">操作</th>
                   </tr>
                 </thead>
                 <tbody class="divide-y divide-[#EFEBE9]">
                   <tr v-if="users.length === 0">
                     <td colspan="7" class="px-6 py-10 text-center text-[#8D6E63] italic">暂无用户数据</td>
                   </tr>
                   <tr v-for="user in paginatedUsers" :key="user.id" class="hover:bg-[#FAFAFA] transition-colors group">
                     <td class="px-6 py-4">
                       <input type="checkbox" :checked="selectedIds.has(user.id)" @change="toggleSelection(user.id)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                     </td>
                     <td class="px-6 py-4 font-mono text-xs font-bold text-[#8D6E63]">#{{ user.id }}</td>
                     <td class="px-6 py-4">
                        <div class="flex items-center gap-3">
                           <img :src="user.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-8 h-8 rounded-full bg-zinc-50 object-cover border border-zinc-100" />
                           <span class="font-bold text-[#3E2723]">{{ user.nickname }}</span>
                        </div>
                     </td>
                     <td class="px-6 py-4 text-[#8D6E63] font-mono">{{ user.username }}</td>
                     <td class="px-6 py-4">
                        <span :class="['px-2 py-1 rounded text-xs font-bold uppercase', user.role === 'ADMIN' ? 'bg-rose-100 text-rose-600' : 'bg-blue-100 text-blue-600']">{{ user.role }}</span>
                     </td>
                     <td class="px-6 py-4 text-[#8D6E63] text-xs font-medium">{{ formatDate(user.createdAt) }}</td>
                     <td class="px-6 py-4 text-right">
                        <button @click="deleteUser(user.id)" class="p-2 text-rose-300 hover:text-rose-500 hover:bg-rose-50 rounded-full transition-colors" title="删除">
                          <Trash2 class="w-4 h-4" />
                        </button>
                     </td>
                   </tr>
                 </tbody>
               </table>
             </div>
          </div>
          <!-- 用户分页 -->
          <Pagination 
            :current="userPage" 
            :total="users.length" 
            :page-size="userPageSize"
            @change="(p) => userPage = p"
            @update:page-size="(s) => userPageSize = s"
          />
        </div>

        <!-- 商品标签页 -->
        <div v-if="activeTab === 'products'">
          <div class="flex justify-between items-center mb-6">
            <div class="flex items-center gap-4">
              <h3 class="text-xl font-bold text-[#3E2723]">商品列表</h3>
              <label class="flex items-center gap-2 cursor-pointer text-sm font-bold text-[#5D4037]">
                <input type="checkbox" :checked="isAllSelected(paginatedProducts)" @change="toggleAllSelection(paginatedProducts)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500" />
                全选本页
              </label>
              <button 
                v-if="selectedIds.size > 0"
                @click="batchDeleteProducts"
                class="bg-rose-500 text-white px-4 py-2 rounded-lg font-bold shadow-sm hover:bg-rose-600 transition-all flex items-center gap-2 text-sm"
              >
                <Trash2 class="w-4 h-4" /> 批量下架 ({{ selectedIds.size }})
              </button>
            </div>
            <button @click="openProductModal()" class="bg-rose-500 text-white px-6 py-2.5 rounded-full font-bold shadow-lg shadow-rose-200 hover:bg-rose-600 hover:scale-105 transition-all flex items-center gap-2">
              <Plus class="w-4 h-4" /> 新增商品
            </button>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div v-for="product in paginatedProducts" :key="product.id" class="bg-white rounded-2xl overflow-hidden border border-[#EFEBE9] hover:border-rose-200 transition-all group shadow-lg relative">
              <div class="absolute top-2 left-2 z-10">
                <input type="checkbox" :checked="selectedIds.has(product.id)" @change="toggleSelection(product.id)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer w-5 h-5 shadow-sm" />
              </div>
              <div class="aspect-square bg-[#FAFAFA] relative">
                <img :src="product.imageUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                <div class="absolute top-2 right-2 bg-white/80 backdrop-blur px-2 py-1 rounded-lg text-xs font-bold text-[#5D4037]">库存: {{ product.stock }}</div>
              </div>
              <div class="p-4">
                <div class="flex justify-between items-start mb-2">
                  <h3 class="font-bold text-[#3E2723] truncate flex-1 pr-2" :title="product.name">{{ product.name }}</h3>
                  <div class="text-rose-500 font-black">¥{{ product.price }}</div>
                </div>
                <div class="text-xs text-[#8D6E63] mb-4 bg-[#FFF8E1] inline-block px-2 py-1 rounded">{{ product.category }}</div>
                <div class="flex gap-2 pt-2 border-t border-[#EFEBE9]">
                   <button @click="openProductModal(product)" class="flex-1 py-2 bg-[#FAFAFA] text-[#8D6E63] rounded-lg text-xs font-bold hover:bg-blue-50 hover:text-blue-600 transition-colors">编辑</button>
                   <button @click="deleteProduct(product.id)" class="flex-1 py-2 bg-[#FAFAFA] text-[#8D6E63] rounded-lg text-xs font-bold hover:bg-rose-50 hover:text-rose-500 transition-colors">下架</button>
                </div>
              </div>
            </div>
          </div>
          <!-- 商品分页 -->
          <Pagination 
            :current="productPage" 
            :total="products.length" 
            :page-size="productPageSize"
            @change="(p) => productPage = p"
            @update:page-size="(s) => productPageSize = s"
          />
        </div>

        <!-- 评论标签页 -->
        <div v-if="activeTab === 'comments'">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-xl font-bold text-[#3E2723]">评论管理</h3>
            <button 
              v-if="selectedIds.size > 0"
              @click="batchDeleteComments"
              class="bg-rose-500 text-white px-4 py-2 rounded-lg font-bold shadow-sm hover:bg-rose-600 transition-all flex items-center gap-2 text-sm"
            >
              <Trash2 class="w-4 h-4" /> 批量删除 ({{ selectedIds.size }})
            </button>
          </div>
          <div class="bg-white rounded-3xl shadow-lg border border-[#EFEBE9] overflow-hidden">
             <div class="overflow-x-auto">
               <table class="w-full text-left text-sm text-[#5D4037]">
                 <thead class="bg-[#FFF8E1] border-b border-[#EFEBE9] font-bold uppercase tracking-wider">
                   <tr>
                     <th class="px-6 py-4 w-12">
                       <input type="checkbox" :checked="isAllSelected(paginatedComments)" @change="toggleAllSelection(paginatedComments)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                     </th>
                     <th class="px-6 py-4">ID</th>
                     <th class="px-6 py-4">用户</th>
                     <th class="px-6 py-4">评论内容</th>
                     <th class="px-6 py-4">关联文章</th>
                     <th class="px-6 py-4">时间</th>
                     <th class="px-6 py-4 text-right">操作</th>
                   </tr>
                 </thead>
                 <tbody class="divide-y divide-[#EFEBE9]">
                   <tr v-if="comments.length === 0">
                     <td colspan="7" class="px-6 py-10 text-center text-[#8D6E63] italic">暂无评论数据</td>
                   </tr>
                   <tr v-for="comment in paginatedComments" :key="comment.id" class="hover:bg-[#FAFAFA] transition-colors group">
                     <td class="px-6 py-4">
                       <input type="checkbox" :checked="selectedIds.has(comment.id)" @change="toggleSelection(comment.id)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                     </td>
                     <td class="px-6 py-4 font-mono text-xs font-bold text-[#8D6E63]">#{{ comment.id }}</td>
                     <td class="px-6 py-4">
                       <div class="flex items-center gap-3">
                         <img :src="comment.author?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-8 h-8 rounded-full bg-zinc-50 object-cover border border-zinc-100" />
                         <span class="font-bold text-[#3E2723]">{{ comment.author?.nickname || 'Unknown' }}</span>
                       </div>
                     </td>
                     <td class="px-6 py-4">
                        <div class="max-w-xs truncate" :title="comment.content">{{ comment.content }}</div>
                        <img v-if="comment.imageUrl" :src="comment.imageUrl" class="mt-2 h-10 w-10 object-cover rounded border border-zinc-200" />
                     </td>
                     <td class="px-6 py-4">
                        <span class="bg-[#FFF3E0] text-[#8D6E63] px-2 py-1 rounded text-xs font-bold">Post #{{ comment.post?.id }}</span>
                     </td>
                     <td class="px-6 py-4 text-[#8D6E63] text-xs font-medium">{{ formatDate(comment.createdAt) }}</td>
                     <td class="px-6 py-4 text-right">
                        <button @click="deleteComment(comment.id)" class="p-2 text-rose-300 hover:text-rose-500 hover:bg-rose-50 rounded-full transition-colors" title="删除">
                          <Trash2 class="w-4 h-4" />
                        </button>
                     </td>
                   </tr>
                 </tbody>
               </table>
             </div>
          </div>
          <!-- 评论分页 -->
          <Pagination 
            :current="commentPage" 
            :total="comments.length" 
            :page-size="commentPageSize"
            @change="(p) => commentPage = p"
            @update:page-size="(s) => commentPageSize = s"
          />
        </div>

        <!-- 订单标签页 -->
        <div v-if="activeTab === 'orders'" class="space-y-6">
          <div class="flex justify-between items-center">
            <div class="flex gap-2 overflow-x-auto pb-2">
               <button 
                 v-for="status in ['ALL', 'PENDING', 'SHIPPED', 'COMPLETED', 'CANCELLED']" 
                 :key="status"
                 @click="setOrderFilter(status)"
                 :class="['px-5 py-2 rounded-full text-xs font-bold transition-all whitespace-nowrap border', 
                   activeOrderFilter === status 
                     ? 'bg-rose-500 text-white border-rose-500 shadow-md shadow-rose-200' 
                     : 'bg-white text-[#8D6E63] border-[#EFEBE9] hover:border-rose-200 hover:text-rose-500']"
               >
                 {{ status === 'ALL' ? '全部订单' : status === 'PENDING' ? '待发货' : status === 'SHIPPED' ? '已发货' : status === 'COMPLETED' ? '已完成' : '已取消' }}
               </button>
            </div>
            <button 
              v-if="selectedIds.size > 0"
              @click="batchDeleteOrders"
              class="bg-rose-500 text-white px-4 py-2 rounded-lg font-bold shadow-sm hover:bg-rose-600 transition-all flex items-center gap-2 text-sm"
            >
              <Trash2 class="w-4 h-4" /> 批量删除 ({{ selectedIds.size }})
            </button>
          </div>

          <div class="bg-white rounded-3xl shadow-lg border border-[#EFEBE9] overflow-hidden">
             <div class="overflow-x-auto">
               <table class="w-full text-left text-sm text-[#5D4037]">
                 <thead class="bg-[#FFF8E1] border-b border-[#EFEBE9] font-bold uppercase tracking-wider">
                   <tr>
                     <th class="px-6 py-4 w-12">
                       <input type="checkbox" :checked="isAllSelected(paginatedOrders)" @change="toggleAllSelection(paginatedOrders)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                     </th>
                     <th class="px-6 py-4">订单号</th>
                     <th class="px-6 py-4">商品信息</th>
                     <th class="px-6 py-4">用户</th>
                     <th class="px-6 py-4">收货信息</th>
                     <th class="px-6 py-4">总金额</th>
                     <th class="px-6 py-4">状态</th>
                     <th class="px-6 py-4 text-right">操作</th>
                   </tr>
                 </thead>
                 <tbody class="divide-y divide-[#EFEBE9]">
                   <tr v-if="paginatedOrders.length === 0">
                     <td colspan="8" class="px-6 py-10 text-center text-[#8D6E63] italic">暂无相关订单</td>
                   </tr>
                   <tr v-for="order in paginatedOrders" :key="order.id" class="hover:bg-[#FAFAFA] transition-colors group">
                     <td class="px-6 py-4">
                       <input type="checkbox" :checked="selectedIds.has(order.id)" @change="toggleSelection(order.id)" class="rounded border-zinc-300 text-rose-500 focus:ring-rose-500 cursor-pointer" />
                     </td>
                     <td class="px-6 py-4 font-mono text-xs font-bold text-[#8D6E63]">#{{ order.id }}</td>
                     <td class="px-6 py-4">
                        <div class="space-y-2">
                           <div v-for="item in order.items" :key="item.id" class="flex items-center gap-2">
                              <img :src="item.product?.imageUrl" class="w-8 h-8 rounded object-cover border border-zinc-100" />
                              <div class="text-xs">
                                 <div class="font-bold text-[#3E2723]">{{ item.product?.name }}</div>
                                 <div class="text-[#8D6E63]">x{{ item.quantity }}</div>
                              </div>
                           </div>
                        </div>
                     </td>
                     <td class="px-6 py-4">
                        <div class="flex items-center gap-3">
                           <img :src="order.user?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-8 h-8 rounded-full bg-zinc-50 object-cover border border-zinc-100" />
                           <span class="font-bold text-[#3E2723]">{{ order.user?.nickname || 'Unknown' }}</span>
                        </div>
                     </td>
                     <td class="px-6 py-4">
                        <div class="text-xs">
                           <div class="font-bold">{{ order.contactName }} <span class="text-[#8D6E63] font-mono ml-1">{{ order.contactPhone }}</span></div>
                           <div class="text-[#8D6E63] mt-0.5 truncate max-w-xs" :title="order.shippingAddress">{{ order.shippingAddress }}</div>
                        </div>
                     </td>
                     <td class="px-6 py-4 font-black text-rose-500">¥{{ order.totalAmount }}</td>
                     <td class="px-6 py-4">
                        <span :class="['px-2 py-1 rounded text-xs font-bold', 
                             order.status === 'COMPLETED' ? 'bg-green-100 text-green-600' : 
                             order.status === 'SHIPPED' ? 'bg-blue-100 text-blue-600' : 
                             order.status === 'PENDING' ? 'bg-amber-100 text-amber-600' : 'bg-zinc-100 text-zinc-500']">
                             {{ order.status === 'PENDING' ? '待发货' : order.status === 'SHIPPED' ? '已发货' : order.status === 'COMPLETED' ? '已完成' : order.status === 'CANCELLED' ? '已取消' : order.status }}
                        </span>
                     </td>
                     <td class="px-6 py-4 text-right">
                        <div class="flex justify-end gap-2">
                           <button v-if="order.status === 'PENDING'" @click="updateOrderStatus(order.id, 'SHIPPED')" class="px-3 py-1 bg-blue-500 text-white rounded-lg text-xs font-bold shadow hover:bg-blue-600 transition-all">发货</button>
                           <button v-if="order.status === 'SHIPPED'" @click="updateOrderStatus(order.id, 'COMPLETED')" class="px-3 py-1 bg-green-500 text-white rounded-lg text-xs font-bold shadow hover:bg-green-600 transition-all">完成</button>
                           <button @click="deleteOrder(order.id)" class="px-3 py-1 bg-white border border-rose-200 text-rose-500 rounded-lg text-xs font-bold hover:bg-rose-50 transition-all">删除</button>
                        </div>
                     </td>
                   </tr>
                 </tbody>
               </table>
             </div>
          </div>

          <!-- 分页控制 -->
          <Pagination 
            :current="orderPage" 
            :total="filteredOrders.length" 
            :page-size="orderPageSize"
            @change="(p) => orderPage = p"
            @update:page-size="(s) => orderPageSize = s"
          />
        </div>

        <!-- 退款标签页 -->
        <div v-if="activeTab === 'refunds'" class="space-y-6">
          <div class="bg-white rounded-3xl shadow-lg border border-[#EFEBE9] overflow-hidden">
             <div class="overflow-x-auto">
               <table class="w-full text-left text-sm text-[#5D4037]">
                 <thead class="bg-[#FFF8E1] border-b border-[#EFEBE9] font-bold uppercase tracking-wider">
                   <tr>
                     <th class="px-6 py-4">订单号</th>
                     <th class="px-6 py-4">用户</th>
                     <th class="px-6 py-4">退款金额</th>
                     <th class="px-6 py-4">类型/原因</th>
                     <th class="px-6 py-4">状态/物流</th>
                     <th class="px-6 py-4 text-right">操作</th>
                   </tr>
                 </thead>
                 <tbody class="divide-y divide-[#EFEBE9]">
                   <tr v-if="refundOrders.length === 0">
                     <td colspan="6" class="px-6 py-10 text-center text-[#8D6E63] italic">暂无售后申请</td>
                   </tr>
                   <tr v-for="order in paginatedRefunds" :key="order.id" class="hover:bg-[#FAFAFA] transition-colors group">
                     <td class="px-6 py-4 font-mono text-xs font-bold text-[#8D6E63]">#{{ order.id }}</td>
                     <td class="px-6 py-4">
                        <div class="flex items-center gap-3">
                           <img :src="order.user?.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-8 h-8 rounded-full bg-zinc-50 object-cover border border-zinc-100" />
                           <div>
                              <div class="font-bold text-[#3E2723]">{{ order.user?.nickname || 'Unknown' }}</div>
                              <div class="text-xs text-[#8D6E63]">{{ order.contactPhone || '-' }}</div>
                           </div>
                        </div>
                     </td>
                     <td class="px-6 py-4 font-black text-rose-500">¥{{ order.totalAmount }}</td>
                     <td class="px-6 py-4">
                        <div class="space-y-1">
                           <span class="inline-block font-bold text-[10px] px-1.5 py-0.5 rounded border" :class="order.refundType === 'RETURN_AND_REFUND' ? 'bg-orange-50 text-orange-600 border-orange-200' : 'bg-blue-50 text-blue-600 border-blue-200'">{{ order.refundType === 'RETURN_AND_REFUND' ? '退货退款' : '仅退款' }}</span>
                           <div class="text-xs font-bold text-[#5D4037]">{{ order.refundReason }}</div>
                        </div>
                     </td>
                     <td class="px-6 py-4">
                        <div v-if="order.refundStatus === 'REFUND_APPLIED'" class="text-xs font-bold text-amber-500 bg-amber-50 px-2 py-1 rounded inline-block">待审核</div>
                        <div v-else-if="order.refundStatus === 'REFUND_APPROVED'">
                           <div v-if="order.refundType === 'RETURN_AND_REFUND'">
                              <div v-if="!order.returnStatus || order.returnStatus === 'WAITING_SHIPMENT'" class="text-xs text-orange-500 font-bold bg-orange-50 px-2 py-1 rounded inline-block">待买家寄回</div>
                              <div v-else-if="order.returnStatus === 'SHIPPED'" class="space-y-2">
                                 <div class="text-xs text-blue-600 font-bold bg-blue-50 px-2 py-1 rounded inline-block">买家已发货</div>
                                 <div class="text-[10px] text-[#8D6E63] bg-[#FFF8E1] p-2 rounded border border-[#EFEBE9]">
                                    <div class="font-bold">{{ order.returnLogisticsCompany }}</div>
                                    <div class="font-mono">{{ order.returnTrackingNumber }}</div>
                                 </div>
                                 <!-- 模拟地图 -->
                                 <div class="h-20 w-32 bg-zinc-100 rounded-lg overflow-hidden relative border border-zinc-200 shadow-sm group/map cursor-pointer">
                                    <img src="https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=simple%20map%20route%20logistics&image_size=landscape_16_9" class="w-full h-full object-cover opacity-80 group-hover/map:scale-110 transition-transform duration-500" />
                                    <div class="absolute inset-0 flex items-center justify-center bg-black/10">
                                      <div class="bg-white/90 px-2 py-1 rounded text-[10px] font-bold text-[#5D4037] shadow-sm">物流轨迹</div>
                                    </div>
                                 </div>
                              </div>
                              <div v-else-if="order.returnStatus === 'RECEIVED'" class="text-xs text-green-600 font-bold bg-green-50 px-2 py-1 rounded inline-block">已收货完成</div>
                           </div>
                           <div v-else class="text-xs text-green-600 font-bold bg-green-50 px-2 py-1 rounded inline-block">退款处理中</div>
                        </div>
                        <div v-else-if="order.refundStatus === 'REFUND_SUCCESS'" class="text-xs text-green-600 font-bold bg-green-50 px-2 py-1 rounded inline-block">退款成功</div>
                        <div v-else class="text-xs text-zinc-400 font-bold bg-zinc-50 px-2 py-1 rounded inline-block">已拒绝/取消</div>
                     </td>
                     <td class="px-6 py-4 text-right">
                        <div class="flex justify-end gap-2">
                           <template v-if="order.refundStatus === 'REFUND_APPLIED'">
                              <button @click="handleRefund(order.id, true)" class="px-3 py-1.5 bg-green-500 text-white rounded-lg text-xs font-bold shadow hover:bg-green-600 transition-all">同意</button>
                              <button @click="handleRefund(order.id, false)" class="px-3 py-1.5 bg-white border border-rose-200 text-rose-500 rounded-lg text-xs font-bold hover:bg-rose-50 transition-all">拒绝</button>
                           </template>
                           <template v-else-if="order.refundStatus === 'REFUND_APPROVED' && order.refundType === 'RETURN_AND_REFUND' && order.returnStatus === 'SHIPPED'">
                              <button @click="router.push(`/admin/return/${order.id}`)" class="px-3 py-1.5 bg-blue-500 text-white rounded-lg text-xs font-bold shadow hover:bg-blue-600 transition-all">查看退货物流</button>
                           </template>
                           <span v-else class="text-xs text-zinc-300 font-bold italic px-2">无操作</span>
                        </div>
                     </td>
                   </tr>
                 </tbody>
               </table>
             </div>
          </div>
          <!-- 退款分页 -->
          <Pagination 
            :current="refundPage" 
            :total="refundOrders.length" 
            :page-size="refundPageSize"
            @change="(p) => refundPage = p"
            @update:page-size="(s) => refundPageSize = s"
          />
        </div>

        <!-- 帖子标签页（已更新网格和管理） -->
        <div v-if="activeTab === 'posts'">
           <div class="flex justify-between items-center mb-6">
             <div class="flex gap-2">
               <button 
                 v-for="filter in [
                   { key: 'ALL', label: '全部文章' },
                   { key: 'ADMIN', label: '官方/管理员' },
                   { key: 'USER', label: '用户/社区' }
                 ]"
                 :key="filter.key"
                 @click="setPostFilter(filter.key)"
                 :class="['px-5 py-2 rounded-full text-xs font-bold transition-all border', 
                   activePostFilter === filter.key 
                     ? 'bg-rose-500 text-white border-rose-500 shadow-md shadow-rose-200' 
                     : 'bg-white text-[#8D6E63] border-[#EFEBE9] hover:border-rose-200 hover:text-rose-500']"
               >
                 {{ filter.label }}
               </button>
             </div>
            <button @click="openPostModal()" class="bg-rose-500 text-white px-6 py-2.5 rounded-full font-bold shadow-lg shadow-rose-200 hover:bg-rose-600 hover:scale-105 transition-all flex items-center gap-2">
              <Plus class="w-4 h-4" /> 发布文章
            </button>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
             <div v-for="post in paginatedPosts" :key="post.id" class="bg-white rounded-2xl overflow-hidden border border-[#EFEBE9] hover:border-rose-200 transition-all group shadow-lg flex flex-col relative">
                <div v-if="post.author?.role === 'ADMIN'" class="absolute top-2 right-2 z-10 bg-rose-500 text-white text-[10px] font-bold px-2 py-1 rounded-lg shadow-sm">OFFICIAL</div>
                <div v-else class="absolute top-2 right-2 z-10 bg-blue-500 text-white text-[10px] font-bold px-2 py-1 rounded-lg shadow-sm">USER</div>
                
                <div class="h-48 bg-[#FAFAFA] relative overflow-hidden">
                   <img :src="post.coverImage" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                   <div class="absolute bottom-2 left-2 bg-white/90 backdrop-blur px-2 py-1 rounded text-xs font-bold text-[#5D4037] shadow-sm">{{ post.category }}</div>
                </div>
                <div class="p-4 flex-1 flex flex-col">
                   <h3 class="font-bold text-[#3E2723] text-lg mb-2 line-clamp-1" :title="post.title">{{ post.title }}</h3>
                   <div class="flex items-center gap-2 mb-4">
                      <img :src="post.author?.avatar" class="w-5 h-5 rounded-full" />
                      <span class="text-xs text-[#8D6E63]">{{ post.author?.nickname }}</span>
                   </div>
                   <div class="mt-auto flex gap-2 pt-3 border-t border-[#EFEBE9]">
                      <button @click="openPostModal(post)" class="flex-1 py-2 bg-[#FAFAFA] text-[#8D6E63] rounded-lg text-xs font-bold hover:bg-blue-50 hover:text-blue-600 transition-colors">编辑</button>
                      <button @click="deletePost(post.id)" class="flex-1 py-2 bg-[#FAFAFA] text-[#8D6E63] rounded-lg text-xs font-bold hover:bg-rose-50 hover:text-rose-500 transition-colors">删除</button>
                   </div>
                </div>
             </div>
          </div>
          <!-- 帖子分页 -->
          <Pagination 
            :current="postPage" 
            :total="filteredPosts.length" 
            :page-size="postPageSize"
            @change="(p) => postPage = p"
            @update:page-size="(s) => postPageSize = s"
          />
        </div>
        <!-- 聊天标签页 -->
        <div v-if="activeTab === 'chat'" class="h-[calc(100vh-200px)] bg-white rounded-3xl shadow-lg border border-[#EFEBE9] overflow-hidden flex">
           <!-- 联系人列表 -->
           <div class="w-80 border-r border-[#EFEBE9] flex flex-col bg-[#FFF8E1]/30">
              <div class="p-4 border-b border-[#EFEBE9]">
                 <h3 class="font-bold text-[#5D4037]">最近联系人</h3>
              </div>
              <div class="flex-1 overflow-y-auto">
                 <div v-if="chatContacts.length === 0" class="p-8 text-center text-[#8D6E63] text-sm italic">
                    暂无消息记录
                 </div>
                 <button 
                   v-for="contact in chatContacts" 
                   :key="contact.id"
                   @click="selectContact(contact)"
                   class="w-full p-4 flex items-center gap-3 hover:bg-[#FFF3E0] transition-colors text-left border-b border-[#EFEBE9]/50 relative"
                   :class="selectedContact?.id === contact.id ? 'bg-[#FFF3E0] border-l-4 border-l-rose-500' : 'border-l-4 border-l-transparent'"
                 >
                    <div class="relative">
                        <img :src="contact.avatar || 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar&image_size=square'" class="w-10 h-10 rounded-full bg-zinc-50 object-cover border border-zinc-100" />
                        <div v-if="contact.unreadCount > 0" class="absolute -top-1 -right-1 w-3 h-3 bg-rose-500 rounded-full border border-white"></div>
                    </div>
                    <div class="flex-1 min-w-0">
                       <div class="font-bold text-[#3E2723] truncate flex justify-between">
                           {{ contact.nickname }}
                           <span class="text-[10px] text-[#8D6E63] font-normal">{{ contact.lastMessageTime ? new Date(contact.lastMessageTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : '' }}</span>
                       </div>
                       <div class="text-xs text-[#8D6E63] truncate">{{ contact.lastMessage || '点击查看消息记录' }}</div>
                    </div>
                 </button>
              </div>
           </div>
           
           <!-- 聊天区域 -->
           <div class="flex-1 bg-zinc-50">
              <div v-if="selectedContact" class="h-full">
                 <ChatBox 
                   :key="selectedContact.id" 
                   :target-user-id="selectedContact.id" 
                   :available-orders="orders"
                   @close="selectedContact = null"
                   @messages-read="handleMessagesRead(selectedContact.id)"
                 />
              </div>
              <div v-else class="h-full flex items-center justify-center text-[#8D6E63] flex-col gap-4">
                 <MessageCircle class="w-16 h-16 opacity-20" />
                 <p>请选择左侧联系人开始沟通</p>
              </div>
           </div>
        </div>
      </div>
    </div>

    <!-- 商品弹窗 -->
    <div v-if="showProductModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg p-6 shadow-2xl animate-in fade-in zoom-in duration-200">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-xl font-bold text-[#3E2723]">{{ editingProduct ? '编辑商品' : '添加商品' }}</h3>
          <button @click="showProductModal = false" class="p-2 hover:bg-[#FAFAFA] rounded-full text-[#8D6E63]"><X class="w-5 h-5" /></button>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">商品名称</label>
            <input v-model="productForm.name" type="text" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]" />
          </div>
          <div class="grid grid-cols-2 gap-4">
             <div>
              <label class="block text-sm font-bold text-[#5D4037] mb-1">价格</label>
              <input v-model="productForm.price" type="number" step="0.01" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]" />
            </div>
            <div>
              <label class="block text-sm font-bold text-[#5D4037] mb-1">库存</label>
              <input v-model="productForm.stock" type="number" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">分类</label>
             <select v-model="productForm.category" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]">
               <option>工具</option>
               <option>食品</option>
               <option>食材</option>
               <option>文创</option>
               <option>茶具</option>
             </select>
          </div>
          <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">图片</label>
            <ImageUpload v-model="productForm.imageUrl" />
          </div>
          <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">描述</label>
            <textarea v-model="productForm.description" rows="3" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]"></textarea>
          </div>
        </div>

        <div class="mt-8 flex justify-end gap-3">
          <button @click="showProductModal = false" class="px-6 py-2 border border-[#D7CCC8] rounded-lg text-[#8D6E63] hover:bg-[#FAFAFA] font-bold">取消</button>
          <button @click="saveProduct" class="px-6 py-2 bg-rose-500 text-white rounded-lg hover:bg-rose-600 font-bold shadow-lg shadow-rose-200">保存</button>
        </div>
      </div>
    </div>

    <!-- 帖子弹窗 -->
    <div v-if="showPostModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl w-full max-w-2xl p-6 shadow-2xl animate-in fade-in zoom-in duration-200 h-[80vh] flex flex-col">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-xl font-bold text-[#3E2723]">{{ editingPost ? '编辑文章' : '发布文章' }}</h3>
          <button @click="showPostModal = false" class="p-2 hover:bg-[#FAFAFA] rounded-full text-[#8D6E63]"><X class="w-5 h-5" /></button>
        </div>
        
        <div class="space-y-4 flex-1 overflow-y-auto pr-2">
          <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">文章标题</label>
            <input v-model="postForm.title" type="text" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]" />
          </div>
           <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">分类</label>
             <select v-model="postForm.category" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723]">
               <option>茶饮</option>
               <option>烘焙</option>
               <option>花草</option>
               <option>家居</option>
               <option>穿搭</option>
               <option>生活</option>
               <option>美食</option>
               <option>手作</option>
             </select>
          </div>
          <div>
            <label class="block text-sm font-bold text-[#5D4037] mb-1">封面图片</label>
            <ImageUpload v-model="postForm.coverImage" />
          </div>
          <div class="flex-1">
            <label class="block text-sm font-bold text-[#5D4037] mb-1">内容</label>
            <textarea v-model="postForm.content" rows="10" class="w-full px-4 py-2 bg-[#FAFAFA] border border-[#EFEBE9] rounded-lg focus:ring-rose-500 focus:border-rose-500 text-[#3E2723] h-full min-h-[200px]"></textarea>
          </div>
        </div>

        <div class="mt-8 flex justify-end gap-3 pt-4 border-t border-[#EFEBE9]">
          <button @click="showPostModal = false" class="px-6 py-2 border border-[#D7CCC8] rounded-lg text-[#8D6E63] hover:bg-[#FAFAFA] font-bold">取消</button>
          <button @click="savePost" class="px-6 py-2 bg-rose-500 text-white rounded-lg hover:bg-rose-600 font-bold shadow-lg shadow-rose-200">发布</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.chart {
  height: 100%;
  width: 100%;
}
</style>
