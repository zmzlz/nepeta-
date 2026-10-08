export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  stock: number;
  category: string;
}

export interface CartItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  price: number;
}

export interface User {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  role?: string;
  bio?: string;
  age?: number;
  location?: string;
  followersCount?: number;
  followingCount?: number;
  totalLikesReceived?: number;
  totalFavoritesReceived?: number;
}

export interface ChatContact {
  id: number;
  nickname: string;
  avatar: string;
  unreadCount: number;
  lastMessage: string;
  lastMessageTime: string;
}

export interface Order {
  id: number;
  totalAmount: number;
  status: string;
  createdAt: string; // 更改了从 createTime 以匹配后端
  shippingAddress: string;
  addressSnapshot?: string;
  contactName: string;
  contactPhone: string;
  items: OrderItem[];
  user?: User;
  refundStatus?: string;
  refundReason?: string;
  refundType?: string;
  returnStatus?: string;
  returnLogisticsCompany?: string;
  returnTrackingNumber?: string;
}

export interface BlogPost {
  id: number;
  title: string;
  content: string;
  coverImage: string;
  images?: string[];
  author: User;
  views: number;
  likes: number;
  favorites?: number;
  commentsCount?: number;
  category: string;
  createdAt: string;
  tags?: string[];
  isLiked?: boolean;
  isFavorited?: boolean;
}

export interface BlogComment {
  id: number;
  content: string;
  imageUrl?: string;
  author: User;
  post?: BlogPost;
  likes?: number;
  parent?: BlogComment;
  replies?: BlogComment[];
  isLiked?: boolean;
  createdAt: string;
}

export interface ChatMessage {
  id: number;
  sender: User;
  receiver: User;
  order?: Order;
  content: string;
  imageUrl?: string;
  read: boolean;
  createdAt: string;
}
