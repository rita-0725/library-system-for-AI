package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        // 检查用户名是否已存在
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 确保角色和状态有默认值
        if (user.getRole() == null) {
            user.setRole(User.Role.STUDENT);
        }
        if (user.getStatus() == null) {
            user.setStatus(User.Status.ACTIVE);
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(java.time.LocalDateTime.now());
        }
        
        // 加密密码
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        // 调试：输出密码信息
        System.out.println("=== Password Debug ===");
        System.out.println("Plain password: " + plainPassword);
        System.out.println("Hashed password: " + hashedPassword);
        
        try {
            boolean result = BCrypt.checkpw(plainPassword, hashedPassword);
            System.out.println("BCrypt check result: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("BCrypt check exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 测试方法：生成正确的 BCrypt 密码哈希（用于验证）
    public static String generatePasswordHash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User save(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }
}