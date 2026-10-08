package com.taogueyuan.service;

import com.taogueyuan.dto.LoginRequest;
import com.taogueyuan.dto.RegisterRequest;
import com.taogueyuan.entity.User;

public interface UserService {
    User login(LoginRequest request);
    User register(RegisterRequest request);
    java.util.List<User> getAllUsers();
    User getUserById(Long id);

    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void followUser(Long followerId, Long targetUserId);
    User getUserById(Long id, Long currentUserId);
}
