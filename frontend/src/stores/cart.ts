import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { marketApi } from '@/api';

export const useCartStore = defineStore('cart', () => {
  const items = ref<any[]>([]);
  const isOpen = ref(false); // 控制购物车抽屉可见性
  
  // 商品总数量
  const count = computed(() => items.value.reduce((acc, item) => acc + item.quantity, 0));
  
  // 只检查是否存在商品
  const hasItems = computed(() => items.value.length > 0);

  async function fetchCart(userId: number) {
    try {
      const res: any = await marketApi.getCart(userId);
      // 确保 res 是数组
      items.value = Array.isArray(res) ? res : [];
    } catch (err) {
      console.error('Failed to fetch cart:', err);
      items.value = [];
    }
  }

  async function addToCart(userId: number, productId: number, quantity: number) {
    await marketApi.addToCart(userId, productId, quantity);
    await fetchCart(userId); // 刷新购物车获取更新后的商品
  }

  async function removeFromCart(id: number, userId: number) {
    await marketApi.removeFromCart(id);
    await fetchCart(userId);
  }

  async function clearCart(userId: number) {
    await marketApi.clearCart(userId);
    items.value = [];
  }

  function openCart() {
    isOpen.value = true;
  }

  function closeCart() {
    isOpen.value = false;
  }

  return { items, count, hasItems, isOpen, fetchCart, addToCart, removeFromCart, clearCart, openCart, closeCart };
});
