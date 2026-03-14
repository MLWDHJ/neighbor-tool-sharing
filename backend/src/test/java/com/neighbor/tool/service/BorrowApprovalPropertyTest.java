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
 * 借用申请处理属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class BorrowApprovalPropertyTest {
    
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
     * Property 22: 申请详情数据完整性
     * Feature: neighbor-tool-sharing, Property 22: 申请详情数据完整性
     * Validates: Requirements 7.2
     * 
     * 对于任何借用申请详情查询，返回的数据应该包含借用人信息、工具信息、借用时间和费用信息
     */
    @Property(tries = 100)
    void borrowRequestDetailCompleteness() {
        // 创建借用申请
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        
        // 查询借用详情
        Borrow borrowDetail = borrowRepository.selectById(borrow.getId());
        
        // 验证数据完整性
        assertThat(borrowDetail).isNotNull();
        assertThat(borrowDetail.getBorrowerId()).isNotNull();
        assertThat(borrowDetail.getLenderId()).isNotNull();
        assertThat(borrowDetail.getToolId()).isNotNull();
        assertThat(borrowDetail.getStartDate()).isNotNull();
        assertThat(borrowDetail.getEndDate()).isNotNull();
        assertThat(borrowDetail.getDays()).isNotNull();
        assertThat(borrowDetail.getDepositAmount()).isNotNull();
        assertThat(borrowDetail.getRentAmount()).isNotNull();
        assertThat(borrowDetail.getStatus()).isNotNull();
        
        // 清理
        messageRepository.delete(null);
        borrowRepository.deleteById(borrow.getId());
    }
    
    /**
     * Property 23: 借用申请状态转换
     * Feature: neighbor-tool-sharing, Property 23: 借用申请状态转换
     * Validates: Requirements 7.4, 7.5
     * 
     * 对于任何待处理的借用申请，当出借人同意时状态应该变为"已同意"，
     * 当拒绝时状态应该变为"已拒绝"，且应该通知借用人
     */
    @Property(tries = 100)
    void borrowRequestStatusTransition(@ForAll boolean approve) {
        // 创建借用申请
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        
        // 清理创建申请时的通知
        messageRepository.delete(null);
        
        if (approve) {
            // 同意申请
            borrowService.approveBorrowRequest(borrow.getId(), lender.getId());
            
            // 验证状态变为已同意
            Borrow updatedBorrow = borrowRepository.selectById(borrow.getId());
            assertThat(updatedBorrow.getStatus()).isEqualTo("approved");
            
            // 验证通知已发送
            LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(Message::getUserId, borrower.getId());
            messageWrapper.eq(Message::getType, "request_approved");
            Message message = messageRepository.selectOne(messageWrapper);
            assertThat(message).isNotNull();
            
        } else {
            // 拒绝申请
            borrowService.rejectBorrowRequest(borrow.getId(), lender.getId(), "测试拒绝");
            
            // 验证状态变为已拒绝
            Borrow updatedBorrow = borrowRepository.selectById(borrow.getId());
            assertThat(updatedBorrow.getStatus()).isEqualTo("rejected");
            
            // 验证通知已发送
            LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(Message::getUserId, borrower.getId());
            messageWrapper.eq(Message::getType, "request_rejected");
            Message message = messageRepository.selectOne(messageWrapper);
            assertThat(message).isNotNull();
        }
        
        // 清理
        messageRepository.delete(null);
        borrowRepository.deleteById(borrow.getId());
        
        // 恢复工具状态
        tool.setStatus("available");
        toolRepository.updateById(tool);
    }
    
    /**
     * Property 24: 申请同意后工具状态更新
     * Feature: neighbor-tool-sharing, Property 24: 申请同意后工具状态更新
     * Validates: Requirements 7.6
     * 
     * 对于任何被同意的借用申请，对应的工具状态应该更新为"已借出"
     */
    @Property(tries = 100)
    void toolStatusUpdateAfterApproval() {
        // 创建借用申请
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Borrow borrow = borrowService.createBorrowRequest(
                borrower.getId(), tool.getId(), startDate, endDate, "测试借用");
        
        // 验证工具初始状态
        Tool toolBefore = toolRepository.selectById(tool.getId());
        assertThat(toolBefore.getStatus()).isEqualTo("available");
        
        // 同意申请
        borrowService.approveBorrowRequest(borrow.getId(), lender.getId());
        
        // 验证工具状态更新为已借出
        Tool toolAfter = toolRepository.selectById(tool.getId());
        assertThat(toolAfter.getStatus()).isEqualTo("borrowed");
        
        // 清理
        messageRepository.delete(null);
        borrowRepository.deleteById(borrow.getId());
        
        // 恢复工具状态
        tool.setStatus("available");
        toolRepository.updateById(tool);
    }
}
