<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import HeaderNav from './components/HeaderNav.vue'
import FooterNav from './components/FooterNav.vue'
import AuthModal from './components/AuthModal.vue'

const route = useRoute()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
</script>

<template>
  <div class="min-h-screen flex flex-col font-sans text-dark-900">
    <HeaderNav v-if="!isAdminRoute" />
    <main class="flex-grow">
      <RouterView />
    </main>
    <FooterNav v-if="!isAdminRoute" />
    <AuthModal />
  </div>
</template>

<style>
/* Global resets if needed */
body {
  margin: 0;
  padding: 0;
}
</style>
