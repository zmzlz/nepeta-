<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { X, MapPin, Phone, User, Home } from 'lucide-vue-next'
import { addressApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import { regionData, codeToText } from 'element-china-area-data'

const props = defineProps<{
  isOpen: boolean
  editingAddress?: any
}>()

const emit = defineEmits(['close', 'refresh'])

const userStore = useUserStore()
const toast = useToast()
const loading = ref(false)

const form = ref({
  recipientName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

// 区域选择状态
const selectedProvince = ref('')
const selectedCity = ref('')
const selectedDistrict = ref('')

// 基于选择的计算选项
const provinceOptions = computed(() => regionData)
const cityOptions = computed(() => {
  if (!selectedProvince.value) return []
  const province = regionData.find(p => p.value === selectedProvince.value)
  return province ? province.children : []
})
const districtOptions = computed(() => {
  if (!selectedCity.value) return []
  const province = regionData.find(p => p.value === selectedProvince.value)
  if (!province) return []
  const city = province.children.find(c => c.value === selectedCity.value)
  return city ? city.children : []
})

const isEditing = computed(() => !!props.editingAddress)

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    if (props.editingAddress) {
      form.value = { ...props.editingAddress }
      // 如果我们只存储了名称，这里可能需要反向查找代码
      // 但为了简单起见，如果没有代码，用户需要重新选择
      // 理想情况下后端也存储代码，或者我们尝试匹配名称
      // 假设目前用户重新选择，或者如果匹配失败我们只显示文本输入
      // 或者我们只保留简单的文本输入用于显示，但允许下拉选择覆盖？
      // 尝试在名称匹配时设置下拉框
      
      // 尝试从名称查找代码（简单逻辑，如果是大数据集可能较慢，但对客户端可接受）
      // 实际上 element-china-area-data 提供 CodeToText 但没有直接提供不需遍历的 TextToCode
      // 目前先重置选择或留空，用户需要时可以更新
      // 或者更好：如果无法匹配则不重置，只显示文本 
      // 但我们正在用下拉框替换文本输入
      // 所以我们必须尝试匹配或让用户重新选择
      
      selectedProvince.value = ''
      selectedCity.value = ''
      selectedDistrict.value = ''
      
      // 尝试匹配省份
      const p = regionData.find(item => item.label === form.value.province)
      if (p) {
        selectedProvince.value = p.value
        // 尝试匹配城市
        const c = p.children?.find(item => item.label === form.value.city)
        if (c) {
          selectedCity.value = c.value
          // 尝试匹配区县
          const d = c.children?.find(item => item.label === form.value.district)
          if (d) {
            selectedDistrict.value = d.value
          }
        }
      }

    } else {
      form.value = {
        recipientName: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: false
      }
      selectedProvince.value = ''
      selectedCity.value = ''
      selectedDistrict.value = ''
    }
  }
})

// 选择改变时更新表单文本
watch(selectedProvince, (val) => {
  if (val) {
    form.value.province = codeToText[val]
    selectedCity.value = '' // 重置城市
    selectedDistrict.value = '' // 重置区县
  }
})
watch(selectedCity, (val) => {
  if (val) {
    form.value.city = codeToText[val]
    selectedDistrict.value = '' // 重置区县
  }
})
watch(selectedDistrict, (val) => {
  if (val) {
    form.value.district = codeToText[val]
  }
})

const handleSubmit = async () => {
  if (!userStore.user?.id) return

  // 基础验证
  if (!form.value.recipientName || !form.value.phone || !form.value.detailAddress) {
    toast.warning('请填写完整信息')
    return
  }
  
  if (!form.value.province || !form.value.city || !form.value.district) {
    toast.warning('请选择完整的地区信息')
    return
  }

  loading.value = true
  try {
    if (isEditing.value) {
      await addressApi.updateAddress(props.editingAddress.id, form.value)
      toast.success('地址更新成功')
    } else {
      await addressApi.addAddress(userStore.user.id, form.value)
      toast.success('地址添加成功')
    }
    emit('refresh')
    emit('close')
  } catch (error) {
    console.error('Failed to save address:', error)
    toast.error('保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-[60] flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm transition-opacity" @click="$emit('close')"></div>
    
    <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl relative overflow-hidden animate-in zoom-in duration-200">
      <div class="p-6 border-b border-zinc-100 flex items-center justify-between bg-zinc-50">
        <h3 class="text-xl font-black text-zinc-800 flex items-center gap-2">
          <MapPin class="w-5 h-5 text-rose-500" />
          {{ isEditing ? '编辑地址' : '新增地址' }}
        </h3>
        <button @click="$emit('close')" class="p-2 hover:bg-zinc-200 rounded-full transition-colors text-zinc-500">
          <X class="w-5 h-5" />
        </button>
      </div>

      <div class="p-8 space-y-5">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label class="text-xs font-bold text-zinc-500 ml-1">联系人</label>
            <div class="relative">
              <User class="w-4 h-4 text-zinc-400 absolute left-3 top-3" />
              <input v-model="form.recipientName" type="text" placeholder="姓名" class="w-full pl-9 pr-4 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-300 transition-all font-medium" />
            </div>
          </div>
          <div class="space-y-1.5">
            <label class="text-xs font-bold text-zinc-500 ml-1">手机号</label>
            <div class="relative">
              <Phone class="w-4 h-4 text-zinc-400 absolute left-3 top-3" />
              <input v-model="form.phone" type="tel" placeholder="手机号码" class="w-full pl-9 pr-4 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-300 transition-all font-medium" />
            </div>
          </div>
        </div>

        <div class="space-y-1.5">
          <label class="text-xs font-bold text-zinc-500 ml-1">地区信息</label>
          <div class="grid grid-cols-3 gap-2">
            <!-- 省份 -->
            <div class="relative">
               <select v-model="selectedProvince" class="w-full px-3 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl focus:ring-2 focus:ring-rose-200 text-sm appearance-none pr-8">
                  <option value="" disabled>选择省份</option>
                  <option v-for="item in provinceOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
               </select>
               <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-zinc-500">
                  <svg class="h-4 w-4 fill-current" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/></svg>
               </div>
            </div>

            <!-- 城市 -->
            <div class="relative">
               <select v-model="selectedCity" :disabled="!selectedProvince" class="w-full px-3 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl focus:ring-2 focus:ring-rose-200 text-sm appearance-none pr-8 disabled:opacity-50">
                  <option value="" disabled>选择城市</option>
                  <option v-for="item in cityOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
               </select>
               <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-zinc-500">
                  <svg class="h-4 w-4 fill-current" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/></svg>
               </div>
            </div>

            <!-- 区县 -->
            <div class="relative">
               <select v-model="selectedDistrict" :disabled="!selectedCity" class="w-full px-3 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl focus:ring-2 focus:ring-rose-200 text-sm appearance-none pr-8 disabled:opacity-50">
                  <option value="" disabled>选择区县</option>
                  <option v-for="item in districtOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
               </select>
               <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-zinc-500">
                  <svg class="h-4 w-4 fill-current" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/></svg>
               </div>
            </div>
          </div>
        </div>

        <div class="space-y-1.5">
          <label class="text-xs font-bold text-zinc-500 ml-1">详细地址</label>
          <div class="relative">
            <Home class="w-4 h-4 text-zinc-400 absolute left-3 top-3" />
            <textarea v-model="form.detailAddress" rows="2" placeholder="街道、楼牌号等" class="w-full pl-9 pr-4 py-2.5 bg-zinc-50 border border-zinc-200 rounded-xl focus:ring-2 focus:ring-rose-200 focus:border-rose-300 transition-all font-medium resize-none"></textarea>
          </div>
        </div>

        <div class="flex items-center gap-3 pt-2">
          <input type="checkbox" id="isDefault" v-model="form.isDefault" class="w-4 h-4 text-rose-500 rounded border-zinc-300 focus:ring-rose-500" />
          <label for="isDefault" class="text-sm font-bold text-zinc-700 cursor-pointer select-none">设为默认地址</label>
        </div>
      </div>

      <div class="p-6 border-t border-zinc-100 bg-zinc-50 flex justify-end gap-3">
        <button @click="$emit('close')" class="px-6 py-2.5 border border-zinc-300 text-zinc-600 rounded-xl font-bold hover:bg-white hover:border-zinc-400 transition-all">取消</button>
        <button @click="handleSubmit" :disabled="loading" class="px-8 py-2.5 bg-rose-500 text-white rounded-xl font-bold hover:bg-rose-600 shadow-lg shadow-rose-200 transition-all disabled:opacity-50 flex items-center gap-2">
          <span v-if="loading" class="animate-spin w-4 h-4 border-2 border-white/30 border-t-white rounded-full"></span>
          保存
        </button>
      </div>
    </div>
  </div>
</template>
