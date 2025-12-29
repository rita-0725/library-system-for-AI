package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Borrowing;
import com.example.librarymanagement.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
}