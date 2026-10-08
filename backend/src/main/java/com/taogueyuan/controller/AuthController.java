package com.taogueyuan.controller;

import com.taogueyuan.dto.AuthResponse;
import com.taogueyuan.dto.LoginRequest;
import com.taogueyuan.dto.RegisterRequest;
import com.taogueyuan.entity.User;
import com.taogueyuan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        return ResponseEntity.ok(convertToAuthResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok(convertToAuthResponse(user));
    }

    private AuthResponse convertToAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        response.setToken("dummy-token-" + user.getId()); // 占位令牌
        return response;
    }
}
