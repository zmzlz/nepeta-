import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 目前直接使用后端 URL，以避免任何代理问题
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const adminApi = {
  getStats: () => api.get('/admin/stats'),
};

export const blogApi = {
  getPosts: (category?: string, userId?: number) => api.get('/blog/posts', { params: { category, userId } }),
  getPostById: (id: number, userId?: number) => api.get(`/blog/posts/${id}`, { params: { userId } }),
  createPost: (data: any) => api.post('/blog/posts', data),
  updatePost: (id: number, data: any) => api.put(`/blog/posts/${id}`, data),
  getComments: (postId: number) => api.get(`/blog/posts/${postId}/comments`),
  getAllComments: () => api.get('/blog/comments'),
  addComment: (postId: number, data: any) => api.post(`/blog/posts/${postId}/comments`, data),
  deleteComment: (id: number) => api.delete(`/blog/comments/${id}`),
  likePost: (postId: number, userId: number) => api.post(`/blog/posts/${postId}/like`, null, { params: { userId } }),
  favoritePost: (postId: number, userId: number) => api.post(`/blog/posts/${postId}/favorite`, null, { params: { userId } }),
  deletePost: (id: number) => api.delete(`/blog/posts/${id}`),
  getLikedPosts: (userId: number) => api.get(`/blog/users/${userId}/liked`),
  getFavoritedPosts: (userId: number) => api.get(`/blog/users/${userId}/favorited`),
};

export const marketApi = {
  getProducts: (category?: string) => api.get('/market/products', { params: { category } }),
  getProductById: (id: number) => api.get(`/market/products/${id}`),
  addProduct: (data: any) => api.post('/market/products', data),
  updateProduct: (id: number, data: any) => api.put(`/market/products/${id}`, data),
  deleteProduct: (id: number) => api.delete(`/market/products/${id}`),
  getCart: (userId: number) => api.get('/market/cart', { params: { userId } }),
  addToCart: (userId: number, productId: number, quantity: number) => api.post('/market/cart/add', { userId, productId, quantity }),
  removeFromCart: (id: number) => api.delete(`/market/cart/${id}`),
  clearCart: (userId: number) => api.delete('/market/cart/clear', { params: { userId } }),
  createOrder: (data: any) => api.post('/market/orders', data),
  getUserOrders: (userId: number) => api.get('/market/orders', { params: { userId } }),
  getAllOrders: () => api.get('/market/orders'),
  updateOrderStatus: (id: number, status: string) => api.put(`/market/orders/${id}/status`, { status }),
  deleteOrder: (id: number) => api.delete(`/market/orders/${id}`),
  applyRefund: (id: number, reason: string, type: string = 'ONLY_REFUND') => api.post(`/market/orders/${id}/refund`, { reason, type }),
  handleRefund: (id: number, approved: boolean) => api.put(`/market/orders/${id}/refund/handle`, { approved }),
  submitReturnLogistics: (id: number, company: string, trackingNumber: string) => api.post(`/market/orders/${id}/return-logistics`, { company, trackingNumber }),
  confirmReturnReceipt: (id: number) => api.post(`/market/orders/${id}/confirm-return`),
};

export const userApi = {
  getAllUsers: () => api.get('/users'),
  getUserById: (id: number, currentUserId?: number) => api.get(`/users/${id}`, { params: { currentUserId } }),
  updateUser: (id: number, data: any) => api.put(`/users/${id}`, data),
  deleteUser: (id: number) => api.delete(`/users/${id}`),
  followUser: (targetUserId: number, followerId: number) => api.post(`/users/${targetUserId}/follow`, null, { params: { followerId } }),
};

export const addressApi = {
  getUserAddresses: (userId: number) => api.get('/addresses', { params: { userId } }),
  addAddress: (userId: number, data: any) => api.post('/addresses', data, { params: { userId } }),
  updateAddress: (id: number, data: any) => api.put(`/addresses/${id}`, data),
  deleteAddress: (id: number) => api.delete(`/addresses/${id}`),
};

export const favoriteApi = {
  getFavorites: (userId: number) => api.get('/favorites', { params: { userId } }),
  toggleFavorite: (userId: number, productId: number) => api.post(`/favorites/${productId}`, null, { params: { userId } }),
  checkFavorite: (userId: number, productId: number) => api.get(`/favorites/${productId}/check`, { params: { userId } }),
};

export const chatApi = {
  sendMessage: (data: { senderId: number; receiverId: number; orderId?: number; content: string; imageUrl?: string }) => api.post('/chat/send', data),
  getOrderMessages: (orderId: number) => api.get(`/chat/order/${orderId}`),
  getChatHistory: (userId1: number, userId2: number) => api.get('/chat/history', { params: { userId1, userId2 } }),
  getUserMessages: (userId: number) => api.get(`/chat/user/${userId}`),
  getAllMessages: () => api.get('/chat/all'),
  markAsRead: (messageId: number) => api.post(`/chat/read/${messageId}`),
  getRecentContacts: (userId: number) => api.get(`/chat/contacts/${userId}`),
};

export default api;
