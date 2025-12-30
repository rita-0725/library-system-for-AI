package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("用户服务测试")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole(User.Role.STUDENT);
        testUser.setStatus(User.Status.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    // ============ 注册测试 ============

    @Test
    @DisplayName("成功注册新用户")
    public void testRegisterNewUserSuccess() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User registeredUser = userService.register(newUser);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("newuser", registeredUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("注册时用户名已存在异常")
    public void testRegisterUserAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        when(userRepository.findByUsername("existinguser")).thenReturn(existingUser);

        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setPassword("password123");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.register(newUser),
                "用户名已存在");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("注册时自动设置默认角色为学生")
    public void testRegisterDefaultRoleStudent() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setRole(null);
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // Act
        User registeredUser = userService.register(newUser);

        // Assert
        assertNotNull(registeredUser.getRole());
        assertEquals(User.Role.STUDENT, registeredUser.getRole());
    }

    @Test
    @DisplayName("注册时自动设置默认状态为活跃")
    public void testRegisterDefaultStatusActive() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setStatus(null);
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // Act
        User registeredUser = userService.register(newUser);

        // Assert
        assertNotNull(registeredUser.getStatus());
        assertEquals(User.Status.ACTIVE, registeredUser.getStatus());
    }

    @Test
    @DisplayName("注册时密码被正确加密")
    public void testRegisterPasswordEncrypted() {
        // Arrange
        String plainPassword = "password123";
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword(plainPassword);
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // Act
        User registeredUser = userService.register(newUser);

        // Assert
        assertNotNull(registeredUser.getPassword());
        assertNotEquals(plainPassword, registeredUser.getPassword());
        assertTrue(BCrypt.checkpw(plainPassword, registeredUser.getPassword()));
    }

    // ============ 查找用户测试 ============

    @Test
    @DisplayName("按用户名查找用户成功")
    public void testFindByUsernameSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        User foundUser = userService.findByUsername("testuser");

        // Assert
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("按用户名查找用户不存在")
    public void testFindByUsernameNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        // Act
        User foundUser = userService.findByUsername("nonexistent");

        // Assert
        assertNull(foundUser);
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("按ID查找用户成功")
    public void testFindByIdSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User foundUser = userService.findById(1L);

        // Assert
        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getUserId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("按ID查找用户不存在")
    public void testFindByIdNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        User foundUser = userService.findById(999L);

        // Assert
        assertNull(foundUser);
        verify(userRepository, times(1)).findById(999L);
    }

    // ============ 密码检查测试 ============

    @Test
    @DisplayName("密码检查成功匹配")
    public void testCheckPasswordSuccess() {
        // Arrange
        String plainPassword = "password123";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // Act
        boolean result = userService.checkPassword(plainPassword, hashedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("密码检查失败不匹配")
    public void testCheckPasswordFailed() {
        // Arrange
        String plainPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // Act
        boolean result = userService.checkPassword(wrongPassword, hashedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("密码检查异常处理")
    public void testCheckPasswordException() {
        // Arrange
        String plainPassword = "password123";
        String invalidHash = "invalid";

        // Act
        boolean result = userService.checkPassword(plainPassword, invalidHash);

        // Assert
        assertFalse(result);
    }

    // ============ 用户管理测试 ============

    @Test
    @DisplayName("保存用户成功")
    public void testSaveUserSuccess() {
        // Arrange
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        User savedUser = userService.save(testUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        assertNotNull(savedUser.getUpdatedAt());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("删除用户成功")
    public void testDeleteUserSuccess() {
        // Arrange
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.delete(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("查找所有用户")
    public void testFindAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("testuser2");
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> foundUsers = userService.findAll();

        // Assert
        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("获取用户总数")
    public void testGetTotalUsers() {
        // Arrange
        when(userRepository.count()).thenReturn(5L);

        // Act
        long total = userService.getTotalUsers();

        // Assert
        assertEquals(5L, total);
        verify(userRepository, times(1)).count();
    }

    // ============ 密码哈希生成测试 ============

    @Test
    @DisplayName("生成密码哈希成功")
    public void testGeneratePasswordHashSuccess() {
        // Arrange
        String plainPassword = "testpassword";

        // Act
        String hash = UserService.generatePasswordHash(plainPassword);

        // Assert
        assertNotNull(hash);
        assertNotEquals(plainPassword, hash);
        assertTrue(BCrypt.checkpw(plainPassword, hash));
    }

    @Test
    @DisplayName("生成的密码哈希每次都不同（盐值不同）")
    public void testGeneratePasswordHashDifferentEachTime() {
        // Arrange
        String plainPassword = "testpassword";

        // Act
        String hash1 = UserService.generatePasswordHash(plainPassword);
        String hash2 = UserService.generatePasswordHash(plainPassword);

        // Assert
        assertNotEquals(hash1, hash2);
        assertTrue(BCrypt.checkpw(plainPassword, hash1));
        assertTrue(BCrypt.checkpw(plainPassword, hash2));
    }
}
