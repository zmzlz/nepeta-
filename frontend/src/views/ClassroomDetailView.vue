<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, ChefHat, Users, CheckCircle2, PlayCircle, Info } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const tutorialId = route.params.id as string

// 教程的模拟数据
const tutorialsData: Record<string, any> = {
  '1': {
    id: '1',
    title: '红桃粿',
    subtitle: '超详细的红桃粿制作教程！馅料可以自由DIY噢😋',
    image: '/src/assets/images/25.png', // 回退到绝对或标准相对路径
    meta: {
      time: '60分钟',
      difficulty: '中级',
      yield: '约12个'
    },
    description: '红桃粿是潮汕地区传统的祭祀节庆食品。外皮软糯，内馅丰富。外形像桃子，寓意着长寿、吉祥、富贵。话不多说直接上干货！',
    ingredients: [
      { name: '糯米', amount: '500g' },
      { name: '香菇、虾皮、花生', amount: '适量' },
      { name: '菜脯（萝卜干）', amount: '适量' },
      { name: '粘米粉', amount: '800g' },
      { name: '生粉', amount: '400g' },
      { name: '红曲米粉/红色素', amount: '少许' }
    ],
    steps: [
      {
        title: '准备材料',
        desc: '糯米500g提前泡一夜，蒸熟备用。香菇、虾皮、花生洗净切碎，炒香。菜脯（萝卜干）切碎，洗净去咸味。'
      },
      {
        title: '制作馅料',
        desc: '将虾皮、花生碎、香菇、菜脯炒香，与蒸熟的糯米饭拌匀，调味。'
      },
      {
        title: '制作粿皮',
        desc: '将粘米粉800g和生粉400g混合，用开水烫熟，揉成面团。加入少量红曲米粉或食用红色素，揉成粉红色面团。'
      },
      {
        title: '包制红桃粿',
        desc: '取一小块面团，擀成圆皮，包入馅料，在模具里面按成红桃形状。'
      },
      {
        title: '蒸制与完成',
        desc: '将包好的红桃粿放入蒸笼，大火蒸15分钟。蒸好后，可以趁热刷一层薄油，防止干裂。'
      }
    ],
    tips: [
      '粿皮的制作是关键，要充分揉透，这样粿皮才会软糯有弹性。',
      '馅料可以根据个人口味调整，也可以选择其他喜欢的配料。',
      '蒸制时，确保水开后再放粿，蒸笼内要有足够的空间，以免粘连。'
    ]
  },
  '2': {
    id: '2',
    title: '鼠曲粿',
    subtitle: '清香四溢的传统味道（咸/甜双拼）',
    image: '/src/assets/images/26.png',
    meta: {
      time: '90分钟',
      difficulty: '中级',
      yield: '约15个'
    },
    description: '这里分享的是我常用的两种口味馅料：咸香糯米饭和甜口绿豆沙。白花鼠曲草的清香搭配丰富的内馅，是难得的美味。',
    ingredients: [
      { name: '糯米/大米', amount: '各533g' },
      { name: '虾米', amount: '150g' },
      { name: '香菇/红皮花生', amount: '30g/100g' },
      { name: '生脱皮绿豆', amount: '630g' },
      { name: '白糖', amount: '261g' },
      { name: '白花鼠曲草', amount: '1000g' }
    ],
    steps: [
      { title: '咸香馅准备', desc: '混合糯米、大米洗净电饭锅煮熟（水少点，45分钟）。虾米切碎，香菇提前浸泡切碎，两者下油锅（猪油更香）爆香，加1勺盐巴、4勺酱油、2勺炒焗佐料精调味。' },
      { title: '拌匀咸香馅', desc: '将炒好的配料混合蒸熟的米饭拌匀放凉。包粿前加入芹菜粒和两勺潮汕集锦红葱油，搅拌均匀。' },
      { title: '甜口绿豆沙', desc: '生脱皮绿豆泡水后铺炊笼布大火蒸40分钟。用碾压器压成豆沙泥加261克白糖，下适量玉米油在不粘锅翻炒成绿豆沙。' },
      { title: '处理鼠曲草', desc: '白花鼠曲草洗净，水开加少许小苏打焯水1-2分钟。捞出过凉水洗净，挤干水分（可100g捏成团冷冻保存），打成汁后翻炒成鼠曲草酱。' },
      { title: '包制与蒸制', desc: '将鼠曲草酱与粿皮面团混合揉匀，分别包入咸香糯米馅和甜口绿豆沙，印模成型后上锅蒸熟即可。' }
    ],
    tips: [
      '用1勺猪油润锅炒配料会更香。',
      '如果试吃后觉得米饭不够咸，可以适当加点酱油进去。',
      '鼠曲草焯水加小苏打可以保持翠绿的颜色。'
    ]
  },
  '3': {
    id: '3',
    title: '朴籽粿',
    subtitle: '潮汕春日限定美味',
    image: '/src/assets/images/27.png',
    meta: {
      time: '45分钟',
      difficulty: '初级',
      yield: '约10个'
    },
    description: '一口解锁潮汕春天的味道，软糯清香，咸甜都好吃～',
    ingredients: [
      { name: '朴籽叶', amount: '200g' },
      { name: '糯米粉', amount: '300g' },
      { name: '粘米粉', amount: '100g' },
      { name: '花生碎', amount: '50g' },
      { name: '虾米', amount: '20g' },
      { name: '盐 / 糖', amount: '适量' },
      { name: '清水', amount: '500ml' }
    ],
    steps: [
      { title: '处理朴籽叶', desc: '朴籽叶焯水 3 分钟捞出，加 500ml 清水用搅拌机打成细腻泥状。' },
      { title: '揉制面团', desc: '将朴籽泥与糯米粉、粘米粉混合，加适量盐 / 糖，揉至光滑不粘手的面团。' },
      { title: '包馅成型', desc: '面团分成 50g 小剂，包入花生虾米馅，捏紧收口，整理成圆饼状。' },
      { title: '蒸制成品', desc: '水开后放入蒸笼，中火蒸 20 分钟，至表面发亮即可出锅。' },
      { title: '完成享用', desc: '趁热食用口感最佳，软糯清香，咸甜风味都超绝～' }
    ],
    tips: [
      '朴籽叶焯水可以去除涩味，让成品更清香。',
      '糯米粉和粘米粉的比例是口感关键，3:1 的比例软糯不粘牙。',
      '馅料可以根据喜好调整，甜口可加花生芝麻糖，咸口加虾米香菇。',
      '蒸制时垫上朴籽叶，防粘还能增香。'
    ]
  },
  '4': {
    id: '4',
    title: '甜粿',
    subtitle: '正月十五贺顺佳品',
    image: '/src/assets/images/28.png',
    meta: {
      time: '120分钟',
      difficulty: '中级',
      yield: '约2斤'
    },
    description: '潮汕甜粿做法，今天蒸了甜粿来爷贺贺顺。配比经典，甜而不腻，软糯弹牙。',
    ingredients: [
      { name: '水磨糯米粉', amount: '500g' },
      { name: '白糖', amount: '300g' },
      { name: '红糖', amount: '50g' },
      { name: '清水', amount: '400-450g' },
      { name: '玉米油', amount: '30g' },
      { name: '黑白芝麻', amount: '少许' }
    ],
    steps: [
      { title: '熬煮糖水', desc: '先取锅倒入500g清水，加入300g白糖+50g红糖（不喜甜可减），开火融化白糖和红糖，放凉备用。' },
      { title: '初步揉粉', desc: '盆里倒入500g糯米粉，把晾凉的糖水一点点倒入粉里，边倒边揉，揉到看不见干粉后继续揉2-3分钟。' },
      { title: '调整状态', desc: '继续加水揉，揉到手拿起粉浆从高处低落，可以成直线非常丝滑的垂落，类似浓稠酸奶状（非牛顿体感觉）。盖保鲜膜静止1小时。' },
      { title: '加油入模', desc: '静止1小时后加入30g玉米油搅拌均匀，倒入刷了油的耐高温模具。' },
      { title: '蒸制与装饰', desc: '上锅高压锅中火蒸一个半小时。出锅趁热撒上黑白芝麻，彻底晾凉才能脱模。' }
    ],
    tips: [
      '糖水不要一次性全倒进去，要分次慢慢加。',
      '最后粉浆的状态很关键，要像浓稠酸奶一样顺滑。',
      '甜粿一定要彻底晾凉才能脱模，否则容易粘连。'
    ]
  },
  '5': {
    id: '5',
    title: '咸水粿',
    subtitle: '经典潮汕街头小吃',
    image: '/src/assets/images/29.png',
    meta: {
      time: '45分钟',
      difficulty: '初级',
      yield: '约20个'
    },
    description: '咸水粿（潮汕话称“水粿”）是潮汕地区经典的街头小吃，以米浆蒸制的小碗状粿皮搭配菜脯粒和甜酱而成，口感软糯咸香。',
    ingredients: [
      { name: '粘米', amount: '适量' },
      { name: '薯粉', amount: '少许' },
      { name: '菜脯粒（萝卜干）', amount: '适量' },
      { name: '蒜末、虾米', amount: '少许' },
      { name: '甜酱/辣椒酱', amount: '适量' },
      { name: '猪油/食用油', amount: '适量' }
    ],
    steps: [
      { title: '磨制米浆', desc: '大米浸泡后磨成细腻米浆，加入少量薯粉增加弹性，过滤去杂质。' },
      { title: '蒸制粿皮', desc: '特制小陶碗或铝制模具刷油，倒入米浆（约八分满）。蒸5-8分钟，米浆凝固成凹形小碗状，边缘略透明。' },
      { title: '炒制菜脯', desc: '萝卜干切粒，用猪油或蒜末爆香，可加少许白糖平衡咸味，加入虾米提味。' },
      { title: '组合成型', desc: '蒸好的粿皮趁热放上炒好的菜脯粒，淋一勺甜酱或辣椒酱。' }
    ],
    tips: [
      '“咸水”指蒸制时需在模具中加少量水，使米浆成型后边缘略凹陷。',
      '现做现吃，用竹签或勺子挖取，连粿皮带馅料一口吞下口感最佳。',
      '搭配潮汕工夫茶解腻是经典吃法。'
    ]
  },
  '6': {
    id: '6',
    title: '菜头粿',
    subtitle: '新年好彩头，潮汕人的最爱',
    image: '/src/assets/images/30.png',
    meta: {
      time: '60分钟',
      difficulty: '初级',
      yield: '约1大盘'
    },
    description: '潮汕菜头粿，潮汕话谐音“彩头粿”，新年吃菜头粿，吃了有好彩头。如果你是白萝卜爱好者，一定会爱上它清甜的味道。',
    ingredients: [
      { name: '白萝卜', amount: '4斤' },
      { name: '番薯粉', amount: '400g' },
      { name: '花生米', amount: '适量' },
      { name: '芹菜（只取梗）', amount: '少量' },
      { name: '虾米', amount: '少量' },
      { name: '海盐', amount: '适量' }
    ],
    steps: [
      { title: '处理萝卜', desc: '白萝卜洗净后削皮，刨成丝，加适量海盐静置约8～10分钟，让白萝卜析出水份。' },
      { title: '准备配料', desc: '花生米用工具（擀面杖）碾碎；芹菜去掉叶子，只取芹菜梗切碎。' },
      { title: '混合食材', desc: '用手搓干白萝卜的水份，再用剪刀把萝卜丝剪碎剪短。把花生米碎、芹菜碎、虾米加到白萝卜丝里抓匀。' },
      { title: '加入番薯粉', desc: '加入番薯粉（作为粘合剂，一点点加避免过量）。用双手掌心把所有食材整成一个球体。' },
      { title: '蒸制与煎香', desc: '蒸锅水开后，隔水蒸35~40分钟。蒸熟出锅后可直接加辣椒酱，或切片小火煎到两面焦黄蘸辣椒酱吃。' }
    ],
    tips: [
      '食材里加的是番薯粉，不是普通的面粉，不可替换。',
      '番薯粉在加的过程中，建议一点点加进白萝卜丝里，避免过量。',
      '煎至两面金黄后蘸辣椒酱，是最好吃的人间美味。'
    ]
  }
}

// 基于 id 动态解析头像图片
const resolvedAvatar = computed(() => {
  const avatarMap: Record<string, string> = {
    '1': new URL('../assets/images/25.png', import.meta.url).href,
    '2': new URL('../assets/images/26.png', import.meta.url).href,
    '3': new URL('../assets/images/27.png', import.meta.url).href,
    '4': new URL('../assets/images/28.png', import.meta.url).href,
    '5': new URL('../assets/images/29.png', import.meta.url).href,
    '6': new URL('../assets/images/30.png', import.meta.url).href,
  }
  return avatarMap[tutorialId] || avatarMap['1']
})

// 如果 id 匹配 1-6，则基于 id 动态解析图片
const resolvedImage = computed(() => {
  const imgMap: Record<string, string> = {
    '1': new URL('/avatars/ht.png', import.meta.url).href,
    '2': new URL('/avatars/sq.png', import.meta.url).href,
    '3': new URL('/avatars/pz.png', import.meta.url).href,
    '4': new URL('/avatars/tg.png', import.meta.url).href,
    '5': new URL('/avatars/xs.png', import.meta.url).href,
    '6': new URL('/avatars/ct.png', import.meta.url).href,
  }
  return imgMap[tutorialId] || imgMap['3']
})

// 为其他卡片概念性映射 ID
const defaultTutorial = {
  id: tutorialId,
  title: '经典糕点教程',
  subtitle: '传承古法手艺',
  image: '/src/assets/images/27.png', // 通用图片
  meta: { time: '45分钟', difficulty: '中级', yield: '2-4人份' },
  description: '这是一道充满家乡味道的传统糕点。通过简单的食材和用心的制作，找回记忆中的香甜。',
  ingredients: [
    { name: '主要食材', amount: '500g' },
    { name: '辅料', amount: '适量' },
    { name: '调味料', amount: '少许' }
  ],
  steps: [
    { title: '准备工作', desc: '将所有食材清洗干净，按比例称量好备用。' },
    { title: '混合搅拌', desc: '将主要粉类与水按特定比例混合，揉捏至均匀无颗粒状态。' },
    { title: '成型烹饪', desc: '放入模具成型，大火蒸制或小火慢煎至熟透。' }
  ],
  tips: ['制作过程中注意火候的控制。']
}

const tutorial = computed(() => {
  return tutorialsData[tutorialId] || { ...defaultTutorial, title: `教程 #${tutorialId}` }
})

const goBack = () => {
  router.push('/classroom')
}
</script>

<template>
  <div class="bg-[#fcf8f1] min-h-screen pb-20 font-sans selection:bg-[#c36d77] selection:text-white">
    <!-- 详情页顶部导航栏 -->
    <div class="sticky top-0 z-40 bg-[#fcf8f1]/90 backdrop-blur-md border-b border-[#f3e8dc]">
      <div class="container mx-auto px-4 md:px-8 h-16 flex items-center justify-between">
        <button @click="goBack" class="flex items-center gap-2 text-[#835e3d] hover:text-[#c36d77] font-bold transition-colors group">
          <div class="w-8 h-8 rounded-full bg-white flex items-center justify-center shadow-sm group-hover:shadow-md transition-all">
            <ArrowLeft class="w-4 h-4" />
          </div>
          返回课堂
        </button>
        <div class="font-bold text-[#6d4e34] hidden md:block tracking-widest font-serif">糯果小课堂</div>
        <div class="w-24"></div> <!-- 用于居中的占位符 -->
      </div>
    </div>

    <!-- 英雄横幅 -->
    <div class="container mx-auto px-4 md:px-8 mt-8 mb-12">
      <div class="bg-white rounded-[2rem] shadow-[0_20px_40px_-15px_rgba(68,43,27,0.08)] overflow-hidden flex flex-col md:flex-row border border-[#f1e6da]">
        
        <!-- 左侧：文本内容 -->
        <div class="p-8 md:p-12 lg:p-16 flex-1 flex flex-col justify-center">
          <div class="inline-block px-3 py-1 bg-[#fff5e6] text-[#c36d77] rounded-full text-xs font-bold mb-6 border border-[#fde8cd] self-start">
            手把手教程
          </div>
          <h1 class="text-4xl md:text-5xl lg:text-6xl font-extrabold text-[#503224] mb-4 font-serif leading-tight">
            {{ tutorial.title }}
          </h1>
          <p class="text-xl text-[#9d8576] font-medium mb-8">
            {{ tutorial.subtitle }}
          </p>
          
          <div class="flex flex-wrap items-center gap-6 text-[#7b6352] mb-8 bg-[#fffaf3] p-5 rounded-2xl border border-[#f1e6da]">
            <div class="flex items-center gap-2">
              <Clock class="w-5 h-5 text-[#c36d77]" />
              <span class="font-bold">{{ tutorial.meta.time }}</span>
            </div>
            <div class="w-1 h-1 rounded-full bg-[#dcc9b6]"></div>
            <div class="flex items-center gap-2">
              <ChefHat class="w-5 h-5 text-[#c36d77]" />
              <span class="font-bold">{{ tutorial.meta.difficulty }}</span>
            </div>
            <div class="w-1 h-1 rounded-full bg-[#dcc9b6]"></div>
            <div class="flex items-center gap-2">
              <Users class="w-5 h-5 text-[#c36d77]" />
              <span class="font-bold">{{ tutorial.meta.yield }}</span>
            </div>
          </div>

          <p class="text-[#7b6352] leading-relaxed">
            {{ tutorial.description }}
          </p>
        </div>

        <!-- 右侧：图片 -->
        <div class="w-full md:w-[45%] bg-[#fffaf3] flex items-center justify-center p-12 border-l border-[#f1e6da] relative overflow-hidden">
          <!-- 装饰圆圈 -->
          <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[120%] aspect-square bg-[#fdf3e6] rounded-full blur-3xl opacity-60"></div>
          
          <!-- 小悬浮头像（现在是插画） -->
          <div class="absolute top-8 right-8 w-24 h-24 rounded-full border-4 border-white shadow-[0_8px_20px_rgba(0,0,0,0.1)] overflow-hidden z-20 animate-bounce-slow bg-[#fcf8f1] flex items-center justify-center">
            <img :src="resolvedAvatar" alt="Avatar" class="w-[85%] h-[85%] object-contain" />
          </div>

          <!-- 主英雄图片（现在是真实照片） -->
          <div class="relative z-10 w-full max-w-[320px] aspect-square rounded-[2rem] overflow-hidden shadow-2xl border-8 border-white group hover:-translate-y-2 transition-transform duration-500">
            <img :src="resolvedImage" alt="糕点图片" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="container mx-auto px-4 md:px-8">
      <div class="flex flex-col lg:flex-row gap-10">
        
        <!-- 左列：步骤 -->
        <div class="flex-[2] lg:pr-8">
          <h2 class="text-2xl font-bold text-[#503224] mb-8 font-serif flex items-center gap-3">
            <PlayCircle class="w-6 h-6 text-[#c36d77]" />
            制作步骤
          </h2>

          <div class="space-y-8 relative before:absolute before:inset-0 before:ml-6 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-[#f1e6da] before:via-[#f1e6da] before:to-transparent">
            
            <div v-for="(step, index) in tutorial.steps" :key="index" class="relative flex items-start justify-between md:justify-normal md:odd:flex-row-reverse group">
              <!-- 时间线圆点 -->
              <div class="flex items-center justify-center w-12 h-12 rounded-full bg-white border-4 border-[#fffaf3] shadow-md text-[#c36d77] font-black text-xl absolute left-0 md:left-1/2 -translate-x-1/2 z-10 group-hover:bg-[#c36d77] group-hover:text-white transition-colors duration-300">
                {{ index + 1 }}
              </div>
              
              <!-- 内容卡片 -->
              <div class="w-[calc(100%-4rem)] md:w-[calc(50%-3rem)] bg-white p-6 md:p-8 rounded-2xl shadow-sm border border-[#f1e6da] group-hover:shadow-md transition-shadow">
                <h3 class="text-lg font-bold text-[#6d4e34] mb-3">{{ step.title }}</h3>
                <p class="text-[#7b6352] text-[15px] leading-relaxed">
                  {{ step.desc }}
                </p>
              </div>
            </div>

          </div>
        </div>

        <!-- 右列：食材与提示 -->
        <div class="flex-1 space-y-8">
          
          <!-- 食材框 -->
          <div class="bg-white rounded-3xl p-8 shadow-[0_10px_30px_-15px_rgba(68,43,27,0.08)] border border-[#f1e6da] sticky top-24">
            <h2 class="text-xl font-bold text-[#503224] mb-6 font-serif border-b border-[#f3e8dc] pb-4 flex items-center gap-2">
              <span class="text-2xl">🥣</span> 所需食材
            </h2>
            
            <ul class="space-y-4">
              <li v-for="(item, index) in tutorial.ingredients" :key="index" class="flex items-center justify-between group">
                <div class="flex items-center gap-3 text-[#6d4e34] font-medium">
                  <CheckCircle2 class="w-4 h-4 text-[#dcc9b6] group-hover:text-[#c36d77] transition-colors" />
                  {{ item.name }}
                </div>
                <div class="text-[#9d8576] text-sm font-bold bg-[#fffaf3] px-3 py-1 rounded-lg border border-[#f1e6da]">
                  {{ item.amount }}
                </div>
              </li>
            </ul>
          </div>

          <!-- 提示框 -->
          <div class="bg-[#fff5e6] rounded-3xl p-8 shadow-sm border border-[#fde8cd]">
            <h2 class="text-lg font-bold text-[#835e3d] mb-4 font-serif flex items-center gap-2">
              <Info class="w-5 h-5 text-[#c36d77]" />
              小贴士 (Tips)
            </h2>
            <ul class="space-y-3">
              <li v-for="(tip, index) in tutorial.tips" :key="index" class="text-[#7b6352] text-sm leading-relaxed flex items-start gap-2">
                <span class="text-[#c36d77] font-bold mt-0.5">•</span>
                {{ tip }}
              </li>
            </ul>
          </div>

        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* Smooth scrolling and refined typography */
@keyframes bounce-slow {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
.animate-bounce-slow {
  animation: bounce-slow 3s ease-in-out infinite;
}
</style>
