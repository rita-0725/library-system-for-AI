package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.Borrowing;
import com.example.librarymanagement.service.BookService;
import com.example.librarymanagement.service.UserService;
import com.example.librarymanagement.service.BorrowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowingService borrowingService;

    // ===== 图书管理 =====
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @PostMapping("/books")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        return ResponseEntity.ok(bookService.save(book));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = bookService.findById(id);
        if (book != null) {
            book.setTitle(bookDetails.getTitle());
            book.setAuthor(bookDetails.getAuthor());
            book.setIsbn(bookDetails.getIsbn());
            book.setCategory(bookDetails.getCategory());
            book.setStock(bookDetails.getStock());
            book.setLocation(bookDetails.getLocation());
            return ResponseEntity.ok(bookService.save(book));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long userId, @RequestParam String status) {
        User user = userService.findById(userId);
        if (user != null) {
            user.setStatus(User.Status.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(userService.save(user));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    // ===== 统计报表 =====
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalBooks", bookService.getTotalBooks());
        stats.put("totalBorrowings", borrowingService.getTotalBorrowings());
        stats.put("overdueCount", borrowingService.getOverdueCount());
        stats.put("activeBorrowings", borrowingService.getActiveBorrowingsCount());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/popular-books")
    public ResponseEntity<List<Map<String, Object>>> getPopularBooks() {
        return ResponseEntity.ok(borrowingService.getPopularBooks());
    }

    @GetMapping("/borrowing-records")
    public ResponseEntity<List<Borrowing>> getAllBorrowingRecords() {
        return ResponseEntity.ok(borrowingService.getAllBorrowings());
    }

    @GetMapping("/overdue-records")
    public ResponseEntity<List<Borrowing>> getOverdueRecords() {
        return ResponseEntity.ok(borrowingService.getOverdueRecords());
    }

    @GetMapping("/user-borrowing-stats")
    public ResponseEntity<List<Map<String, Object>>> getUserBorrowingStats() {
        return ResponseEntity.ok(borrowingService.getUserBorrowingStats());
    }
}
