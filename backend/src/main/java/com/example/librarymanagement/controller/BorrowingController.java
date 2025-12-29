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
@CrossOrigin(origins="*", maxAge=3600)
public class BorrowingController {

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody BorrowRequest request) {
        try {
            User user = userService.findByUsername(request.getUsername());
            Book book = bookService.findById(request.getBookId());
            if (user == null || book == null) {
                return ResponseEntity.badRequest().body("User or Book not found");
            }
            return ResponseEntity.ok(borrowingService.borrowBook(user.getUserId(), book.getBookId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestParam Long borrowingId) {
        try {
            return ResponseEntity.ok(borrowingService.returnBook(borrowingId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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