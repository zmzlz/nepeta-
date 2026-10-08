package com.taogueyuan.service;

import com.taogueyuan.entity.*;
import com.taogueyuan.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MarketService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private AddressRepository addressRepository;

    // --- 商品相关方法 ---

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // --- 收藏相关方法 ---

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void toggleFavorite(Long userId, Long productId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found")); // 用户未找到
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found")); // 商品未找到
            
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProduct(product);
            favoriteRepository.save(favorite);
        }
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    // --- 购物车相关方法 ---

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // 用户未找到
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found")); // 商品未找到

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    // --- 订单相关方法 ---

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found")); // 订单未找到
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
    
    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    public Order applyRefund(Long orderId, String reason, String type) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setRefundStatus("REFUND_APPLIED");
        order.setRefundReason(reason);
        order.setRefundType(type != null ? type : "ONLY_REFUND"); // Default to ONLY_REFUND
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order handleRefund(Long orderId, boolean approved) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (approved) {
            order.setRefundStatus("REFUND_APPROVED");
            if ("RETURN_AND_REFUND".equals(order.getRefundType())) {
                order.setReturnStatus("WAITING_SHIPMENT");
            } else {
                order.setStatus("CANCELLED"); // For ONLY_REFUND, mark as cancelled/refunded immediately
                order.setRefundStatus("REFUND_SUCCESS");
            }
        } else {
            order.setRefundStatus("REFUND_REJECTED");
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order submitReturnLogistics(Long orderId, String company, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!"RETURN_AND_REFUND".equals(order.getRefundType())) {
             throw new RuntimeException("Not a return order");
        }
        
        order.setReturnLogisticsCompany(company);
        order.setReturnTrackingNumber(trackingNumber);
        order.setReturnStatus("SHIPPED");
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmReturnReceipt(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!"RETURN_AND_REFUND".equals(order.getRefundType())) {
             throw new RuntimeException("Not a return order");
        }
        
        order.setReturnStatus("RECEIVED");
        order.setRefundStatus("REFUND_SUCCESS");
        order.setStatus("CANCELLED"); // Order is fully cancelled/refunded
        
        return orderRepository.save(order);
    }

    /**
     * 从选中的购物车商品创建订单。
     * 如果 cartItemIds 为空或 null，则使用购物车中的所有商品。
     */
    @Transactional
    public Order createOrderFromCart(Long userId, List<Long> cartItemIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // 用户未找到
        
        // 查找地址
        Address address = findUserAddress(userId);

        List<CartItem> allCartItems = cartItemRepository.findByUserId(userId);
        if (allCartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty"); // 购物车为空
        }

        List<CartItem> selectedItems;
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            selectedItems = allCartItems.stream()
                    .filter(item -> cartItemIds.contains(item.getId()))
                    .collect(Collectors.toList());
        } else {
            selectedItems = allCartItems;
        }

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("No items selected for checkout"); // 未选择结算商品
        }

        return createOrderInternal(user, address, selectedItems, true);
    }

    /**
     * 直接为单个商品创建订单（立即购买）。
     */
    @Transactional
    public Order createOrderDirect(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // 用户未找到
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found")); // 商品未找到

        // 查找地址
        Address address = findUserAddress(userId);

        // 创建一个临时的 CartItem 结构或直接传递给内部方法
        // 实际上 createOrderInternal 只需要产品、数量、价格。
        // 让我们创建一个临时的 CartItem 对象（不保存到数据库）仅用于传递数据
        CartItem tempItem = new CartItem();
        tempItem.setUser(user);
        tempItem.setProduct(product);
        tempItem.setQuantity(quantity);

        return createOrderInternal(user, address, List.of(tempItem), false);
    }

    private Address findUserAddress(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        if (addresses.isEmpty()) {
            throw new RuntimeException("No shipping address found. Please add an address in your profile."); // 未找到收货地址，请在个人中心添加
        }
        // 如果存在默认地址则返回，否则返回第一个
        return addresses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .orElse(addresses.get(0));
    }

    private Order createOrderInternal(User user, Address address, List<CartItem> items, boolean deleteFromCart) {
        Order order = new Order();
        order.setUser(user);
        
        // 使用地址详情
        String fullAddress = address.getProvince() + " " + address.getCity() + " " + 
                             address.getDistrict() + " " + address.getDetailAddress();
        order.setShippingAddress(fullAddress);
        order.setContactName(address.getRecipientName()); // 假设收件人姓名是联系人姓名
        order.setContactPhone(address.getPhone());
        order.setStatus("PENDING");
        order.setRefundStatus("NONE");

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getProduct().getPrice());
            
            orderItems.add(orderItem);
            
            total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);
        
        Order savedOrder = orderRepository.save(order);
        
        if (deleteFromCart) {
            cartItemRepository.deleteAll(items);
        }
        
        return savedOrder;
    }
}
