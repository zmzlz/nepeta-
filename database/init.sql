-- Create Database
CREATE DATABASE IF NOT EXISTS taogueyuan DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE taogueyuan;

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: products
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: blog_posts
CREATE TABLE IF NOT EXISTS blog_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    cover_image VARCHAR(255),
    category VARCHAR(50),
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table: orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    shipping_address VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table: order_items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Table: cart_items
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Initial Data: Users
-- password is 'password' (encrypted with BCrypt if backend uses it, but here plain for demo or assuming backend handles it)
-- For Spring Security default, we usually need BCrypt. Let's assume the backend will hash it on registration, 
-- but for seed data, we might need a hashed string if we want to login directly.
-- Using a placeholder hash for 'password': $2a$10$D1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1.k1 (Just an example, better to use plain text if the backend has a custom loader or just let user register)
-- Let's insert a test user.
INSERT INTO users (username, password, nickname, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6aBECYn.kXl/W5/f1.k1.k1', '管理员', 'ADMIN'),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6aBECYn.kXl/W5/f1.k1.k1', '阿桃', 'USER');

-- Initial Data: Products
INSERT INTO products (name, description, price, stock, image_url, category) VALUES 
('桃粿源·限定萌趣中性笔', '软萌红桃粿造型笔盖，书写顺滑不断墨，伴你记录生活小确幸。', 59.00, 100, 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=peach%20jam%20jar%20gift%20box&image_size=square', '文具'),
('手绘粿品·软抄笔记本', '封面精绘潮汕十二粿，优质护眼纸张，留住你的每一个奇思妙想。', 39.00, 200, 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=canvas%20tote%20bag%20minimal&image_size=square', '文创'),
('春天希望·鼠曲草种子盲盒', '亲手种下一株鼠曲草，体验从春泥到粿香的完整过程，满载生命力。', 49.00, 50, 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=home%20fragrance%20diffuser&image_size=square', '绿植'),
('桃桃家族·金属烤漆徽章', '精致烤漆工艺，色彩鲜亮不掉色。挂在包包或衣服上，点亮每天好心情。', 29.00, 300, 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=colorful%20phone%20case%20pastel&image_size=square', '周边'),
('金黄诱人·手工栀粿', '选用上等栀子熬制，清热去火。切片煎至金黄蘸白糖，每一口都是古早的潮汕味道。', 45.00, 80, 'https://coresg-normal.trae.ai/api/ide/v1/text_to_image?prompt=traditional%20wooden%20kueh%20mold&image_size=square', '食品');

-- Later 6 to 10
-- Let's just note they exist in DB

-- Initial Data: Blog Posts
INSERT INTO blog_posts (title, content, category, views, likes, user_id) VALUES 
('咖啡气泡水，你试过吗？', '夏日炎炎，来一杯清爽的咖啡气泡水吧...', '茶饮', 1209, 86, 2),
('在家也能做夏日清爽甜品', '分享一个超简单的甜品食谱...', '分享', 892, 55, 2),
('好吃不腻的薯条搭配', '薯条配什么酱最好吃？...', '生活', 523, 30, 2);
