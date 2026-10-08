package com.taogueyuan.config;

import com.taogueyuan.entity.BlogPost;
import com.taogueyuan.entity.Product;
import com.taogueyuan.entity.User;
import com.taogueyuan.repository.BlogPostRepository;
import com.taogueyuan.repository.ProductRepository;
import com.taogueyuan.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DataLoader {

    // 演示用种子账号密码（仅用于本地初始化，非真实凭证）
    private static final String DEMO_PASSWORD = "taogueyuan-demo";

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, BlogPostRepository blogPostRepository, ProductRepository productRepository) {
        return args -> {
            // Create Users
            if (userRepository.findByUsername("tao").isEmpty()) {
                User tao = new User();
                tao.setUsername("tao");
                tao.setPassword(DEMO_PASSWORD);
                tao.setNickname("阿桃");
                tao.setRole("ADMIN"); // Changed to ADMIN for tao to test dashboard
                tao.setAvatar("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar%20girl%20illustration&image_size=square");
                userRepository.save(tao);
            }

            if (userRepository.findByUsername("guo").isEmpty()) {
                User guo = new User();
                guo.setUsername("guo");
                guo.setPassword(DEMO_PASSWORD);
                guo.setNickname("阿果");
                guo.setRole("USER"); // Added role
                guo.setAvatar("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar%20boy%20illustration&image_size=square");
                userRepository.save(guo);
            }

            if (userRepository.findByUsername("taotao").isEmpty()) {
                User taotao = new User();
                taotao.setUsername("taotao");
                taotao.setPassword(DEMO_PASSWORD);
                taotao.setNickname("桃桃");
                taotao.setRole("USER"); // Added role
                taotao.setAvatar("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=avatar%20cat%20illustration&image_size=square");
                userRepository.save(taotao);
            }

            // Create Blog Posts
            if (blogPostRepository.count() == 0) {
                User tao = userRepository.findByUsername("tao").orElse(null);
                User guo = userRepository.findByUsername("guo").orElse(null);
                User taotao = userRepository.findByUsername("taotao").orElse(null);

                if (tao != null) {
                    BlogPost post1 = new BlogPost();
                    post1.setTitle("咖啡气泡水，你试过吗？");
                    post1.setContent("这是一个非常清爽的夏日饮品教程...");
                    post1.setCategory("教程");
                    post1.setAuthor(tao);
                    post1.setCoverImage("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=coffee%20soda%20sparkling%20drink&image_size=landscape_4_3");
                    post1.setViews(1209);
                    post1.setLikes(56);
                    blogPostRepository.save(post1);

                    BlogPost post5 = new BlogPost();
                    post5.setTitle("草莓奶油蛋糕");
                    post5.setCategory("烘焙");
                    post5.setAuthor(tao);
                    post5.setCoverImage("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=strawberry%20cream%20cake&image_size=square");
                    post5.setViews(1800);
                    post5.setLikes(64);
                    blogPostRepository.save(post5);
                }

                if (guo != null) {
                    BlogPost post2 = new BlogPost();
                    post2.setTitle("在家也能做夏日清爽甜品");
                    post2.setContent("分享一个简单的甜品做法...");
                    post2.setCategory("分享");
                    post2.setAuthor(guo);
                    post2.setCoverImage("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=summer%20refreshing%20dessert%20homemade&image_size=landscape_4_3");
                    post2.setViews(892);
                    post2.setLikes(32);
                    blogPostRepository.save(post2);

                    BlogPost post4 = new BlogPost();
                    post4.setTitle("粉色樱花");
                    post4.setCategory("花草");
                    post4.setAuthor(guo);
                    post4.setCoverImage("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=pink%20cherry%20blossom%20spring&image_size=portrait_4_3");
                    post4.setViews(2100);
                    post4.setLikes(102);
                    blogPostRepository.save(post4);
                }

                if (taotao != null) {
                    BlogPost post3 = new BlogPost();
                    post3.setTitle("好吃不腻的薯条搭配");
                    post3.setContent("薯条配什么酱最好吃？");
                    post3.setCategory("生活");
                    post3.setAuthor(taotao);
                    post3.setCoverImage("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=french%20fries%20dipping%20sauce&image_size=landscape_4_3");
                    post3.setViews(523);
                    post3.setLikes(103);
                    blogPostRepository.save(post3);
                }
            }

            // Create Market Products
            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("传统红桃粿模具");
                p1.setDescription("手工雕刻实木模具，纹理清晰，经久耐用。");
                p1.setPrice(new BigDecimal("29.90"));
                p1.setStock(100);
                p1.setCategory("工具");
                p1.setImageUrl("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=wooden%20red%20peach%20kueh%20mold%20traditional%20chinese%20kitchen%20tool&image_size=square");
                productRepository.save(p1);

                Product p2 = new Product();
                p2.setName("潮汕粿品礼盒");
                p2.setDescription("精选5种经典口味，送礼佳品。");
                p2.setPrice(new BigDecimal("88.00"));
                p2.setStock(50);
                p2.setCategory("食品");
                p2.setImageUrl("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=teochew%20kueh%20gift%20box%20traditional%20food%20packaging&image_size=square");
                productRepository.save(p2);

                Product p3 = new Product();
                p3.setName("天然鼠壳草粉");
                p3.setDescription("制作鼠壳粿必备原料，天然无添加。");
                p3.setPrice(new BigDecimal("15.50"));
                p3.setStock(200);
                p3.setCategory("食材");
                p3.setImageUrl("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=cudweed%20herb%20powder%20green%20natural%20ingredient&image_size=square");
                productRepository.save(p3);

                Product p4 = new Product();
                p4.setName("手绘粿品明信片");
                p4.setDescription("一套10张，记录潮汕美食文化。");
                p4.setPrice(new BigDecimal("9.90"));
                p4.setStock(500);
                p4.setCategory("文创");
                p4.setImageUrl("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=hand%20painted%20postcard%20teochew%20food%20illustration&image_size=square");
                productRepository.save(p4);

                Product p5 = new Product();
                p5.setName("潮汕功夫茶具套装");
                p5.setDescription("白瓷茶具，品茶必备。");
                p5.setPrice(new BigDecimal("128.00"));
                p5.setStock(30);
                p5.setCategory("茶具");
                p5.setImageUrl("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=white%20porcelain%20kung%20fu%20tea%20set%20traditional%20chinese&image_size=square");
                productRepository.save(p5);

                Product p6 = new Product();
                p6.setName("水晶球模具");
                p6.setDescription("制作水晶球粿的专用模具。");
                p6.setPrice(new BigDecimal("12.00"));
                p6.setStock(150);
                p6.setCategory("工具");
                p6.setImageUrl("https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=transparent%20crystal%20dumpling%20mold%20kitchen%20tool&image_size=square");
                productRepository.save(p6);
            }
        };
    }
}