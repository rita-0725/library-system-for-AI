package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByUser_UserId(Long userId);
    List<Borrowing> findByBook_BookIdAndReturnDateIsNull(Long bookId);
}