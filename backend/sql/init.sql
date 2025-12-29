-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- 用户表(users) - 兼容MySQL 5.5基础版
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    status ENUM('active', 'banned') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 创建触发器
DELIMITER //
CREATE TRIGGER users_update_timestamp 
BEFORE UPDATE ON users 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = NOW();
END//
DELIMITER ;

-- 图书表(books) - 兼容MySQL 5.5基础版
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    category VARCHAR(100) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    location VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 创建触发器
DELIMITER //
CREATE TRIGGER books_update_timestamp 
BEFORE UPDATE ON books 
FOR EACH ROW 
BEGIN
    SET NEW.updated_at = NOW();
END//
DELIMITER ;

-- 借阅记录表(borrowings) - 兼容MySQL 5.5基础版
CREATE TABLE IF NOT EXISTS borrowings (
    borrowing_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    fine DECIMAL(10, 2) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 插入图书（初始化不需要用户，请在前端注册新用户）
INSERT INTO books (title, author, isbn, category, stock, location) VALUES
('Java核心技术', 'Cay S. Horstmann', '9787111213826', '计算机', 5, '第一书架A1'),
('设计模式', 'Gang of Four', '9787111213827', '计算机', 3, '第二书架B1'),
('计算机网络', '谢希仁', '9787111135067', '计算机', 4, '第三书架C1'),
('数据库系统概论', '王珊', '9787040353624', '计算机', 6, '第四书架D1'),
('人工智能基础', '周志华', '9787303149415', '计算机', 2, '第五书架E1');