package com.taogueyuan.repository;

import com.taogueyuan.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByCategoryOrderByCreatedAtDesc(String category);
    List<BlogPost> findAllByOrderByCreatedAtDesc();
}