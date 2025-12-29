package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword);
    }

    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    public boolean isAvailable(Long bookId) {
        Book book = findById(bookId);
        return book != null && book.getStock() > 0;
    }

    public void decreaseStock(Long bookId) {
        Book book = findById(bookId);
        if (book != null && book.getStock() > 0) {
            book.setStock(book.getStock() - 1);
            save(book);
        }
    }

    public void increaseStock(Long bookId) {
        Book book = findById(bookId);
        if (book != null) {
            book.setStock(book.getStock() + 1);
            save(book);
        }
    }
}