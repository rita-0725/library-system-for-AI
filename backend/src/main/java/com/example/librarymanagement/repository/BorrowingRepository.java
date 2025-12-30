package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByUserId(Long userId);
    List<Borrowing> findByBookIdAndReturnDateIsNull(Long bookId);
    List<Borrowing> findByReturnDateIsNull();

    @Query(value = "SELECT b.book_id, bk.title, COUNT(*) as borrow_count " +
                   "FROM borrowings b " +
                   "JOIN books bk ON b.book_id = bk.book_id " +
                   "GROUP BY b.book_id, bk.title " +
                   "ORDER BY borrow_count DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getPopularBooks();

    @Query(value = "SELECT u.user_id, u.username, COUNT(*) as borrow_count " +
                   "FROM borrowings b " +
                   "JOIN users u ON b.user_id = u.user_id " +
                   "GROUP BY u.user_id, u.username " +
                   "ORDER BY borrow_count DESC", nativeQuery = true)
    List<Object[]> getUserBorrowingStats();
}