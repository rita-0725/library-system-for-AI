# 图书管理系统 - Library Management System

## 项目概述
这是一个基于Spring Boot + Vue.js的现代化图书管理系统，支持图书检索、借阅管理、用户认证等功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 5.5.14
- **ORM**: Spring Data JPA
- **密码加密**: BCrypt
- **Java版本**: 21

### 前端
- **框架**: Vue.js 3
- **构建工具**: Vite
- **路由**: Vue Router
- **HTTP客户端**: Axios

## 项目结构

```
图书管理系统/
├── backend/                          # 后端项目
│   ├── pom.xml                      # Maven配置文件
│   ├── sql/
│   │   └── init.sql                 # 数据库初始化脚本
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/librarymanagement/
│           │       ├── entity/      # 实体类
│           │       ├── repository/  # 数据访问层
│           │       ├── service/     # 业务逻辑层
│           │       └── controller/  # 控制层
│           └── resources/
│               └── application.properties
│
└── frontend/                        # 前端项目
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── components/
        │   ├── Home.vue
        │   ├── Login.vue
        │   ├── Register.vue
        │   ├── BookSearch.vue
        │   └── BorrowingHistory.vue
        └── router/
            └── index.js
```

## 主要功能

### 1. 用户管理
- ✅ 用户注册（学生、教师、管理员）
- ✅ 用户登录认证
- ✅ 密码加密存储（BCrypt）
- ✅ 用户账户状态管理

### 2. 图书检索与查询
- ✅ 按书名、作者、ISBN等多条件查询
- ✅ 显示图书详细信息
- ✅ 库存状态查询

### 3. 借阅与归还管理
- ✅ 借书功能（自动检查库存）
- ✅ 还书功能
- ✅ 逾期费用计算
- ✅ 借阅历史记录

### 4. 馆藏信息维护
- ✅ 管理员增删改查图书信息
- ✅ 库存管理
- ✅ 图书分类

### 5. 数据统计与报表
- 后续实现

## 数据库设计

### users 表
```sql
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    status ENUM('active', 'banned') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### books 表
```sql
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    category TEXT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    location VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### borrowings 表
```sql
CREATE TABLE borrowings (
    borrowing_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    fine DECIMAL(10, 2) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);
```

## API 接口设计

### 用户相关
| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 注册 | POST | /api/users/register | 用户注册 |
| 登录 | POST | /api/users/login | 用户登录 |
| 更新用户 | PUT | /api/users/{userId} | 更新用户信息 |

### 图书相关
| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 图书查询 | GET | /api/books/search | 多条件查询图书 |
| 获取图书 | GET | /api/books/{id} | 获取图书详情 |
| 创建图书 | POST | /api/books | 创建图书（管理员） |
| 更新图书 | PUT | /api/books/{id} | 更新图书信息 |
| 删除图书 | DELETE | /api/books/{id} | 删除图书 |
| 查询库存 | GET | /api/books/{id}/stock | 查询库存 |
| 更新库存 | PUT | /api/books/{id}/stock | 更新库存 |

### 借阅相关
| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 借书 | POST | /api/borrow | 借书 |
| 还书 | POST | /api/return | 还书 |
| 借阅历史 | GET | /api/borrowings/{userId} | 查看借阅历史 |
| 删除记录 | DELETE | /api/borrowings/{borrowingId} | 删除借阅记录 |

## 安装与运行

### 后端启动

1. 确保已安装 Java 21 和 Maven
2. 修改数据库配置：编辑 `backend/src/main/resources/application.properties`
3. 创建数据库并导入初始化脚本：
   ```bash
   mysql -u root -p < backend/sql/init.sql
   ```
4. 启动Spring Boot应用：
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   后端默认运行在 http://localhost:8080

### 前端启动

1. 确保已安装 Node.js 和 npm
2. 安装依赖：
   ```bash
   cd frontend
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
   前端默认运行在 http://localhost:5173

## 非功能性需求

- 🚀 **响应时间**: 列表页加载 ≤1.5s
- 🔒 **安全性**: 密码使用BCrypt加密存储
- 🔧 **可维护性**: 模块化设计，接口清晰
- 📱 **兼容性**: 支持现代浏览器

## 测试账户

- **管理员**: admin / password（需通过BCrypt验证）
- **学生**: student1 / password
- **教师**: teacher1 / password

## 下一步计划

1. ✅ 核心功能实现
2. ⏳ 统计报表功能
3. ⏳ 权限控制完善
4. ⏳ 单元测试
5. ⏳ 部署优化

## 许可证

MIT License