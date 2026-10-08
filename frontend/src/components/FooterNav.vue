<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Instagram, Twitter, Facebook, Globe, MessageCircle, Youtube } from 'lucide-vue-next'

const route = useRoute()

const footerConfig = computed(() => {
 if (route.path.startsWith('/market')) {
    return {
      style: 'bg-gradient-to-r from-pastel-lilac via-pastel-pink to-pastel-blue text-dark-800',
      titleColor: 'text-dark-900',
      subColor: 'text-dark-800',
      linkColor: 'text-dark-800 hover:text-primary-500',
      iconBg: 'bg-white text-dark-500 hover:text-primary-500',
      copyrightColor: 'text-dark-500 border-white/50',
      showStickers: false
    }

  } else {
    // 首页默认样式
    return {
      style: 'bg-primary-50 bg-dots text-dark-900', // 粉色圆点图案
      titleColor: 'text-primary-700',
      subColor: 'text-dark-500',
      linkColor: 'text-dark-500 hover:text-primary-500',
      iconBg: 'bg-white text-primary-400 hover:text-primary-500 shadow-sm',
      copyrightColor: 'text-primary-300 border-primary-200',
      showStickers: true
    }
  }
})
</script>

<template>
  <footer :class="['pt-16 pb-8 mt-0 w-full relative overflow-hidden', footerConfig.style]">
    
    <!-- 首页专属的悬浮贴纸动画 -->
    <div v-if="footerConfig.showStickers" class="absolute top-10 right-10 hidden md:block animate-bounce duration-[3000ms]">
      <div class="bg-secondary-yellow text-white px-4 py-2 rounded-tr-xl rounded-tl-xl rounded-bl-xl text-sm font-bold shadow-lg transform rotate-12">
        Hi!
      </div>
    </div>
    <div v-if="footerConfig.showStickers" class="absolute top-20 left-20 hidden md:block animate-bounce duration-[4000ms]">
      <div class="bg-primary-500 text-white px-4 py-2 rounded-tr-xl rounded-tl-xl rounded-br-xl text-sm font-bold shadow-lg transform -rotate-12">
        Yay!
      </div>
    </div>

    <div class="container mx-auto px-6 md:px-12 relative z-10">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
        <div class="space-y-6">
          <h3 :class="['font-extrabold text-2xl flex items-center gap-2', footerConfig.titleColor]">
            <span v-if="!route.path.startsWith('/classroom')" class="bg-white border border-zinc-100 p-1 rounded-lg shadow-sm w-8 h-8 flex items-center justify-center overflow-hidden">
              <img src="/ico (1).png" alt="Logo" class="w-full h-full object-contain scale-110" />
            </span>
            Taogueyuan
          </h3>
          <p :class="['text-sm leading-relaxed font-medium', footerConfig.subColor]">
            致力于传播潮汕粿品文化，让传统美食与现代生活完美融合。<br/>
            Made with ❤️ for Kueh Lovers.
          </p>
        </div>
        
        <div>
          <h4 :class="['font-bold mb-6 text-lg', footerConfig.titleColor]">关于我们</h4>
          <ul class="space-y-3 text-sm font-medium">
            <li><a href="#" :class="['transition-colors', footerConfig.linkColor]">品牌故事</a></li>
            <li><a href="#" :class="['transition-colors', footerConfig.linkColor]">加入我们</a></li>
            <li><a href="#" :class="['transition-colors', footerConfig.linkColor]">联系方式</a></li>
          </ul>
        </div>

        <div>
          <h4 :class="['font-bold mb-6 text-lg', footerConfig.titleColor]">帮助中心</h4>
          <ul class="space-y-3 text-sm font-medium">
            <li><a href="#" :class="['transition-colors', footerConfig.linkColor]">购物指南</a></li>
            <li><a href="#" :class="['transition-colors', footerConfig.linkColor]">支付方式</a></li>
            <li><a href="#" :class="['transition-colors', footerConfig.linkColor]">物流配送</a></li>
          </ul>
        </div>

        <div>
          <h4 :class="['font-bold mb-6 text-lg', footerConfig.titleColor]">关注我们</h4>
          <div class="flex gap-4">
            <a href="#" :class="['w-10 h-10 rounded-full flex items-center justify-center transition-all hover:scale-110', footerConfig.iconBg]">
              <Instagram class="w-5 h-5" />
            </a>
            <a href="#" :class="['w-10 h-10 rounded-full flex items-center justify-center transition-all hover:scale-110', footerConfig.iconBg]">
              <Twitter class="w-5 h-5" />
            </a>
            <a href="#" :class="['w-10 h-10 rounded-full flex items-center justify-center transition-all hover:scale-110', footerConfig.iconBg]">
              <Facebook class="w-5 h-5" />
            </a>
             <a href="#" :class="['w-10 h-10 rounded-full flex items-center justify-center transition-all hover:scale-110', footerConfig.iconBg]">
              <MessageCircle class="w-5 h-5" />
            </a>
          </div>
        </div>
      </div>

      <div :class="['border-t pt-8 flex flex-col md:flex-row justify-between items-center text-xs font-medium', footerConfig.copyrightColor]">
        <p>&copy; 2026 桃粿源 Taogueyuan. All rights reserved.</p>
        <div class="flex gap-6 mt-4 md:mt-0">
          <a href="#" class="hover:underline">Privacy Policy</a>
          <a href="#" class="hover:underline">Terms of Service</a>
        </div>
      </div>
    </div>
  </footer>
</template>
