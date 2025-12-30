package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Borrowing;
import com.example.librarymanagement.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowingService {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookService bookService;

    public Borrowing borrowBook(Long userId, Long bookId) {
        if (!bookService.isAvailable(bookId)) {
            throw new RuntimeException("Book not available");
        }
        Borrowing borrowing = new Borrowing();
        borrowing.setUserId(userId);
        borrowing.setBookId(bookId);
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(14)); // 14 days loan period
        bookService.decreaseStock(bookId);
        return borrowingRepository.save(borrowing);
    }

    public Borrowing returnBook(Long borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId).orElse(null);
        if (borrowing == null || borrowing.getReturnDate() != null) {
            throw new RuntimeException("Invalid borrowing record");
        }
        borrowing.setReturnDate(LocalDateTime.now());
        if (borrowing.getReturnDate().isAfter(borrowing.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(borrowing.getDueDate(), borrowing.getReturnDate());
            borrowing.setFine(BigDecimal.valueOf(daysOverdue * 0.5)); // 0.5 per day
        }
        bookService.increaseStock(borrowing.getBookId());
        return borrowingRepository.save(borrowing);
    }

    public List<Borrowing> getBorrowingsByUser(Long userId) {
        return borrowingRepository.findByUserId(userId);
    }

    public List<Borrowing> getAllBorrowings() {
        return borrowingRepository.findAll();
    }

    public long getTotalBorrowings() {
        return borrowingRepository.count();
    }

    public long getActiveBorrowingsCount() {
        return borrowingRepository.findByReturnDateIsNull().size();
    }

    public List<Borrowing> getOverdueRecords() {
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> allBorrowings = borrowingRepository.findByReturnDateIsNull();
        List<Borrowing> overdueRecords = new ArrayList<>();
        for (Borrowing b : allBorrowings) {
            if (b.getDueDate().isBefore(now)) {
                overdueRecords.add(b);
            }
        }
        return overdueRecords;
    }

    public long getOverdueCount() {
        return getOverdueRecords().size();
    }

    public List<Map<String, Object>> getPopularBooks() {
        List<Object[]> results = borrowingRepository.getPopularBooks();
        List<Map<String, Object>> popularBooks = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> book = new HashMap<>();
            book.put("bookId", row[0]);
            book.put("title", row[1]);
            book.put("borrowCount", row[2]);
            popularBooks.add(book);
        }
        return popularBooks;
    }

    public List<Map<String, Object>> getUserBorrowingStats() {
        List<Object[]> results = borrowingRepository.getUserBorrowingStats();
        List<Map<String, Object>> stats = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("userId", row[0]);
            stat.put("username", row[1]);
            stat.put("borrowCount", row[2]);
            stats.add(stat);
        }
        return stats;
    }
}