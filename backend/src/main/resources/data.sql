-- Clear existing data and insert fresh sample books
DELETE FROM books;
DELETE FROM users;

-- 用户将由 DataInitializer 在应用启动时创建

INSERT INTO books (title, author, isbn, category, stock, location) VALUES
('Java核心技术', 'Cay S. Horstmann', '978-7-111-55662-9', 'Programming', 5, 'A1-01'),
('设计模式', 'Gang of Four', '978-0-201-63361-0', 'Programming', 3, 'A1-02'),
('计算机网络', 'Andrew S. Tanenbaum', '978-0-13-359440-9', 'Networking', 4, 'B2-01'),
('数据库系统原理', 'Henry F. Korth', '978-0-07-120413-2', 'Database', 2, 'B2-02'),
('人工智能基础', 'David L. Poole', '978-0-19-957744-8', 'AI', 3, 'C3-01');
