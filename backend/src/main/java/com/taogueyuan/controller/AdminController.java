package com.taogueyuan.controller;

import com.taogueyuan.entity.BlogComment;
import com.taogueyuan.entity.Order;
import com.taogueyuan.entity.User;
import com.taogueyuan.service.BlogService;
import com.taogueyuan.service.MarketService;
import com.taogueyuan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserService userService;

    @Autowired
    private BlogService blogService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get last 7 days
        List<String> dates = new ArrayList<>();
        List<Integer> ordersCount = new ArrayList<>();
        List<Integer> usersCount = new ArrayList<>();
        List<Integer> interactionsCount = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();

        List<Order> allOrders = marketService.getAllOrders();
        List<User> allUsers = userService.getAllUsers();
        List<BlogComment> allComments = blogService.getAllComments();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));

            // Count orders
            long orders = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(date))
                    .count();
            ordersCount.add((int) orders);

            // Count users
            long users = allUsers.stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().toLocalDate().equals(date))
                    .count();
            usersCount.add((int) users);

            // Count comments (interactions)
            long comments = allComments.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().equals(date))
                    .count();
            interactionsCount.add((int) comments);
        }

        stats.put("dates", dates);
        stats.put("orders", ordersCount);
        stats.put("users", usersCount);
        stats.put("interactions", interactionsCount);

        return stats;
    }
}
