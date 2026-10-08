package com.taogueyuan.repository;

import com.taogueyuan.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    List<BlogComment> findByPostIdOrderByCreatedAtDesc(Long postId);
    List<BlogComment> findAllByOrderByCreatedAtDesc();
    void deleteByPostId(Long postId);
}