package com.taogueyuan.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    private String nickname;

    private String avatar;

    private String role; // USER, ADMIN

    private String bio;

    private Integer age;

    private String location;

    @ElementCollection
    private java.util.Set<Long> likedPostIds = new java.util.HashSet<>();

    @ElementCollection
    private java.util.Set<Long> favoritePostIds = new java.util.HashSet<>();

    @ElementCollection
    private java.util.Set<Long> followingIds = new java.util.HashSet<>();

    @ElementCollection
    private java.util.Set<Long> followerIds = new java.util.HashSet<>();

    @Transient
    private boolean isFollowing;

    @Transient
    private Integer followersCount = 0;

    @Transient
    private Integer followingCount = 0;

    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}