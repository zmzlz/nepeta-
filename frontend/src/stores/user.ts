import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

// 配置ure base URL for axios if not already done globally
const api = axios.create({
  baseURL: '/api' // 使用相对路径触发 Vite 代理
})

export const useUserStore = defineStore('user', () => {
  // 初始化时检查过期
  const checkExpiry = () => {
    const expiry = localStorage.getItem('login_expiry')
    if (expiry && parseInt(expiry) < Date.now()) {
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      localStorage.removeItem('login_expiry')
      return true // 已过期
    }
    return false
  }

  checkExpiry()

  // 从任一存储中获取的助手函数
  const getStoredItem = (key: string) => localStorage.getItem(key) || sessionStorage.getItem(key)

  const user = ref(JSON.parse(getStoredItem('user') || 'null'))
  const isLoggedIn = ref(!!user.value)
  const token = ref(getStoredItem('token') || '')
  const showAuthModal = ref(false)

  function openAuthModal() {
    showAuthModal.value = true
  }

  function closeAuthModal() {
    showAuthModal.value = false
  }

  async function login(credentials: any, remember: boolean = false) {
    try {
      const response = await api.post('/auth/login', credentials)
      const userData = response.data
      
      user.value = userData
      token.value = userData.token
      isLoggedIn.value = true
      
      // 清除以前的存储以避免冲突
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      localStorage.removeItem('login_expiry')
      sessionStorage.removeItem('user')
      sessionStorage.removeItem('token')

      if (remember) {
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('token', userData.token)
        // 设置过期时间为3天后
        const threeDays = 3 * 24 * 60 * 60 * 1000
        localStorage.setItem('login_expiry', (Date.now() + threeDays).toString())
      } else {
        sessionStorage.setItem('user', JSON.stringify(userData))
        sessionStorage.setItem('token', userData.token)
      }
      
      return userData
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  async function register(credentials: any) {
    try {
      const response = await api.post('/auth/register', credentials)
      const userData = response.data
      
      user.value = userData
      token.value = userData.token
      isLoggedIn.value = true
      
      // 注册默认使用 session storage
      sessionStorage.setItem('user', JSON.stringify(userData))
      sessionStorage.setItem('token', userData.token)
      
      return userData
    } catch (error) {
      console.error('Registration failed:', error)
      throw error
    }
  }

  function logout() {
    user.value = null
    token.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('login_expiry')
    sessionStorage.removeItem('user')
    sessionStorage.removeItem('token')
  }

  return { user, isLoggedIn, token, showAuthModal, openAuthModal, closeAuthModal, login, register, logout }
})
