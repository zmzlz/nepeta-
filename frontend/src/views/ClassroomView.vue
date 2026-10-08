<script setup lang="ts">
import { Star, Heart, Users, ArrowRight, Mail, Phone, MapPin, BookOpen, MessageCircle, Send, Instagram } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToTutorial = (id: number) => {
  router.push(`/classroom/${id}`)
}
</script>

<template>
  <div class="classroom-page">
    <section class="classroom-hero">
      <video
        src="../assets/images/h1.mp4"
        class="hero-video"
        autoplay
        muted
        loop
        playsinline
        controlsList="nodownload nofullscreen noremoteplayback"
        disablePictureInPicture
      >
        您的浏览器不支持视频播放
      </video>
      <div class="hero-mask"></div>
      <div class="hero-content">
        <h1>传统糕点</h1>
        <p>探索千年传承的米食文化，跟随古法手作技艺，感受家的温暖味道</p>
        <button>立即了解</button>
      </div>
    </section>

    <section class="featured-section">
      <div class="section-head">
        <h2>精选糕点</h2>
        <p>每一款都是味蕾的极致记忆</p>
      </div>

      <div class="featured-grid">
        <div class="featured-card group cursor-pointer" @click="goToTutorial(1)">
          <div class="featured-image-wrap">
            <img src="../assets/images/25.png" class="featured-image" />
          </div>
          <div class="featured-text">
            <h3>红桃粿</h3>
            <p>超详细的红桃粿制作教程</p>
            <div class="featured-action">
              <span>查看教程</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>

        <div class="featured-card group cursor-pointer" @click="goToTutorial(2)">
          <div class="featured-image-wrap">
            <img src="../assets/images/26.png" class="featured-image" />
          </div>
          <div class="featured-text">
            <h3>鼠曲粿</h3>
            <p>清香四溢的传统味道</p>
            <div class="featured-action">
              <span>查看教程</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>

        <div class="featured-card group cursor-pointer" @click="goToTutorial(3)">
          <div class="featured-image-wrap">
            <img src="../assets/images/27.png" class="featured-image" />
          </div>
          <div class="featured-text">
            <h3>朴籽粿</h3>
            <p>潮汕春日限定美味</p>
            <div class="featured-action">
              <span>查看教程</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>

        <div class="featured-card group cursor-pointer" @click="goToTutorial(4)">
          <div class="featured-image-wrap">
            <img src="../assets/images/28.png" class="featured-image" />
          </div>
          <div class="featured-text">
            <h3>甜粿</h3>
            <p>正月十五贺顺佳品</p>
            <div class="featured-action">
              <span>查看教程</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>

        <div class="featured-card group cursor-pointer" @click="goToTutorial(5)">
          <div class="featured-image-wrap">
            <img src="../assets/images/29.png" class="featured-image" />
          </div>
          <div class="featured-text">
            <h3>咸水粿</h3>
            <p>经典潮汕街头小吃</p>
            <div class="featured-action">
              <span>查看教程</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>

        <div class="featured-card group cursor-pointer" @click="goToTutorial(6)">
          <div class="featured-image-wrap">
            <img src="../assets/images/30.png" class="featured-image" />
          </div>
          <div class="featured-text">
            <h3>菜头粿</h3>
            <p>新年好彩头，潮汕人的最爱</p>
            <div class="featured-action">
              <span>查看教程</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="culture-section">
      <div class="culture-image">
        <img src="../assets/images/81.jpg" class="w-full h-full object-cover" />
      </div>
      <div class="culture-content">
        <h2>传承千年的米食文化</h2>
        <p>
          糍，是闽南地区对米制品的统称，承载着深厚的文化底蕴和家族记忆。每一款点心都有其独特的寓意和制作技艺，代代相传。
        </p>
        <p>
          在糯果小课堂，我们致力于保护和传承这些珍贵的传统手艺，让更多人了解并走进这充满温情的传统美食。
        </p>
        <div class="culture-stats">
          <div class="culture-stat">
            <div class="culture-icon"><BookOpen class="w-6 h-6" /></div>
            <div class="culture-value">50+</div>
            <div class="culture-label">传授配方</div>
          </div>
          <div class="culture-stat">
            <div class="culture-icon"><Users class="w-6 h-6" /></div>
            <div class="culture-value">10k+</div>
            <div class="culture-label">学员</div>
          </div>
          <div class="culture-stat">
            <div class="culture-icon"><Heart class="w-6 h-6" /></div>
            <div class="culture-value">100%</div>
            <div class="culture-label">用户好评</div>
          </div>
        </div>
      </div>
    </section>

    <footer class="classroom-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">
            <div class="w-8 h-8 rounded-full overflow-hidden bg-white flex items-center justify-center">
              <img src="/ico (1).png" alt="Logo" class="w-full h-full object-contain scale-110" />
            </div>
            <span>糯果小课堂</span>
          </div>
          <p>传承传统米食文化，分享家的温暖味道</p>
        </div>

        <div class="footer-links">
          <h4>快速链接</h4>
          <a href="#">首页</a>
          <a href="#">课程介绍</a>
          <a href="#">热销小糍</a>
          <a href="#">学员评价</a>
          <a href="#">关于我们</a>
        </div>

        <div class="footer-contact">
          <h4>联系方式</h4>
          <a href="#"><Mail class="w-4 h-4" /> contact@example.com</a>
          <a href="#"><Phone class="w-4 h-4" /> +86 138 **** 0000</a>
          <a href="#"><MapPin class="w-4 h-4" /> 糯趣馆门店</a>
        </div>

        <div class="footer-social">
          <h4>关注我们</h4>
          <div class="social-list">
            <a href="#"><MessageCircle class="w-4 h-4" /></a>
            <a href="#"><Send class="w-4 h-4" /></a>
            <a href="#"><Instagram class="w-4 h-4" /></a>
          </div>
        </div>
      </div>
      <div class="footer-copyright">© 2024 糯果小课堂。保留所有权利。</div>
    </footer>
  </div>
</template>

<style scoped>
.classroom-page {
  background: #fcf8f1;
  min-height: 100vh;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.classroom-hero {
  position: relative;
  height: 420px;
  overflow: hidden;
  background: #7f7c78;
}

.hero-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
  transform: scale(1.03);
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(34, 30, 28, 0.6) 0%, rgba(34, 30, 28, 0.45) 35%, rgba(34, 30, 28, 0.32) 60%, rgba(34, 30, 28, 0.24) 100%);
}

.hero-content {
  position: absolute;
  left: 7%;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  max-width: 540px;
  z-index: 2;
  animation: heroFadeIn 0.9s ease-out;
}

.hero-content h1 {
  font-size: clamp(50px, 5.6vw, 80px);
  line-height: 1.08;
  font-weight: 900;
  font-family: "STSong", "Songti SC", "Noto Serif SC", serif;
  letter-spacing: 0.01em;
}

.hero-content p {
  margin-top: 12px;
  font-size: 19px;
  line-height: 1.52;
  opacity: 0.95;
}

.hero-content button {
  margin-top: 24px;
  padding: 11px 28px;
  border-radius: 999px;
  background: #cd6b76;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  box-shadow: 0 10px 22px rgba(205, 107, 118, 0.36);
  transition: transform 0.26s ease, box-shadow 0.26s ease, background-color 0.26s ease;
}

.hero-content button:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 26px rgba(205, 107, 118, 0.42);
}

.hero-content button:active {
  transform: translateY(1px) scale(0.98);
}

.featured-section {
  width: min(1180px, calc(100% - 56px));
  margin: 60px auto 72px;
}

.section-head {
  text-align: center;
  margin-bottom: 36px;
}

.section-head h2 {
  font-size: clamp(40px, 3.9vw, 56px);
  font-weight: 800;
  color: #835e3d;
  line-height: 1.12;
  font-family: "STSong", "Songti SC", "Noto Serif SC", serif;
  letter-spacing: 0.01em;
}

.section-head p {
  margin-top: 14px;
  font-size: 16px;
  color: #c29f78;
  letter-spacing: 0.03em;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.featured-card {
  background: #fffaf3;
  border-radius: 14px;
  border: 1px solid #f1e6da;
  box-shadow: 0 6px 16px rgba(68, 43, 27, 0.08);
  padding: 16px 16px 18px;
  min-height: 372px;
  transition: transform 0.28s ease, box-shadow 0.28s ease;
  will-change: transform;
  animation: cardFadeUp 0.55s ease both;
}

.featured-card:nth-child(2) {
  animation-delay: 0.05s;
}

.featured-card:nth-child(3) {
  animation-delay: 0.1s;
}

.featured-card:nth-child(4) {
  animation-delay: 0.15s;
}

.featured-card:nth-child(5) {
  animation-delay: 0.2s;
}

.featured-card:nth-child(6) {
  animation-delay: 0.25s;
}

.featured-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 28px rgba(68, 43, 27, 0.14);
}

.featured-image-wrap {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  aspect-ratio: 15 / 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.featured-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.42s ease, filter 0.42s ease;
}

.group:hover .featured-image {
  transform: scale(1.03);
  filter: saturate(1.02);
}

.featured-text {
  padding: 14px 12px 0;
}

.featured-text h3 {
  font-size: 30px;
  line-height: 1.1;
  color: #503224;
  font-weight: 700;
  margin-bottom: 8px;
  font-family: "STSong", "Songti SC", "Noto Serif SC", serif;
}

.featured-text p {
  color: #9d8576;
  font-size: 13px;
  line-height: 1.65;
}

.featured-action {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid #f3e8dc;
  color: #d0636e;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  font-weight: 700;
  transition: color 0.24s ease;
}

.featured-card:hover .featured-action {
  color: #c9515f;
}

.featured-action :deep(svg) {
  transition: transform 0.25s ease;
}

.featured-card:hover .featured-action :deep(svg) {
  transform: translateX(2px);
}

.culture-section {
  width: min(1140px, calc(100% - 72px));
  margin: 16px auto 72px;
  display: grid;
  grid-template-columns: 0.85fr 1.15fr;
  gap: 64px;
  align-items: center;
}

.culture-image {
  border-radius: 20px;
  overflow: hidden;
  min-height: 340px;
  box-shadow: 0 16px 36px rgba(61, 34, 20, 0.12);
  border: 1px solid #fdfbf8;
  transition: transform 0.35s ease;
}

.culture-image:hover {
  transform: translateY(-3px);
}

.culture-content {
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.culture-content h2 {
  font-size: clamp(34px, 3.2vw, 44px);
  color: #6d4e34;
  font-weight: 800;
  line-height: 1.15;
  font-family: "STSong", "Songti SC", "Noto Serif SC", serif;
  margin-bottom: 20px;
  text-align: left;
}

.culture-content p {
  margin-top: 12px;
  color: #7b6352;
  line-height: 1.75;
  font-size: 14px;
  margin-bottom: 0;
  letter-spacing: 0.02em;
  text-align: left;
}

.culture-stats {
  margin-top: 48px;
  display: flex;
  gap: 80px;
  justify-content: center;
}

.culture-stat {
  text-align: center;
}

.culture-icon {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  background: #c36d77;
  color: #fff;
  display: grid;
  place-items: center;
  margin: 0 auto 12px;
  transition: transform 0.26s ease;
}

.culture-stat:hover .culture-icon {
  transform: translateY(-2px) scale(1.03);
}

.culture-value {
  font-size: 30px;
  font-weight: 900;
  color: #6a472c;
  line-height: 1.1;
}

.culture-label {
  margin-top: 8px;
  color: #9c806a;
  font-size: 13px;
  line-height: 1;
}

.classroom-footer {
  background: #73614a;
  color: #fff;
  padding: 48px 0 20px;
}

.footer-inner {
  width: min(1140px, calc(100% - 72px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 40px;
  align-items: start;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 900;
  margin-bottom: 14px;
  font-size: 20px;
  color: #fff;
  font-family: "STSong", "Songti SC", "Noto Serif SC", serif;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: #c36d77;
  display: grid;
  place-items: center;
}

.footer-brand p {
  font-size: 14px;
  color: #e3d3c2;
  line-height: 1.6;
  max-width: 300px;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links h4,
.footer-contact h4,
.footer-social h4 {
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 16px;
  color: #fff5e6;
  font-family: "STSong", "Songti SC", "Noto Serif SC", serif;
}

.footer-links a,
.footer-contact a {
  display: block;
  font-size: 14px;
  color: #e3d3c2;
  line-height: 1.5;
  transition: color 0.2s ease;
}

.footer-links a:hover,
.footer-contact a:hover {
  color: #fff;
}

.footer-contact {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.footer-contact a {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.footer-social {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.social-list {
  display: flex;
  gap: 14px;
}

.social-list a {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  display: grid;
  place-items: center;
  color: #fff;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.social-list a:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.25);
}

.footer-copyright {
  width: min(1140px, calc(100% - 72px));
  margin: 32px auto 0;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  text-align: center;
  color: #c9b7a4;
  font-size: 13px;
}

@keyframes heroFadeIn {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(-12px);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

@keyframes cardFadeUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1024px) {
  .featured-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }

  .culture-section {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .footer-inner {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
  }

  .hero-content h1 {
    font-size: clamp(44px, 7.5vw, 66px);
  }

  .culture-content p {
    font-size: 15px;
  }

  .culture-icon {
    width: 50px;
    height: 50px;
  }

  .culture-value {
    font-size: 30px;
  }

  .culture-label {
    font-size: 14px;
  }

  .culture-stats {
    gap: 28px;
  }
}

@media (max-width: 768px) {
  .classroom-hero {
    height: 360px;
  }

  .hero-content {
    left: 24px;
    right: 24px;
    max-width: none;
  }

  .featured-section,
  .culture-section,
  .footer-inner,
  .footer-copyright {
    width: calc(100% - 40px);
  }

  .featured-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .featured-card {
    min-height: auto;
    padding: 14px;
  }

  .featured-text {
    padding: 12px 8px 0;
  }

  .featured-text h3 {
    font-size: 26px;
  }

  .culture-content h2 {
    font-size: clamp(30px, 8vw, 38px);
  }

  .culture-content p {
    font-size: 16px;
    line-height: 1.7;
  }

  .culture-stats {
    gap: 22px;
    justify-content: center;
  }

  .culture-icon {
    width: 46px;
    height: 46px;
    margin-bottom: 8px;
  }

  .culture-value {
    font-size: 28px;
  }

  .culture-label {
    font-size: 13px;
    margin-top: 4px;
  }

  .footer-inner {
    grid-template-columns: 1fr;
  }

  .footer-logo {
    font-size: 18px;
  }

  .footer-brand p,
  .footer-links a,
  .footer-contact a {
    font-size: 14px;
  }

  .footer-links h4,
  .footer-contact h4,
  .footer-social h4 {
    font-size: 17px;
  }
}
</style>
