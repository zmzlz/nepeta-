package com.taogueyuan.controller;

import com.taogueyuan.entity.BlogPost;
import com.taogueyuan.entity.BlogComment;
import com.taogueyuan.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin(origins = "*") // 允许来自前端的请求
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping("/posts")
    public List<BlogPost> getAllPosts(@RequestParam(required = false) String category, @RequestParam(required = false) Long userId) {
        if (category != null && !category.isEmpty() && !category.equals("全部")) {
            return blogService.getPostsByCategory(category, userId);
        }
        return blogService.getAllPosts(userId);
    }

    @GetMapping("/posts/{id}")
    public BlogPost getPostById(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return blogService.getPostById(id, userId);
    }

    @PostMapping("/posts")
    public BlogPost createPost(@RequestBody BlogPost post) {
        // 在实际应用中，从 SecurityContext 获取用户
        return blogService.createPost(post);
    }

    @PutMapping("/posts/{id}")
    public BlogPost updatePost(@PathVariable Long id, @RequestBody BlogPost post) {
        return blogService.updatePost(id, post);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        blogService.deletePost(id);
    }

    @PostMapping("/posts/{id}/like")
    public void likePost(@PathVariable Long id, @RequestParam Long userId) {
        blogService.likePost(id, userId);
    }

    @PostMapping("/posts/{id}/favorite")
    public void favoritePost(@PathVariable Long id, @RequestParam Long userId) {
        blogService.favoritePost(id, userId);
    }

    @GetMapping("/users/{userId}/liked")
    public List<BlogPost> getLikedPosts(@PathVariable Long userId) {
        return blogService.getLikedPosts(userId);
    }

    @GetMapping("/users/{userId}/favorited")
    public List<BlogPost> getFavoritedPosts(@PathVariable Long userId) {
        return blogService.getFavoritedPosts(userId);
    }

    @PostMapping("/comments/{id}/like")
    public void likeComment(@PathVariable Long id) {
        blogService.likeComment(id);
    }

    @GetMapping("/posts/{id}/comments")
    public List<BlogComment> getComments(@PathVariable Long id) {
        return blogService.getCommentsByPostId(id);
    }

    @GetMapping("/comments")
    public List<BlogComment> getAllComments() {
        return blogService.getAllComments();
    }

    @PostMapping("/posts/{id}/comments")
    public BlogComment addCommentToPost(@PathVariable Long id, @RequestBody BlogComment comment) {
        return blogService.addComment(id, comment);
    }

    @PostMapping("/comments")
    public BlogComment addComment(@RequestBody BlogComment comment) {
        return blogService.addComment(comment);
    }

    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable Long id) {
        blogService.deleteComment(id);
    }
}
