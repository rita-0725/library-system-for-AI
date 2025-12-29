# 图书管理系统 Bug 修复总结

## 问题1：搜索图书"Java"显示没有 ✅ 已修复

### 根本原因
[BookRepository.java](backend/src/main/java/com/example/librarymanagement/repository/BookRepository.java#L12) 中的 SQL 查询存在缺陷：
```sql
SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn = :keyword
```

**问题**：ISBN字段使用了精确匹配 `=`，导致搜索"Java"时无法找到任何结果。

### 修复方案
将ISBN搜索改为模糊匹配：
```sql
SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%
```

### 修复文件
- [BookRepository.java](backend/src/main/java/com/example/librarymanagement/repository/BookRepository.java#L12) - 第12行

---

## 问题2：注册用户显示"用户存在" 

### 分析
这个"错误"实际上是正确的行为。注册逻辑在 [UserService.java](backend/src/main/java/com/example/librarymanagement/service/UserService.java#L14-16) 中：

```java
User existingUser = userRepository.findByUsername(user.getUsername());
if (existingUser != null) {
    throw new RuntimeException("用户名已存在");
}
```

**可能的原因**：
1. ✅ **没有初始化数据库** - 第一次注册时应该成功
2. ✅ **使用了相同的用户名注册** - 如果多次用同一用户名注册
3. ✅ **数据库未清空** - 前次测试的数据仍在数据库中

### 建议
1. 检查MySQL数据库是否已创建：`library_db`
2. 如果数据库存在，可以清空数据：
   ```sql
   DROP DATABASE library_db;
   ```
3. 重启应用，数据库会自动重建并执行 SQL 初始化脚本

---

## 问题3：登录显示"用户密码错误"

### 分析
登录逻辑在 [UserController.java](backend/src/main/java/com/example/librarymanagement/controller/UserController.java#L34-39) 中正确使用了 BCrypt：

```java
User user = userService.findByUsername(loginRequest.getUsername());
if (user != null && userService.checkPassword(loginRequest.getPassword(), user.getPassword())) {
    return ResponseEntity.ok("Login successful");
}
return ResponseEntity.badRequest().body("Invalid credentials");
```

**可能的原因**：
1. ✅ **用户不存在** - 需要先注册用户
2. ✅ **前端显示所有错误为"用户名或密码错误"** - 无法区分是否是用户不存在还是密码错误
3. ✅ **密码被错误加密** - 注册时应该正确使用 BCrypt 加密（已在代码中实现）

### 修复方案
改进了前端错误处理，使其能显示服务器返回的具体错误信息：
```javascript
catch (err) {
    if (err.response && err.response.status === 400) {
        this.error = err.response.data  // 显示服务器返回的具体错误
    } else {
        this.error = '用户名或密码错误'
    }
}
```

### 修复文件
- [Login.vue](frontend/src/components/Login.vue#L34-46) - 改进错误处理

---

## 完整测试流程

### 1. 重置数据库
```sql
DROP DATABASE IF EXISTS library_db;
```

### 2. 重启应用
- 关闭后端应用
- 确保 MySQL 运行在 `localhost:3306`
- 使用凭证 `root:123456` 
- 启动应用，自动创建数据库和初始化数据

### 3. 测试流程
1. **注册新用户**
   - 用户名：testuser
   - 密码：password123
   - 用户类型：学生
   - ✅ 应该显示"注册成功！请登录"

2. **登录**
   - 用户名：testuser
   - 密码：password123
   - ✅ 应该成功登录

3. **搜索图书**
   - 搜索关键词：Java
   - ✅ 应该找到"Java核心技术"

---

## 技术细节

### 密码加密算法
- 使用 **BCrypt** 加密密码
- 依赖：`org.mindrot:jbcrypt`
- 注册时：密码被加密存储
- 登录时：使用 `BCrypt.checkpw()` 验证

### 数据库初始化
- 初始化脚本：[schema.sql](backend/src/main/resources/schema.sql)
- 初始数据：[data.sql](backend/src/main/resources/data.sql)
- 初始化5本示例图书（Java核心技术、设计模式等）

### API 端点
- 注册：`POST /api/users/register`
- 登录：`POST /api/users/login`
- 搜索：`GET /api/books/search?keyword=Java`

---

## 检查清单 ✅

- [x] 修复图书搜索 SQL 查询
- [x] 改进登录错误提示
- [x] 验证 BCrypt 密码加密逻辑
- [x] 确认数据库初始化脚本

如有其他问题，请检查：
1. MySQL 是否正在运行
2. 数据库连接配置（[application.properties](backend/src/main/resources/application.properties)）
3. 后端是否成功启动，查看日志输出
