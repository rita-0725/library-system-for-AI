package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.Borrowing;
import com.example.librarymanagement.repository.BorrowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("借阅服务测试")
public class BorrowingServiceTest {

    @Autowired
    private BorrowingService borrowingService;

    @MockBean
    private BorrowingRepository borrowingRepository;

    @MockBean
    private BookService bookService;

    private Borrowing testBorrowing;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.now();
        testBorrowing = new Borrowing();
        testBorrowing.setBorrowingId(1L);
        testBorrowing.setUserId(1L);
        testBorrowing.setBookId(1L);
        testBorrowing.setBorrowDate(now);
        testBorrowing.setDueDate(now.plusDays(14));
        testBorrowing.setReturnDate(null);
        testBorrowing.setFine(BigDecimal.ZERO);
    }

    // ============ 借书测试 ============

    @Test
    @DisplayName("成功借书")
    public void testBorrowBookSuccess() {
        // Arrange
        when(bookService.isAvailable(1L)).thenReturn(true);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing borrowing = invocation.getArgument(0);
            borrowing.setBorrowingId(1L);
            return borrowing;
        });
        doNothing().when(bookService).decreaseStock(1L);

        // Act
        Borrowing borrowing = borrowingService.borrowBook(1L, 1L);

        // Assert
        assertNotNull(borrowing);
        assertEquals(1L, borrowing.getUserId());
        assertEquals(1L, borrowing.getBookId());
        assertNotNull(borrowing.getBorrowDate());
        assertNotNull(borrowing.getDueDate());
        assertNull(borrowing.getReturnDate());
        verify(bookService, times(1)).decreaseStock(1L);
        verify(borrowingRepository, times(1)).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("图书不可用时借书失败")
    public void testBorrowBookNotAvailable() {
        // Arrange
        when(bookService.isAvailable(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> borrowingService.borrowBook(1L, 1L),
                "Book not available");
        verify(bookService, never()).decreaseStock(1L);
        verify(borrowingRepository, never()).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("借书时设置正确的应还日期（14天）")
    public void testBorrowBookCorrectDueDate() {
        // Arrange
        when(bookService.isAvailable(1L)).thenReturn(true);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing borrowing = invocation.getArgument(0);
            return borrowing;
        });
        doNothing().when(bookService).decreaseStock(1L);

        // Act
        LocalDateTime beforeBorrow = LocalDateTime.now();
        Borrowing borrowing = borrowingService.borrowBook(1L, 1L);
        LocalDateTime afterBorrow = LocalDateTime.now();

        // Assert
        assertNotNull(borrowing.getDueDate());
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
                borrowing.getBorrowDate(),
                borrowing.getDueDate()
        );
        assertEquals(14, daysDiff);
    }

    // ============ 归还书籍测试 ============

    @Test
    @DisplayName("成功归还书籍")
    public void testReturnBookSuccess() {
        // Arrange
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing borrowing = invocation.getArgument(0);
            return borrowing;
        });
        doNothing().when(bookService).increaseStock(1L);

        // Act
        Borrowing returnedBorrowing = borrowingService.returnBook(1L);

        // Assert
        assertNotNull(returnedBorrowing.getReturnDate());
        assertNotNull(returnedBorrowing.getFine());
        verify(bookService, times(1)).increaseStock(1L);
        verify(borrowingRepository, times(1)).save(any(Borrowing.class));
    }

    @Test
    @DisplayName("归还不存在的借阅记录异常")
    public void testReturnBookNotFound() {
        // Arrange
        when(borrowingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> borrowingService.returnBook(999L),
                "Invalid borrowing record");
        verify(bookService, never()).increaseStock(any());
    }

    @Test
    @DisplayName("重复归还同一记录异常")
    public void testReturnBookAlreadyReturned() {
        // Arrange
        testBorrowing.setReturnDate(LocalDateTime.now().minusDays(1));
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> borrowingService.returnBook(1L),
                "Invalid borrowing record");
        verify(bookService, never()).increaseStock(any());
    }

    @Test
    @DisplayName("按时归还不计罚款")
    public void testReturnBookOnTimeNoFine() {
        // Arrange
        testBorrowing.setBorrowDate(now.minusDays(10));
        testBorrowing.setDueDate(now.plusDays(4));
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing borrowing = invocation.getArgument(0);
            return borrowing;
        });
        doNothing().when(bookService).increaseStock(1L);

        // Act
        Borrowing returnedBorrowing = borrowingService.returnBook(1L);

        // Assert
        assertNotNull(returnedBorrowing.getReturnDate());
        assertEquals(BigDecimal.ZERO, returnedBorrowing.getFine());
    }

    @Test
    @DisplayName("逾期归还计算罚款")
    public void testReturnBookOverdueWithFine() {
        // Arrange
        testBorrowing.setBorrowDate(now.minusDays(20));
        testBorrowing.setDueDate(now.minusDays(6)); // 已逾期6天
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(testBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing borrowing = invocation.getArgument(0);
            return borrowing;
        });
        doNothing().when(bookService).increaseStock(1L);

        // Act
        Borrowing returnedBorrowing = borrowingService.returnBook(1L);

        // Assert
        assertNotNull(returnedBorrowing.getFine());
        assertTrue(returnedBorrowing.getFine().compareTo(BigDecimal.ZERO) > 0);
        // 罚款 = 逾期天数 * 0.5
        assertTrue(returnedBorrowing.getFine().doubleValue() >= 3.0);
    }

    // ============ 查询借阅记录测试 ============

    @Test
    @DisplayName("查询用户的借阅记录成功")
    public void testGetBorrowingsByUserSuccess() {
        // Arrange
        Borrowing borrowing2 = new Borrowing();
        borrowing2.setBorrowingId(2L);
        borrowing2.setUserId(1L);
        borrowing2.setBookId(2L);
        List<Borrowing> borrowings = Arrays.asList(testBorrowing, borrowing2);
        when(borrowingRepository.findByUserId(1L)).thenReturn(borrowings);

        // Act
        List<Borrowing> userBorrowings = borrowingService.getBorrowingsByUser(1L);

        // Assert
        assertNotNull(userBorrowings);
        assertEquals(2, userBorrowings.size());
        assertTrue(userBorrowings.stream().allMatch(b -> b.getUserId().equals(1L)));
        verify(borrowingRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("查询用户无借阅记录")
    public void testGetBorrowingsByUserNoRecords() {
        // Arrange
        when(borrowingRepository.findByUserId(999L)).thenReturn(Arrays.asList());

        // Act
        List<Borrowing> userBorrowings = borrowingService.getBorrowingsByUser(999L);

        // Assert
        assertNotNull(userBorrowings);
        assertTrue(userBorrowings.isEmpty());
        verify(borrowingRepository, times(1)).findByUserId(999L);
    }

    @Test
    @DisplayName("查询所有借阅记录")
    public void testGetAllBorrowings() {
        // Arrange
        Borrowing borrowing2 = new Borrowing();
        borrowing2.setBorrowingId(2L);
        List<Borrowing> borrowings = Arrays.asList(testBorrowing, borrowing2);
        when(borrowingRepository.findAll()).thenReturn(borrowings);

        // Act
        List<Borrowing> allBorrowings = borrowingService.getAllBorrowings();

        // Assert
        assertNotNull(allBorrowings);
        assertEquals(2, allBorrowings.size());
        verify(borrowingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("获取借阅总数")
    public void testGetTotalBorrowings() {
        // Arrange
        when(borrowingRepository.count()).thenReturn(25L);

        // Act
        long total = borrowingService.getTotalBorrowings();

        // Assert
        assertEquals(25L, total);
        verify(borrowingRepository, times(1)).count();
    }

    // ============ 活跃借阅记录测试 ============

    @Test
    @DisplayName("获取活跃借阅计数")
    public void testGetActiveBorrowingsCount() {
        // Arrange
        Borrowing borrowing2 = new Borrowing();
        borrowing2.setBorrowingId(2L);
        borrowing2.setReturnDate(null);
        List<Borrowing> activeBorrowings = Arrays.asList(testBorrowing, borrowing2);
        when(borrowingRepository.findByReturnDateIsNull()).thenReturn(activeBorrowings);

        // Act
        long count = borrowingService.getActiveBorrowingsCount();

        // Assert
        assertEquals(2L, count);
        verify(borrowingRepository, times(1)).findByReturnDateIsNull();
    }

    @Test
    @DisplayName("无活跃借阅记录")
    public void testGetActiveBorrowingsCountZero() {
        // Arrange
        when(borrowingRepository.findByReturnDateIsNull()).thenReturn(Arrays.asList());

        // Act
        long count = borrowingService.getActiveBorrowingsCount();

        // Assert
        assertEquals(0L, count);
        verify(borrowingRepository, times(1)).findByReturnDateIsNull();
    }

    // ============ 逾期记录测试 ============

    @Test
    @DisplayName("获取逾期借阅记录")
    public void testGetOverdueRecords() {
        // Arrange
        Borrowing overdueBorrowing = new Borrowing();
        overdueBorrowing.setBorrowingId(1L);
        overdueBorrowing.setDueDate(now.minusDays(5)); // 已逾期
        overdueBorrowing.setReturnDate(null);

        Borrowing normalBorrowing = new Borrowing();
        normalBorrowing.setBorrowingId(2L);
        normalBorrowing.setDueDate(now.plusDays(5)); // 未逾期
        normalBorrowing.setReturnDate(null);

        List<Borrowing> activeBorrowings = Arrays.asList(overdueBorrowing, normalBorrowing);
        when(borrowingRepository.findByReturnDateIsNull()).thenReturn(activeBorrowings);

        // Act
        List<Borrowing> overdueRecords = borrowingService.getOverdueRecords();

        // Assert
        assertNotNull(overdueRecords);
        assertEquals(1, overdueRecords.size());
        assertTrue(overdueRecords.get(0).getDueDate().isBefore(now));
        verify(borrowingRepository, times(1)).findByReturnDateIsNull();
    }

    @Test
    @DisplayName("无逾期记录")
    public void testGetOverdueRecordsNone() {
        // Arrange
        testBorrowing.setDueDate(now.plusDays(10)); // 未逾期
        List<Borrowing> activeBorrowings = Arrays.asList(testBorrowing);
        when(borrowingRepository.findByReturnDateIsNull()).thenReturn(activeBorrowings);

        // Act
        List<Borrowing> overdueRecords = borrowingService.getOverdueRecords();

        // Assert
        assertNotNull(overdueRecords);
        assertTrue(overdueRecords.isEmpty());
    }

    @Test
    @DisplayName("获取逾期计数")
    public void testGetOverdueCount() {
        // Arrange
        Borrowing overdueBorrowing = new Borrowing();
        overdueBorrowing.setDueDate(now.minusDays(5));
        overdueBorrowing.setReturnDate(null);
        List<Borrowing> activeBorrowings = Arrays.asList(overdueBorrowing);
        when(borrowingRepository.findByReturnDateIsNull()).thenReturn(activeBorrowings);

        // Act
        long count = borrowingService.getOverdueCount();

        // Assert
        assertEquals(1L, count);
    }

    // ============ 统计数据测试 ============

    @Test
    @DisplayName("获取热门图书列表")
    public void testGetPopularBooks() {
        // Arrange
        Object[] book1 = {1L, "Java核心技术", 10L};
        Object[] book2 = {2L, "设计模式", 8L};
        List<Object[]> results = Arrays.asList(book1, book2);
        when(borrowingRepository.getPopularBooks()).thenReturn(results);

        // Act
        List<Map<String, Object>> popularBooks = borrowingService.getPopularBooks();

        // Assert
        assertNotNull(popularBooks);
        assertEquals(2, popularBooks.size());
        assertEquals(1L, popularBooks.get(0).get("bookId"));
        assertEquals("Java核心技术", popularBooks.get(0).get("title"));
        assertEquals(10L, popularBooks.get(0).get("borrowCount"));
        verify(borrowingRepository, times(1)).getPopularBooks();
    }

    @Test
    @DisplayName("无热门图书")
    public void testGetPopularBooksEmpty() {
        // Arrange
        when(borrowingRepository.getPopularBooks()).thenReturn(Arrays.asList());

        // Act
        List<Map<String, Object>> popularBooks = borrowingService.getPopularBooks();

        // Assert
        assertNotNull(popularBooks);
        assertTrue(popularBooks.isEmpty());
    }

    @Test
    @DisplayName("获取用户借阅统计")
    public void testGetUserBorrowingStats() {
        // Arrange
        Object[] user1 = {1L, "张三", 5L};
        Object[] user2 = {2L, "李四", 3L};
        List<Object[]> results = Arrays.asList(user1, user2);
        when(borrowingRepository.getUserBorrowingStats()).thenReturn(results);

        // Act
        List<Map<String, Object>> stats = borrowingService.getUserBorrowingStats();

        // Assert
        assertNotNull(stats);
        assertEquals(2, stats.size());
        assertEquals(1L, stats.get(0).get("userId"));
        assertEquals("张三", stats.get(0).get("username"));
        assertEquals(5L, stats.get(0).get("borrowCount"));
        verify(borrowingRepository, times(1)).getUserBorrowingStats();
    }

    @Test
    @DisplayName("无用户借阅统计")
    public void testGetUserBorrowingStatsEmpty() {
        // Arrange
        when(borrowingRepository.getUserBorrowingStats()).thenReturn(Arrays.asList());

        // Act
        List<Map<String, Object>> stats = borrowingService.getUserBorrowingStats();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
}
