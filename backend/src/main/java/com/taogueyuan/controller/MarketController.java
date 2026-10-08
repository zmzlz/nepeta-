package com.taogueyuan.controller;

import com.taogueyuan.entity.CartItem;
import com.taogueyuan.entity.Order;
import com.taogueyuan.entity.Product;
import com.taogueyuan.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketController {

    @Autowired
    private MarketService marketService;

    // --- 商品 ---

    @GetMapping("/products")
    public List<Product> getAllProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return marketService.getProductsByCategory(category);
        }
        return marketService.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return marketService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return marketService.searchProducts(keyword);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return marketService.saveProduct(product);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return marketService.saveProduct(product);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        marketService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // --- 购物车 ---

    @GetMapping("/cart")
    public List<CartItem> getCart(@RequestParam Long userId) {
        return marketService.getCartItems(userId);
    }

    @PostMapping("/cart/add")
    public CartItem addToCart(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long productId = Long.valueOf(payload.get("productId").toString());
        int quantity = Integer.parseInt(payload.get("quantity").toString());
        return marketService.addToCart(userId, productId, quantity);
    }

    @DeleteMapping("/cart/{id}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long id) {
        marketService.removeFromCart(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/cart/clear")
    public ResponseEntity<Void> clearCart(@RequestParam Long userId) {
        marketService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    // --- 订单 ---

    @GetMapping("/orders")
    public List<Order> getUserOrders(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return marketService.getUserOrders(userId);
        }
        return marketService.getAllOrders();
    }

    @PutMapping("/orders/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return marketService.updateOrderStatus(id, payload.get("status"));
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String address = (String) payload.get("address");
        if (address == null) {
            address = (String) payload.get("addressSnapshot");
        }
        String contactName = (String) payload.get("contactName");
        String contactPhone = (String) payload.get("contactPhone");
        
        Order order;
        
        // 检查是否为直接购买
        if (payload.containsKey("productId") && payload.containsKey("quantity")) {
            Long productId = Long.valueOf(payload.get("productId").toString());
            int quantity = Integer.parseInt(payload.get("quantity").toString());
            order = marketService.createOrderDirect(userId, productId, quantity);
        } else {
            // 否则为购物车结算
            List<Long> cartItemIds = null;
            if (payload.containsKey("cartItemIds")) {
                List<?> ids = (List<?>) payload.get("cartItemIds");
                cartItemIds = ids.stream()
                        .map(id -> Long.valueOf(id.toString()))
                        .collect(Collectors.toList());
            }
            order = marketService.createOrderFromCart(userId, cartItemIds);
        }
        
        // 如果提供了地址信息，则更新
        if (address != null) {
            order.setShippingAddress(address);
            order.setContactName(contactName);
            order.setContactPhone(contactPhone);
            marketService.saveOrder(order);
        }
        
        return order;
    }
    
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        marketService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{id}/refund")
    public Order applyRefund(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return marketService.applyRefund(id, payload.get("reason"), payload.get("type"));
    }

    @PostMapping("/orders/{id}/return-logistics")
    public Order submitReturnLogistics(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return marketService.submitReturnLogistics(id, payload.get("company"), payload.get("trackingNumber"));
    }

    @PostMapping("/orders/{id}/confirm-return")
    public Order confirmReturnReceipt(@PathVariable Long id) {
        return marketService.confirmReturnReceipt(id);
    }

    @PutMapping("/orders/{id}/refund/handle")
    public Order handleRefund(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        return marketService.handleRefund(id, payload.get("approved"));
    }
}
