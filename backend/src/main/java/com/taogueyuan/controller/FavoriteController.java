package com.taogueyuan.controller;

import com.taogueyuan.entity.Favorite;
import com.taogueyuan.entity.User;
import com.taogueyuan.repository.UserRepository;
import com.taogueyuan.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserRepository userRepository;

    // 获取用户ID的辅助方法，从请求参数或认证中获取（简化版，目前由用户传递userId）
    // 目前前端在许多调用中显式传递userId，为了保持一致性，我们沿用此模式
    // 如果是GET请求，则使用查询参数。

    @GetMapping
    public ResponseEntity<List<Favorite>> getMyFavorites(@RequestParam Long userId) {
        return ResponseEntity.ok(marketService.getUserFavorites(userId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long productId, @RequestParam Long userId) {
        marketService.toggleFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long productId, @RequestParam Long userId) {
        return ResponseEntity.ok(marketService.isFavorite(userId, productId));
    }
}
