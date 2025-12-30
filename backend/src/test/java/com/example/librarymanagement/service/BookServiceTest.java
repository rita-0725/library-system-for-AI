package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Book;
import com.example.librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("图书服务测试")
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    public void setUp() {
        testBook = new Book();
        testBook.setBookId(1L);
        testBook.setTitle("Java核心技术");
        testBook.setAuthor("Cay S. Horstmann");
        testBook.setIsbn("978-7-111-55662-9");
        testBook.setCategory("Programming");
        testBook.setStock(5);
        testBook.setLocation("A1-01");
    }

    // ============ 搜索图书测试 ============

    @Test
    @DisplayName("搜索图书成功")
    public void testSearchBooksSuccess() {
        // Arrange
        Book book2 = new Book();
        book2.setBookId(2L);
        book2.setTitle("Java设计模式");
        book2.setAuthor("Gang of Four");
        List<Book> books = Arrays.asList(testBook, book2);
        when(bookRepository.searchBooks("Java")).thenReturn(books);

        // Act
        List<Book> results = bookService.searchBooks("Java");

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getTitle().contains("Java")));
        verify(bookRepository, times(1)).searchBooks("Java");
    }

    @Test
    @DisplayName("搜索图书无结果")
    public void testSearchBooksNoResults() {
        // Arrange
        when(bookRepository.searchBooks("NotExist")).thenReturn(Arrays.asList());

        // Act
        List<Book> results = bookService.searchBooks("NotExist");

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(bookRepository, times(1)).searchBooks("NotExist");
    }

    @Test
    @DisplayName("搜索图书关键字为空")
    public void testSearchBooksEmptyKeyword() {
        // Arrange
        when(bookRepository.searchBooks("")).thenReturn(Arrays.asList());

        // Act
        List<Book> results = bookService.searchBooks("");

        // Assert
        assertNotNull(results);
        verify(bookRepository, times(1)).searchBooks("");
    }

    // ============ 查找图书测试 ============

    @Test
    @DisplayName("按ID查找图书成功")
    public void testFindByIdSuccess() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        Book foundBook = bookService.findById(1L);

        // Assert
        assertNotNull(foundBook);
        assertEquals("Java核心技术", foundBook.getTitle());
        assertEquals(1L, foundBook.getBookId());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("按ID查找图书不存在")
    public void testFindByIdNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Book foundBook = bookService.findById(999L);

        // Assert
        assertNull(foundBook);
        verify(bookRepository, times(1)).findById(999L);
    }

    // ============ 库存检查测试 ============

    @Test
    @DisplayName("图书有库存可用")
    public void testIsAvailableSuccess() {
        // Arrange
        testBook.setStock(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        boolean available = bookService.isAvailable(1L);

        // Assert
        assertTrue(available);
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("图书库存为零不可用")
    public void testIsAvailableZeroStock() {
        // Arrange
        testBook.setStock(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        boolean available = bookService.isAvailable(1L);

        // Assert
        assertFalse(available);
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("图书不存在不可用")
    public void testIsAvailableBookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean available = bookService.isAvailable(999L);

        // Assert
        assertFalse(available);
        verify(bookRepository, times(1)).findById(999L);
    }

    // ============ 库存更新测试 ============

    @Test
    @DisplayName("减少库存成功")
    public void testDecreaseStockSuccess() {
        // Arrange
        testBook.setStock(5);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        // Act
        bookService.decreaseStock(1L);

        // Assert
        assertEquals(4, testBook.getStock());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("减少库存不能为负数")
    public void testDecreaseStockNotNegative() {
        // Arrange
        testBook.setStock(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        bookService.decreaseStock(1L);

        // Assert
        assertEquals(0, testBook.getStock());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("减少库存图书不存在")
    public void testDecreaseStockBookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        bookService.decreaseStock(999L);

        // Assert
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("增加库存成功")
    public void testIncreaseStockSuccess() {
        // Arrange
        testBook.setStock(3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        // Act
        bookService.increaseStock(1L);

        // Assert
        assertEquals(4, testBook.getStock());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("增加库存图书不存在")
    public void testIncreaseStockBookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        bookService.increaseStock(999L);

        // Assert
        verify(bookRepository, never()).save(any(Book.class));
    }

    // ============ 图书管理测试 ============

    @Test
    @DisplayName("保存图书成功")
    public void testSaveBookSuccess() {
        // Arrange
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book savedBook = bookService.save(testBook);

        // Assert
        assertNotNull(savedBook);
        assertEquals("Java核心技术", savedBook.getTitle());
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("删除图书成功")
    public void testDeleteBookSuccess() {
        // Arrange
        doNothing().when(bookRepository).deleteById(1L);

        // Act
        bookService.delete(1L);

        // Assert
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("查找所有图书")
    public void testFindAllBooks() {
        // Arrange
        Book book2 = new Book();
        book2.setBookId(2L);
        book2.setTitle("设计模式");
        List<Book> books = Arrays.asList(testBook, book2);
        when(bookRepository.findAll()).thenReturn(books);

        // Act
        List<Book> allBooks = bookService.findAll();

        // Assert
        assertNotNull(allBooks);
        assertEquals(2, allBooks.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("获取图书总数")
    public void testGetTotalBooks() {
        // Arrange
        when(bookRepository.count()).thenReturn(10L);

        // Act
        long total = bookService.getTotalBooks();

        // Assert
        assertEquals(10L, total);
        verify(bookRepository, times(1)).count();
    }

    @Test
    @DisplayName("创建新图书")
    public void testCreateNewBook() {
        // Arrange
        Book newBook = new Book();
        newBook.setTitle("新图书");
        newBook.setAuthor("新作者");
        newBook.setIsbn("123-456-789");
        newBook.setCategory("Technology");
        newBook.setStock(10);
        newBook.setLocation("B1-01");
        when(bookRepository.save(newBook)).thenReturn(newBook);

        // Act
        Book savedBook = bookService.save(newBook);

        // Assert
        assertNotNull(savedBook);
        assertEquals("新图书", savedBook.getTitle());
        assertEquals("新作者", savedBook.getAuthor());
        assertEquals(10, savedBook.getStock());
        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    @DisplayName("更新图书信息")
    public void testUpdateBook() {
        // Arrange
        testBook.setTitle("更新的标题");
        testBook.setStock(8);
        when(bookRepository.save(testBook)).thenReturn(testBook);

        // Act
        Book updatedBook = bookService.save(testBook);

        // Assert
        assertEquals("更新的标题", updatedBook.getTitle());
        assertEquals(8, updatedBook.getStock());
        verify(bookRepository, times(1)).save(testBook);
    }
}
