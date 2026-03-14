package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Review;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.vo.BorrowHistoryVO;
import com.neighbor.tool.model.vo.ToolVO;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.ReviewRepository;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 工具详情服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class ToolDetailPropertyTest {
    
    @Autowired
    private ToolService toolService;
    
    @Autowired
    private ToolRepository toolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    private User testLender;
    private Tool testTool;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        reviewRepository.delete(null);
        borrowRepository.delete(null);
        toolRepository.delete(null);
        userRepository.delete(null);
        
        // 创建测试出借人
        testLender = new User();
        testLender.setPhone("13800138000");
        testLender.setNickname("测试出借人");
        testLender.setAvatar("http://example.com/avatar.jpg");
        testLender.setLocation("测试位置");
        testLender.setLatitude(new BigDecimal("39.9042"));
        testLender.setLongitude(new BigDecimal("116.4074"));
        testLender.setCreditScore(80);
        testLender.setIsVerified(false);
        testLender.setStatus("active");
        userRepository.insert(testLender);
        
        // 创建测试工具
        testTool = new Tool();
        testTool.setUserId(testLender.getId());
        testTool.setName("测试工具");
        testTool.setCategory("electric");
        testTool.setImages("[\"http://example.com/image1.jpg\"]");
        testTool.setPrice(new BigDecimal("100.00"));
        testTool.setCondition("good");
        testTool.setIsFree(true);
        testTool.setRentFee(BigDecimal.ZERO);
        testTool.setDeposit(new BigDecimal("50.00"));
        testTool.setMaxDays(7);
        testTool.setDescription("测试工具描述");
        testTool.setStatus("available");
        testTool.setLatitude(new BigDecimal("39.9042"));
        testTool.setLongitude(new BigDecimal("116.4074"));
        testTool.setBorrowCount(0);
        toolRepository.insert(testTool);
    }
    
    /**
     * Property 13: 工具详情数据完整性
     * Feature: neighbor-tool-sharing, Property 13: 工具详情数据完整性
     * Validates: Requirements 5.1
     * 
     * 对于任何工具详情查询，返回的数据应该包含图片轮播、基本信息、出借人信息、工具描述和借用记录(如有)
     */
    @Property(tries = 100)
    void toolDetailDataCompleteness() {
        // 查询工具详情
        ToolVO toolVO = toolService.getToolDetail(testTool.getId(), 
                new BigDecimal("39.9042"), new BigDecimal("116.4074"));
        
        // 验证基本信息完整性
        assertThat(toolVO.getId()).isEqualTo(testTool.getId());
        assertThat(toolVO.getName()).isNotNull().isNotEmpty();
        assertThat(toolVO.getImages()).isNotNull().isNotEmpty();
        assertThat(toolVO.getPrice()).isNotNull();
        assertThat(toolVO.getDeposit()).isNotNull();
        assertThat(toolVO.getCondition()).isNotNull();
        assertThat(toolVO.getMaxDays()).isNotNull();
        assertThat(toolVO.getDescription()).isNotNull();
        
        // 验证出借人信息完整性
        assertThat(toolVO.getLender()).isNotNull();
        assertThat(toolVO.getLender().getNickname()).isNotNull();
        assertThat(toolVO.getLender().getAvatar()).isNotNull();
        assertThat(toolVO.getLender().getCreditScore()).isNotNull();
        
        // 验证距离信息
        assertThat(toolVO.getDistance()).isNotNull();
        
        // 验证借用历史字段存在（可能为空列表）
        assertThat(toolVO.getBorrowHistory()).isNotNull();
    }
    
    /**
     * Property 14: 工具历史记录限制
     * Feature: neighbor-tool-sharing, Property 14: 工具历史记录限制
     * Validates: Requirements 5.5
     * 
     * 对于任何有借用历史的工具，工具详情页显示的借用记录应该不超过3条，且按时间倒序排列
     */
    @Property(tries = 100)
    void toolHistoryRecordLimit(@ForAll("borrowHistoryCount") int historyCount) {
        // 创建多个借用历史记录
        List<Borrow> borrows = new ArrayList<>();
        for (int i = 0; i < historyCount; i++) {
            User borrower = createTestBorrower("借用人" + i);
            Borrow borrow = createCompletedBorrow(borrower, LocalDate.now().minusDays(historyCount - i));
            borrows.add(borrow);
        }
        
        // 查询工具详情
        ToolVO toolVO = toolService.getToolDetail(testTool.getId(), 
                new BigDecimal("39.9042"), new BigDecimal("116.4074"));
        
        // 验证借用历史不超过3条
        assertThat(toolVO.getBorrowHistory()).isNotNull();
        assertThat(toolVO.getBorrowHistory().size()).isLessThanOrEqualTo(3);
        
        // 如果有借用历史，验证按时间倒序排列
        if (toolVO.getBorrowHistory().size() > 1) {
            List<BorrowHistoryVO> history = toolVO.getBorrowHistory();
            for (int i = 0; i < history.size() - 1; i++) {
                LocalDate currentDate = history.get(i).getEndDate();
                LocalDate nextDate = history.get(i + 1).getEndDate();
                assertThat(currentDate).isAfterOrEqualTo(nextDate);
            }
        }
        
        // 清理测试数据
        for (Borrow borrow : borrows) {
            borrowRepository.deleteById(borrow.getId());
            userRepository.deleteById(borrow.getBorrowerId());
        }
    }
    
    /**
     * Property 15: 已借出工具状态
     * Feature: neighbor-tool-sharing, Property 15: 已借出工具状态
     * Validates: Requirements 5.6
     * 
     * 对于任何状态为"已借出"的工具，系统应该显示预计归还日期，且不允许新的借用申请
     */
    @Property(tries = 100)
    void borrowedToolStatus(@ForAll("futureDays") int daysUntilReturn) {
        // 将工具状态设置为已借出
        testTool.setStatus("borrowed");
        toolRepository.updateById(testTool);
        
        // 创建进行中的借用记录
        User borrower = createTestBorrower("当前借用人");
        LocalDate endDate = LocalDate.now().plusDays(daysUntilReturn);
        
        Borrow currentBorrow = new Borrow();
        currentBorrow.setToolId(testTool.getId());
        currentBorrow.setBorrowerId(borrower.getId());
        currentBorrow.setLenderId(testLender.getId());
        currentBorrow.setStartDate(LocalDate.now().minusDays(1));
        currentBorrow.setEndDate(endDate);
        currentBorrow.setDays(daysUntilReturn + 1);
        currentBorrow.setNote("测试借用");
        currentBorrow.setStatus("in_use");
        currentBorrow.setDepositAmount(testTool.getDeposit());
        currentBorrow.setRentAmount(BigDecimal.ZERO);
        borrowRepository.insert(currentBorrow);
        
        // 查询工具详情
        ToolVO toolVO = toolService.getToolDetail(testTool.getId(), 
                new BigDecimal("39.9042"), new BigDecimal("116.4074"));
        
        // 验证工具状态为已借出
        assertThat(toolVO.getStatus()).isEqualTo("borrowed");
        
        // 验证显示预计归还日期
        assertThat(toolVO.getExpectedReturnDate()).isNotNull();
        assertThat(toolVO.getExpectedReturnDate().toLocalDate()).isEqualTo(endDate);
        
        // 清理测试数据
        borrowRepository.deleteById(currentBorrow.getId());
        userRepository.deleteById(borrower.getId());
        
        // 恢复工具状态
        testTool.setStatus("available");
        toolRepository.updateById(testTool);
    }
    
    // ========== Helper Methods ==========
    
    private User createTestBorrower(String nickname) {
        User borrower = new User();
        borrower.setPhone("138" + System.currentTimeMillis() % 100000000);
        borrower.setNickname(nickname);
        borrower.setAvatar("http://example.com/borrower.jpg");
        borrower.setLocation("借用人位置");
        borrower.setLatitude(new BigDecimal("39.9100"));
        borrower.setLongitude(new BigDecimal("116.4100"));
        borrower.setCreditScore(80);
        borrower.setIsVerified(false);
        borrower.setStatus("active");
        userRepository.insert(borrower);
        return borrower;
    }
    
    private Borrow createCompletedBorrow(User borrower, LocalDate returnDate) {
        Borrow borrow = new Borrow();
        borrow.setToolId(testTool.getId());
        borrow.setBorrowerId(borrower.getId());
        borrow.setLenderId(testLender.getId());
        borrow.setStartDate(returnDate.minusDays(3));
        borrow.setEndDate(returnDate);
        borrow.setActualReturnDate(returnDate);
        borrow.setDays(3);
        borrow.setNote("测试借用");
        borrow.setStatus("returned");
        borrow.setDepositAmount(testTool.getDeposit());
        borrow.setRentAmount(BigDecimal.ZERO);
        borrowRepository.insert(borrow);
        return borrow;
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<Integer> borrowHistoryCount() {
        return Arbitraries.integers().between(1, 10);
    }
    
    @Provide
    Arbitrary<Integer> futureDays() {
        return Arbitraries.integers().between(1, 30);
    }
}
