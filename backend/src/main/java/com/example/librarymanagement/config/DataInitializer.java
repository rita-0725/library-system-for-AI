package com.example.librarymanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.librarymanagement.repository.BookRepository;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired(required = false)
    private BookRepository bookRepository;
    
    @Autowired(required = false)
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            if (bookRepository != null) {
                long count = bookRepository.count();
                logger.info("Current books in database: " + count);
                if (count == 0) {
                    logger.warn("No books found in database. Data should be initialized via SQL scripts.");
                }
            } else {
                logger.warn("BookRepository not available yet. Data initialization via SQL scripts only.");
            }
            
            // 初始化测试用户
            if (userRepository != null) {
                long userCount = userRepository.count();
                logger.info("Current users in database: " + userCount);
                
                // 如果用户表为空或只有少于3个用户，创建测试用户
                if (userCount < 3) {
                    logger.info("Creating test users...");
                    
                    // 检查 admin 用户是否存在
                    if (userRepository.findByUsername("admin") == null) {
                        User admin = new User();
                        admin.setUsername("admin");
                        admin.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
                        admin.setRole(User.Role.ADMIN);
                        admin.setStatus(User.Status.ACTIVE);
                        admin.setCreatedAt(LocalDateTime.now());
                        admin.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(admin);
                        logger.info("Admin user created");
                    }
                    
                    // 检查 student1 用户是否存在
                    if (userRepository.findByUsername("student1") == null) {
                        User student = new User();
                        student.setUsername("student1");
                        student.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
                        student.setRole(User.Role.STUDENT);
                        student.setStatus(User.Status.ACTIVE);
                        student.setCreatedAt(LocalDateTime.now());
                        student.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(student);
                        logger.info("Student user created");
                    }
                    
                    // 检查 teacher1 用户是否存在
                    if (userRepository.findByUsername("teacher1") == null) {
                        User teacher = new User();
                        teacher.setUsername("teacher1");
                        teacher.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
                        teacher.setRole(User.Role.TEACHER);
                        teacher.setStatus(User.Status.ACTIVE);
                        teacher.setCreatedAt(LocalDateTime.now());
                        teacher.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(teacher);
                        logger.info("Teacher user created");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error initializing data: " + e.getMessage(), e);
        }
    }
}