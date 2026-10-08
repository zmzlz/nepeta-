package com.taogueyuan.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "blog_posts")
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String coverImage;

    private String category; // 教程, 分享, 生活, 美食

    @ElementCollection
    private List<String> images;

    @ElementCollection
    private List<String> tags;

    private Integer views = 0;

    @Column(name = "likes_count")
    private Integer likes = 0;

    @Column(name = "favorites_count")
    private Integer favorites = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Transient
    private boolean isLiked;

    @Transient
    private boolean isFavorited;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("password")
    private User author;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}