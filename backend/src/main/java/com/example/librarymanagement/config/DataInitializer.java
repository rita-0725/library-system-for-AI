package com.example.librarymanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.librarymanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired(required = false)
    private BookRepository bookRepository;

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
        } catch (Exception e) {
            logger.error("Error checking database: " + e.getMessage());
        }
    }
}