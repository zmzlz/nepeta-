package com.taogueyuan.service.impl;

import com.taogueyuan.dto.LoginRequest;
import com.taogueyuan.dto.RegisterRequest;
import com.taogueyuan.entity.User;
import com.taogueyuan.repository.UserRepository;
import com.taogueyuan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                return user;
            }
        }
        throw new RuntimeException("Invalid username or password"); // 用户名或密码无效
    }

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists"); // 用户名已存在
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // TODO: 加密密码
        user.setNickname(request.getNickname());
        user.setRole("USER");
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            user.setAvatar(request.getAvatar());
        } else {
            user.setAvatar("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar%20default%20illustration&image_size=square");
        }
        return userRepository.save(user);
    }

    @Override
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return getUserById(id, null);
    }

    @Override
    public User getUserById(Long id, Long currentUserId) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Populate counts
        user.setFollowingCount(user.getFollowingIds().size());
        user.setFollowersCount(user.getFollowerIds().size());

        // Populate isFollowing
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                user.setFollowing(currentUser.getFollowingIds().contains(id));
            }
        }
        
        return user;
    }

    @Override
    public void followUser(Long followerId, Long targetUserId) {
        if (followerId.equals(targetUserId)) {
            throw new RuntimeException("Cannot follow yourself");
        }
        User follower = userRepository.findById(followerId).orElseThrow(() -> new RuntimeException("Follower not found"));
        User target = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("Target user not found"));

        if (follower.getFollowingIds().contains(targetUserId)) {
            // Unfollow
            follower.getFollowingIds().remove(targetUserId);
            target.getFollowerIds().remove(followerId);
        } else {
            // Follow
            follower.getFollowingIds().add(targetUserId);
            target.getFollowerIds().add(followerId);
        }
        userRepository.save(follower);
        userRepository.save(target);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (userDetails.getNickname() != null && !userDetails.getNickname().isEmpty()) {
            user.setNickname(userDetails.getNickname());
        }
        if (userDetails.getAvatar() != null && !userDetails.getAvatar().isEmpty()) {
            user.setAvatar(userDetails.getAvatar());
        }
        if (userDetails.getBio() != null) {
            user.setBio(userDetails.getBio());
        }
        if (userDetails.getAge() != null) {
            user.setAge(userDetails.getAge());
        }
        if (userDetails.getLocation() != null) {
            user.setLocation(userDetails.getLocation());
        }
        // Add password update logic if needed, but usually separate endpoint
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
