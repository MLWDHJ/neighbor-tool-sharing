package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Message;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.MessageRepository;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 借用归还流程属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class BorrowReturnPropertyTest {
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private ToolRepository toolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    private User lender;
    private User borrower;
    private Tool tool;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        messageRepository.delete(null);
        borrowRepository.delete(null);
        toolRepository.delete(null);
        userRepository.delete(null);
        
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
     * Property 25: 归还通知创建
     * Feature: neighbor-tool-sharing, Property 25: 归还通知创建
     * Validates: Requirements 8.2
     * 
     * 对于任何借用人标记为"已归还"的借用记录，系统应该为出借人创建归还确认通知
     */
    @Property(tries = 100)
    void returnNotificationCreation() {
        // 创建并同意借用申请
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        borrowService.approveBorrowRequest(borrow.getId(), lender.getId());
        
        // 清理之前的通知
        messageRepository.delete(null);
        
        // 标记已归还
        borrowService.markAsReturned(borrow.getId(), borrower.getId());
        
        // 验证通知已创建
        LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(Message::getUserId, lender.getId());
        messageWrapper.eq(Message::getType, "return_reminder");
        Message message = messageRepository.selectOne(messageWrapper);
        assertThat(message).isNotNull();
        
        // 清理
        messageRepository.delete(null);
        borrowRepository.deleteById(borrow.getId());
        tool.setStatus("available");
        toolRepository.updateById(tool);
    }
    
    /**
     * Property 26: 归还确认状态更新
     * Feature: neighbor-tool-sharing, Property 26: 归还确认状态更新
     * Validates: Requirements 8.3
     * 
     * 对于任何出借人确认归还的借用记录，状态应该更新为"已归还"
     */
    @Property(tries = 100)
    void returnConfirmationStatusUpdate(@ForAll boolean isDamaged) {
        // 创建并同意借用申请
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        borrowService.approveBorrowRequest(borrow.getId(), lender.getId());
        
        // 标记已归还
        borrowService.markAsReturned(borrow.getId(), borrower.getId());
        
        // 确认归还
        borrowService.confirmReturn(borrow.getId(), lender.getId(), isDamaged, 
                isDamaged ? "工具损坏" : null, 
                isDamaged ? new BigDecimal("10.00") : null);
        
        // 验证状态更新为已完成
        Borrow updatedBorrow = borrowRepository.selectById(borrow.getId());
        assertThat(updatedBorrow.getStatus()).isEqualTo("completed");
        assertThat(updatedBorrow.getIsDamaged()).isEqualTo(isDamaged);
        
        // 清理
        messageRepository.delete(null);
        borrowRepository.deleteById(borrow.getId());
        tool.setStatus("available");
        toolRepository.updateById(tool);
    }
    
    /**
     * Property 27: 归还后工具状态恢复
     * Feature: neighbor-tool-sharing, Property 27: 归还后工具状态恢复
     * Validates: Requirements 8.5
     * 
     * 对于任何归还确认完成的借用记录，对应的工具状态应该更新为"可借用"
     */
    @Property(tries = 100)
    void toolStatusRestoreAfterReturn() {
        // 创建并同意借用申请
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        borrowService.approveBorrowRequest(borrow.getId(), lender.getId());
        
        // 验证工具状态为已借出
        Tool toolBorrowed = toolRepository.selectById(tool.getId());
        assertThat(toolBorrowed.getStatus()).isEqualTo("borrowed");
        
        // 标记已归还并确认
        borrowService.markAsReturned(borrow.getId(), borrower.getId());
        borrowService.confirmReturn(borrow.getId(), lender.getId(), false, null, null);
        
        // 验证工具状态恢复为可借用
        Tool toolReturned = toolRepository.selectById(tool.getId());
        assertThat(toolReturned.getStatus()).isEqualTo("available");
        
        // 清理
        messageRepository.delete(null);
        borrowRepository.deleteById(borrow.getId());
    }
}
