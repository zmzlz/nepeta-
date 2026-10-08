<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-vue-next'

const props = defineProps<{
  current: number
  total: number
  pageSize: number
}>()

const emit = defineEmits(['change', 'update:pageSize'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize))

const pages = computed(() => {
  const current = props.current
  const total = totalPages.value
  const maxVisible = 5
  
  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  
  // 显示页码的逻辑，例如: 1 ... 4 5 6 ... 10
  const pages: (number | string)[] = []
  
  if (current <= 3) {
    for (let i = 1; i <= 4; i++) pages.push(i)
    pages.push('...')
    pages.push(total)
  } else if (current >= total - 2) {
    pages.push(1)
    pages.push('...')
    for (let i = total - 3; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    pages.push('...')
    pages.push(current - 1)
    pages.push(current)
    pages.push(current + 1)
    pages.push('...')
    pages.push(total)
  }
  
  return pages
})

const handlePageChange = (page: number | string) => {
  if (typeof page === 'string') return
  if (page < 1 || page > totalPages.value) return
  emit('change', page)
}

const handlePageSizeChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  emit('update:pageSize', parseInt(target.value))
  emit('change', 1) // 大小改变时重置到第一页
}
</script>

<template>
  <div class="flex flex-col sm:flex-row items-center justify-between gap-4 py-4 px-2 select-none">
    <div class="flex items-center gap-2 text-sm text-[#8D6E63]">
      <span>每页显示</span>
      <select 
        :value="pageSize" 
        @change="handlePageSizeChange"
        class="bg-white border border-[#EFEBE9] rounded-lg px-2 py-1 focus:outline-none focus:border-rose-500 text-[#5D4037] font-bold"
      >
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
        <option :value="100">100</option>
      </select>
      <span>条，共 {{ total }} 条</span>
    </div>

    <div class="flex items-center gap-1">
      <button 
        @click="handlePageChange(1)"
        :disabled="current === 1"
        class="p-2 rounded-lg hover:bg-[#FFF3E0] text-[#8D6E63] disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
        title="第一页"
      >
        <ChevronsLeft class="w-4 h-4" />
      </button>
      <button 
        @click="handlePageChange(current - 1)"
        :disabled="current === 1"
        class="p-2 rounded-lg hover:bg-[#FFF3E0] text-[#8D6E63] disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
        title="上一页"
      >
        <ChevronLeft class="w-4 h-4" />
      </button>

      <div class="flex items-center gap-1 mx-2">
        <button 
          v-for="page in pages" 
          :key="page"
          @click="handlePageChange(page)"
          :disabled="page === '...'"
          :class="[
            'w-8 h-8 rounded-lg text-sm font-bold transition-all flex items-center justify-center',
            page === current 
              ? 'bg-rose-500 text-white shadow-md shadow-rose-200' 
              : page === '...' 
                ? 'text-[#8D6E63] cursor-default' 
                : 'text-[#5D4037] hover:bg-[#FFF3E0] border border-transparent hover:border-rose-200'
          ]"
        >
          {{ page }}
        </button>
      </div>

      <button 
        @click="handlePageChange(current + 1)"
        :disabled="current === totalPages"
        class="p-2 rounded-lg hover:bg-[#FFF3E0] text-[#8D6E63] disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
        title="下一页"
      >
        <ChevronRight class="w-4 h-4" />
      </button>
      <button 
        @click="handlePageChange(totalPages)"
        :disabled="current === totalPages"
        class="p-2 rounded-lg hover:bg-[#FFF3E0] text-[#8D6E63] disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
        title="最后一页"
      >
        <ChevronsRight class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>
