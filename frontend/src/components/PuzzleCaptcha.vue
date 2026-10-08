<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { RefreshCw } from 'lucide-vue-next'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits(['success', 'fail', 'close'])

const canvasRef = ref<HTMLCanvasElement | null>(null)
const blockRef = ref<HTMLCanvasElement | null>(null)
const sliderRef = ref<HTMLDivElement | null>(null)
const sliderContainerRef = ref<HTMLDivElement | null>(null)

const isDragging = ref(false)
const sliderLeft = ref(0)
const sliderMaskWidth = ref(0)
const isLoading = ref(false)

// 配置
const width = 320
const height = 160
const l = 42 // 拼图块边长
const r = 10 // 拼图块圆半径
const PI = Math.PI
const L = l + r * 2 // 实际块宽度

let x = 0 // 随机 x 位置
let y = 0 // 随机 y 位置

const captchaImages = [
  '/avatars/72.jpg',
  '/avatars/73.jpg',
  '/avatars/74.jpg',
  '/avatars/75.jpg',
  '/avatars/76.jpg',
  '/avatars/85.png',
  '/avatars/86.png',
  '/avatars/87.png'
]

const imageCache = new Map<string, HTMLImageElement>()
let currentImage: HTMLImageElement | null = null

const loadImage = (src: string) => {
  if (imageCache.has(src)) {
    return Promise.resolve(imageCache.get(src)!)
  }

  return new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image()
    image.crossOrigin = 'anonymous'
    image.onload = () => {
      imageCache.set(src, image)
      resolve(image)
    }
    image.onerror = () => reject(new Error('image load failed'))
    image.src = src
  })
}

const getRandomNumberByRange = (start: number, end: number) => {
  return Math.round(Math.random() * (end - start) + start)
}

const createCanvas = (c: HTMLCanvasElement, width: number, height: number) => {
  c.width = width
  c.height = height
  const ctx = c.getContext('2d')
  if (!ctx) return null
  ctx.clearRect(0, 0, width, height)
  return ctx
}

const drawPath = (ctx: CanvasRenderingContext2D, x: number, y: number, operation: 'fill' | 'clip') => {
  ctx.beginPath()
  ctx.moveTo(x, y)
  ctx.arc(x + l / 2, y - r + 2, r, 0.72 * PI, 2.26 * PI)
  ctx.lineTo(x + l, y)
  ctx.arc(x + l + r - 2, y + l / 2, r, 1.21 * PI, 2.78 * PI)
  ctx.lineTo(x + l, y + l)
  ctx.lineTo(x, y + l)
  ctx.arc(x + r - 2, y + l / 2, r + 0.4, 2.76 * PI, 1.24 * PI, true)
  ctx.lineTo(x, y)
  ctx.lineWidth = 2
  ctx.fillStyle = 'rgba(255, 255, 255, 0.7)'
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)'
  ctx.stroke()
  ctx.globalCompositeOperation = 'destination-over'
  if (operation === 'fill') {
    ctx.fill()
  } else {
    ctx.clip()
  }
}

const initImg = () => {
  isLoading.value = true
  const randomIndex = getRandomNumberByRange(0, captchaImages.length - 1)
  const src = captchaImages[randomIndex]

  loadImage(src).then((image) => {
    currentImage = image
    isLoading.value = false
    draw()
  }).catch(() => {
    isLoading.value = false
    reset()
  })
}

const draw = () => {
  if (!canvasRef.value || !blockRef.value || !currentImage) return
  
  // 随机位置
  x = getRandomNumberByRange(L + 10, width - (L + 10))
  y = getRandomNumberByRange(10 + r * 2, height - (L + 10))
  
  const ctx = createCanvas(canvasRef.value, width, height)
  const blockCtx = createCanvas(blockRef.value, width, height)
  
  if (!ctx || !blockCtx) return

  ctx.drawImage(currentImage, 0, 0, width, height)
  
  drawPath(ctx, x, y, 'fill')
  blockCtx.drawImage(currentImage, 0, 0, width, height)
  drawPath(blockCtx, x, y, 'clip')

  const yPos = y - r * 2 - 1
  const ImageData = blockCtx.getImageData(x, yPos, L, L)
  blockRef.value.width = L
  blockCtx.putImageData(ImageData, 0, yPos)
}

const reset = () => {
  sliderLeft.value = 0
  sliderMaskWidth.value = 0
  isDragging.value = false
  initImg()
}

const handleDragStart = (e: MouseEvent | TouchEvent) => {
  if (isLoading.value) return
  isDragging.value = true
}

const handleDragMove = (e: MouseEvent | TouchEvent) => {
  if (!isDragging.value || !sliderContainerRef.value) return
  
  let clientX = 0
  if (e instanceof MouseEvent) {
    clientX = e.clientX
  } else {
    clientX = e.touches[0].clientX
  }
  
  const rect = sliderContainerRef.value.getBoundingClientRect()
  let moveX = clientX - rect.left - 20 // 20 is half slider width
  
  if (moveX < 0) moveX = 0
  if (moveX > width - 40) moveX = width - 40
  
  sliderLeft.value = moveX
  sliderMaskWidth.value = moveX + 20
}

const handleDragEnd = () => {
  if (!isDragging.value) return
  isDragging.value = false
  
  // 验证
  const diff = Math.abs(sliderLeft.value - x)
  if (diff < 5) {
    // 成功
    emit('success')
  } else {
    // 失败
    emit('fail')
    setTimeout(() => {
      sliderLeft.value = 0
      sliderMaskWidth.value = 0
    }, 400)
  }
}

watch(() => props.show, (val) => {
  if (val) {
    setTimeout(() => {
        reset()
    }, 100)
  }
})

onMounted(() => {
  captchaImages.forEach((src) => {
    loadImage(src).catch(() => {})
  })

  if (props.show) {
    reset()
  }

  window.addEventListener('mousemove', handleDragMove)
  window.addEventListener('mouseup', handleDragEnd)
  window.addEventListener('touchmove', handleDragMove)
  window.addEventListener('touchend', handleDragEnd)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', handleDragMove)
  window.removeEventListener('mouseup', handleDragEnd)
  window.removeEventListener('touchmove', handleDragMove)
  window.removeEventListener('touchend', handleDragEnd)
})
</script>

<template>
  <div v-if="show" class="fixed inset-0 z-[60] flex items-center justify-center bg-black/20 backdrop-blur-sm animate-in fade-in">
    <div class="bg-white p-4 rounded-2xl shadow-2xl w-[352px] relative">
       <div class="mb-3 flex justify-between items-center">
         <h3 class="font-bold text-zinc-700">安全验证</h3>
         <div class="flex gap-2">
            <button @click="reset" class="p-1 hover:bg-zinc-100 rounded text-zinc-400 hover:text-rose-500 transition-colors" title="刷新">
                <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': isLoading }" />
            </button>
            <button @click="$emit('close')" class="p-1 hover:bg-zinc-100 rounded text-zinc-400 hover:text-zinc-600 transition-colors">
                <span class="text-lg leading-none">&times;</span>
            </button>
         </div>
       </div>

       <div class="relative w-[320px] h-[160px] bg-zinc-100 rounded-lg overflow-hidden mb-4 shadow-inner">
         <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center bg-zinc-100 z-20">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-rose-500"></div>
         </div>
         <canvas ref="canvasRef" :width="width" :height="height" class="w-full h-full block"></canvas>
         <canvas ref="blockRef" :width="width" :height="height" class="absolute top-0 left-0 h-full z-10 block" :style="{ left: sliderLeft + 'px' }"></canvas>
       </div>

       <div ref="sliderContainerRef" class="relative w-full h-10 bg-zinc-100 rounded-full shadow-inner border border-zinc-200">
         <div class="absolute top-0 left-0 h-full bg-rose-100 rounded-full border border-rose-200 transition-all duration-0" :style="{ width: sliderMaskWidth + 'px' }"></div>
         <div class="absolute top-0 left-0 w-full h-full flex items-center justify-center text-xs text-zinc-400 select-none pointer-events-none">
            向右滑动完成拼图
         </div>
         <div 
            ref="sliderRef"
            class="absolute top-0 left-0 w-10 h-10 bg-white rounded-full shadow-md border border-zinc-200 cursor-grab active:cursor-grabbing flex items-center justify-center hover:shadow-lg hover:border-rose-200 transition-all duration-0 z-20"
            :style="{ left: sliderLeft + 'px' }"
            @mousedown="handleDragStart"
            @touchstart="handleDragStart"
         >
           <div class="w-4 h-4 border-r-2 border-b-2 border-rose-400 rotate-[-45deg] ml-[-2px]"></div>
         </div>
       </div>
    </div>
  </div>
</template>
