package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.entity.Message;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 借用服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class BorrowServicePropertyTest {
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private ToolRepository toolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private com.neighbor.tool.repository.MessageRepository messageRepository;
    
    private User lender;
    private User borrower;
    private Tool tool;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        messageRepository.delete(null);
        borrowRepository.delete(null);
        toolRepository.delete(null);
        
        // 创建出借人
        lender = new User();
        lender.setPhone("13800138001");
        lender.setNickname("出借人");
        lender.setAvatar("http://example.com/avatar1.jpg");
        lender.setLocation("位置1");
        lender.setLatitude(new BigDecimal("39.9042"));
        lender.setLongitude(new BigDecimal("116.4074"));
        lender.setCreditScore(80);
        lender.setIsVerified(false);
        lender.setStatus("active");
        userRepository.insert(lender);
        
        // 创建借用人
        borrower = new User();
        borrower.setPhone("13800138002");
        borrower.setNickname("借用人");
        borrower.setAvatar("http://example.com/avatar2.jpg");
        borrower.setLocation("位置2");
        borrower.setLatitude(new BigDecimal("39.9042"));
        borrower.setLongitude(new BigDecimal("116.4074"));
        borrower.setCreditScore(80);
        borrower.setIsVerified(false);
        borrower.setStatus("active");
        userRepository.insert(borrower);
        
        // 创建工具
        tool = new Tool();
        tool.setUserId(lender.getId());
        tool.setName("测试工具");
        tool.setCategory("electric");
        tool.setImages("[\"http://example.com/tool.jpg\"]");
        tool.setPrice(new BigDecimal("100.00"));
        tool.setCondition("good");
        tool.setIsFree(false);
        tool.setRentFee(new BigDecimal("10.00"));
        tool.setDeposit(new BigDecimal("50.00"));
        tool.setMaxDays(7);
        tool.setDescription("测试工具描述");
        tool.setStatus("available");
        tool.setLatitude(new BigDecimal("39.9042"));
        tool.setLongitude(new BigDecimal("116.4074"));
        tool.setBorrowCount(0);
        tool.setViewCount(0);
        toolRepository.insert(tool);
    }
    
    /**
     * Property 16: 借用日期验证
     * Feature: neighbor-tool-sharing, Property 16: 借用日期验证
     * Validates: Requirements 6.2
     * 
     * 对于任何借用申请，开始日期不能早于今天，归还日期不能早于开始日期
     */
    @Property(tries = 100)
    void borrowDateValidation(
            @ForAll("pastDate") LocalDate pastDate,
            @ForAll("futureStartDate") LocalDate startDate) {
        
        // 测试开始日期早于今天
        assertThatThrownBy(() -> 
            borrowService.createBorrowRequest(borrower.getId(), tool.getId(), 
                    pastDate, pastDate.plusDays(1), "测试")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("开始日期");
        
        // 测试归还日期早于开始日期
        assertThatThrownBy(() -> 
            borrowService.createBorrowRequest(borrower.getId(), tool.getId(), 
                    startDate, startDate.minusDays(1), "测试")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("归还日期");
    }
    
    /**
     * Property 17: 借用费用计算正确性
     * Feature: neighbor-tool-sharing, Property 17: 借用费用计算正确性
     * Validates: Requirements 6.3
     * 
     * 对于任何借用申请，租金 = 日租金 × 借用天数
     */
    @Property(tries = 100)
    void borrowFeeCalculation(
            @ForAll("validBorrowDays") int days) {
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days - 1);
        
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试");
        
        // 验证天数计算
        assertThat(borrow.getDays()).isEqualTo(days);
        
        // 验证费用计算
        BigDecimal expectedFee = tool.getRentFee().multiply(BigDecimal.valueOf(days));
        assertThat(borrow.getRentAmount()).isEqualByComparingTo(expectedFee);
        
        // 清理
        borrowRepository.deleteById(borrow.getId());
    }
    
    /**
     * Property 18: 借用天数限制验证
     * Feature: neighbor-tool-sharing, Property 18: 借用天数限制验证
     * Validates: Requirements 6.4
     * 
     * 对于任何借用申请，借用天数不能超过工具设置的最长借用天数
     */
    @Property(tries = 100)
    void borrowDaysLimitValidation(
            @ForAll("exceedMaxDays") int exceedDays) {
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(exceedDays - 1);
        
        // 借用天数超过限制，应该抛出异常
        assertThatThrownBy(() -> 
            borrowService.createBorrowRequest(borrower.getId(), tool.getId(), 
                    startDate, endDate, "测试")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("借用天数");
    }
    
    /**
     * Property 19: 借用说明长度验证
     * Feature: neighbor-tool-sharing, Property 19: 借用说明长度验证
     * Validates: Requirements 6.5
     * 
     * 对于任何借用申请，借用说明不能超过100字
     */
    @Property(tries = 100)
    void borrowNoteLengthValidation(
            @ForAll("longNote") String longNote) {
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);
        
        // 借用说明超过100字，应该抛出异常
        assertThatThrownBy(() -> 
            borrowService.createBorrowRequest(borrower.getId(), tool.getId(), 
                    startDate, endDate, longNote)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("借用说明");
    }
    
    /**
     * Property 20: 借用申请创建完整性
     * Feature: neighbor-tool-sharing, Property 20: 借用申请创建完整性
     * Validates: Requirements 6.6, 6.7
     * 
     * 对于任何有效的借用申请，系统应该创建借用记录并发送通知给出借人
     */
    @Property(tries = 100)
    void borrowRequestCreationCompleteness(
            @ForAll("validBorrowDays") int days,
            @ForAll("validNote") String note) {
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days - 1);
        
        // 创建借用申请
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, note);
        
        // 验证借用记录创建
        assertThat(borrow).isNotNull();
        assertThat(borrow.getId()).isNotNull();
        assertThat(borrow.getToolId()).isEqualTo(tool.getId());
        assertThat(borrow.getBorrowerId()).isEqualTo(borrower.getId());
        assertThat(borrow.getLenderId()).isEqualTo(lender.getId());
        assertThat(borrow.getStartDate()).isEqualTo(startDate);
        assertThat(borrow.getEndDate()).isEqualTo(endDate);
        assertThat(borrow.getDays()).isEqualTo(days);
        assertThat(borrow.getNote()).isEqualTo(note);
        assertThat(borrow.getStatus()).isEqualTo("pending");
        assertThat(borrow.getDepositAmount()).isEqualByComparingTo(tool.getDeposit());
        
        // 验证通知已发送给出借人
        LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(Message::getUserId, lender.getId());
        messageWrapper.eq(Message::getType, "borrow_request");
        messageWrapper.eq(Message::getRelatedId, borrow.getId());
        
        Message message = messageRepository.selectOne(messageWrapper);
        assertThat(message).isNotNull();
        
        // 清理
        messageRepository.deleteById(message.getId());
        borrowRepository.deleteById(borrow.getId());
    }
    
    /**
     * Property 21: 借用申请通知创建
     * Feature: neighbor-tool-sharing, Property 21: 借用申请通知创建
     * Validates: Requirements 7.1
     * 
     * 对于任何新创建的借用申请，系统应该为出借人创建站内消息通知
     */
    @Property(tries = 100)
    void borrowRequestNotificationCreation() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        
        // 创建借用申请
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        
        // 验证通知已创建
        LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(Message::getUserId, lender.getId());
        messageWrapper.eq(Message::getType, "borrow_request");
        messageWrapper.eq(Message::getRelatedId, borrow.getId());
        
        Message message = messageRepository.selectOne(messageWrapper);
        assertThat(message).isNotNull();
        assertThat(message.getTitle()).isNotNull();
        assertThat(message.getContent()).isNotNull();
        assertThat(message.getIsRead()).isFalse();
        
        // 清理
        messageRepository.deleteById(message.getId());
        borrowRepository.deleteById(borrow.getId());
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<LocalDate> pastDate() {
        return Arbitraries.integers()
                .between(1, 365)
                .map(days -> LocalDate.now().minusDays(days));
    }
    
    @Provide
    Arbitrary<LocalDate> futureStartDate() {
        return Arbitraries.integers()
                .between(0, 30)
                .map(days -> LocalDate.now().plusDays(days));
    }
    
    @Provide
    Arbitrary<Integer> validBorrowDays() {
        return Arbitraries.integers().between(1, 7); // 工具最长借用天数为7天
    }
    
    @Provide
    Arbitrary<Integer> exceedMaxDays() {
        return Arbitraries.integers().between(8, 30); // 超过工具最长借用天数
    }
    
    @Provide
    Arbitrary<String> longNote() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(101)
                .ofMaxLength(200);
    }
    
    @Provide
    Arbitrary<String> validNote() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(100);
    }
}
