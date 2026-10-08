package com.taogueyuan.service;

import com.taogueyuan.entity.BlogPost;
import com.taogueyuan.entity.BlogComment;
import com.taogueyuan.repository.BlogPostRepository;
import com.taogueyuan.repository.BlogCommentRepository;
import com.taogueyuan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlogService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCommentRepository blogCommentRepository;

    @Autowired
    private UserRepository userRepository;

    public List<BlogPost> getAllPosts(Long userId) {
        List<BlogPost> posts = blogPostRepository.findAllByOrderByCreatedAtDesc();
        if (userId != null) {
            populateInteractionStatus(posts, userId);
        }
        return posts;
    }

    public List<BlogPost> getPostsByCategory(String category, Long userId) {
        List<BlogPost> posts = blogPostRepository.findByCategoryOrderByCreatedAtDesc(category);
        if (userId != null) {
            populateInteractionStatus(posts, userId);
        }
        return posts;
    }

    public BlogPost getPostById(Long id, Long userId) {
        BlogPost post = blogPostRepository.findById(id).orElse(null);
        if (post != null) {
            post.setViews(post.getViews() == null ? 1 : post.getViews() + 1);
            blogPostRepository.save(post);
            if (userId != null) {
                populateInteractionStatus(java.util.Collections.singletonList(post), userId);
            }
        }
        return post;
    }

    private void populateInteractionStatus(List<BlogPost> posts, Long userId) {
        com.taogueyuan.entity.User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            for (BlogPost post : posts) {
                post.setLiked(user.getLikedPostIds().contains(post.getId()));
                post.setFavorited(user.getFavoritePostIds().contains(post.getId()));
            }
        }
    }

    public BlogPost createPost(BlogPost post) {
        if (post.getAuthor() != null && post.getAuthor().getId() != null) {
            userRepository.findById(post.getAuthor().getId()).ifPresent(post::setAuthor);
        }
        return blogPostRepository.save(post);
    }

    public BlogPost updatePost(Long id, BlogPost postDetails) {
        BlogPost post = getPostById(id, null);
        if (post != null) {
            post.setTitle(postDetails.getTitle());
            post.setContent(postDetails.getContent());
            post.setCategory(postDetails.getCategory());
            post.setCoverImage(postDetails.getCoverImage());
            post.setImages(postDetails.getImages());
            post.setTags(postDetails.getTags());
            
            // 如果前端传来了作者信息，说明是以当前操作者的身份修改了该帖子
            // 根据用户的需求，当管理员修改时，更新作者信息为管理员
            if (postDetails.getAuthor() != null && postDetails.getAuthor().getId() != null) {
                userRepository.findById(postDetails.getAuthor().getId()).ifPresent(post::setAuthor);
            }

            return blogPostRepository.save(post);
        }
        return null;
    }

    @Transactional
    public void deletePost(Long id) {
        blogCommentRepository.deleteByPostId(id);
        blogPostRepository.deleteById(id);
    }

    public void likePost(Long postId, Long userId) {
        BlogPost post = blogPostRepository.findById(postId).orElse(null);
        com.taogueyuan.entity.User user = userRepository.findById(userId).orElse(null);
        
        if (post != null && user != null) {
            if (user.getLikedPostIds().contains(postId)) {
                user.getLikedPostIds().remove(postId);
                post.setLikes(Math.max(0, (post.getLikes() == null ? 0 : post.getLikes()) - 1));
            } else {
                user.getLikedPostIds().add(postId);
                post.setLikes((post.getLikes() == null ? 0 : post.getLikes()) + 1);
            }
            userRepository.save(user);
            blogPostRepository.save(post);
        }
    }

    public void favoritePost(Long postId, Long userId) {
        BlogPost post = blogPostRepository.findById(postId).orElse(null);
        com.taogueyuan.entity.User user = userRepository.findById(userId).orElse(null);

        if (post != null && user != null) {
            if (user.getFavoritePostIds().contains(postId)) {
                user.getFavoritePostIds().remove(postId);
                post.setFavorites(Math.max(0, (post.getFavorites() == null ? 0 : post.getFavorites()) - 1));
            } else {
                user.getFavoritePostIds().add(postId);
                post.setFavorites((post.getFavorites() == null ? 0 : post.getFavorites()) + 1);
            }
            userRepository.save(user);
            blogPostRepository.save(post);
        }
    }

    public List<BlogPost> getLikedPosts(Long userId) {
        com.taogueyuan.entity.User user = userRepository.findById(userId).orElse(null);
        if (user != null && !user.getLikedPostIds().isEmpty()) {
            List<BlogPost> posts = blogPostRepository.findAllById(user.getLikedPostIds());
            populateInteractionStatus(posts, userId);
            return posts;
        }
        return java.util.Collections.emptyList();
    }

    public List<BlogPost> getFavoritedPosts(Long userId) {
        com.taogueyuan.entity.User user = userRepository.findById(userId).orElse(null);
        if (user != null && !user.getFavoritePostIds().isEmpty()) {
            List<BlogPost> posts = blogPostRepository.findAllById(user.getFavoritePostIds());
            populateInteractionStatus(posts, userId);
            return posts;
        }
        return java.util.Collections.emptyList();
    }

    public void likeComment(Long id) {
        BlogComment comment = blogCommentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setLikes(comment.getLikes() == null ? 1 : comment.getLikes() + 1);
            blogCommentRepository.save(comment);
        }
    }

    public List<BlogComment> getCommentsByPostId(Long postId) {
        return blogCommentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public List<BlogComment> getAllComments() {
        return blogCommentRepository.findAllByOrderByCreatedAtDesc();
    }

    public BlogComment addComment(Long postId, BlogComment comment) {
        BlogPost post = getPostById(postId, null);
        if (post != null) {
            comment.setPost(post);
            if (comment.getAuthor() != null && comment.getAuthor().getId() != null) {
                userRepository.findById(comment.getAuthor().getId()).ifPresent(comment::setAuthor);
            }
            // Handle parent comment for replies
            if (comment.getParent() != null && comment.getParent().getId() != null) {
                blogCommentRepository.findById(comment.getParent().getId()).ifPresent(comment::setParent);
            }
            
            // Update comments count
            post.setCommentsCount((post.getCommentsCount() == null ? 0 : post.getCommentsCount()) + 1);
            blogPostRepository.save(post);
            
            return blogCommentRepository.save(comment);
        }
        return null;
    }

    public BlogComment addComment(BlogComment comment) {
        if (comment.getAuthor() != null && comment.getAuthor().getId() != null) {
            userRepository.findById(comment.getAuthor().getId()).ifPresent(comment::setAuthor);
        }
        return blogCommentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        BlogComment comment = blogCommentRepository.findById(id).orElse(null);
        if (comment != null) {
            BlogPost post = comment.getPost();
            if (post != null) {
                post.setCommentsCount(Math.max(0, (post.getCommentsCount() == null ? 0 : post.getCommentsCount()) - 1));
                blogPostRepository.save(post);
            }
            blogCommentRepository.deleteById(id);
        }
    }
}
