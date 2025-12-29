package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.Borrowing;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.service.BorrowingService;
import com.example.librarymanagement.service.UserService;
import com.example.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BorrowingController {

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @PostMapping("/borrow")
    public ResponseEntity<Borrowing> borrowBook(@RequestBody BorrowRequest request) {
        try {
            User user = userService.findByUsername(request.getUsername());
            Book book = bookService.findById(request.getBookId());
            if (user == null || book == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(borrowingService.borrowBook(user, book));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/return")
    public ResponseEntity<Borrowing> returnBook(@RequestParam Long borrowingId) {
        try {
            return ResponseEntity.ok(borrowingService.returnBook(borrowingId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/borrowings/{userId}")
    public ResponseEntity<List<Borrowing>> getBorrowingHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByUser(userId));
    }

    @DeleteMapping("/borrowings/{borrowingId}")
    public ResponseEntity<Void> deleteBorrowing(@PathVariable Long borrowingId) {
        // Implement delete logic if needed
        return ResponseEntity.noContent().build();
    }

    public static class BorrowRequest {
        private String username;
        private Long bookId;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }
}