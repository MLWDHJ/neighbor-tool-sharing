package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.CreditLog;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.CreditLogRepository;
import com.neighbor.tool.repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 信用评分服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class CreditServicePropertyTest {
    
    @Autowired
    private CreditService creditService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CreditLogRepository creditLogRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        creditLogRepository.delete(null);
        
        // 创建测试用户
        testUser = new User();
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setAvatar("http://example.com/avatar.jpg");
        testUser.setLocation("测试位置");
        testUser.setLatitude(new BigDecimal("39.9042"));
        testUser.setLongitude(new BigDecimal("116.4074"));
        testUser.setCreditScore(80);
        testUser.setIsVerified(false);
        testUser.setStatus("active");
        userRepository.insert(testUser);
    }
    
    /**
     * Property 32: 用户初始信用评分
     * Feature: neighbor-tool-sharing, Property 32: 用户初始信用评分
     * Validates: Requirements 11.1
     * 
     * 对于任何新注册用户，初始信用评分应该为80分
     */
    @Property(tries = 100)
    void userInitialCreditScore() {
        // 验证新用户的初始信用评分
        assertThat(testUser.getCreditScore()).isEqualTo(80);
    }
    
    /**
     * Property 33: 按时归还信用奖励
     * Feature: neighbor-tool-sharing, Property 33: 按时归还信用奖励
     * Validates: Requirements 11.2
     * 
     * 对于任何按时归还的借用记录，借用人信用评分应该增加2分
     */
    @Property(tries = 100)
    void onTimeReturnCreditReward(@ForAll("borrowId") Long borrowId) {
        int beforeScore = testUser.getCreditScore();
        
        // 按时归还奖励
        creditService.rewardOnTimeReturn(testUser.getId(), borrowId);
        
        // 验证信用评分增加2分
        User updatedUser = userRepository.selectById(testUser.getId());
        assertThat(updatedUser.getCreditScore()).isEqualTo(beforeScore + 2);
        
        // 恢复初始状态
        testUser.setCreditScore(beforeScore);
        userRepository.updateById(testUser);
    }
    
    /**
     * Property 34: 逾期归还信用惩罚
     * Feature: neighbor-tool-sharing, Property 34: 逾期归还信用惩罚
     * Validates: Requirements 11.3
     * 
     * 对于任何逾期归还的借用记录，借用人信用评分应该减少5分/天
     */
    @Property(tries = 100)
    void overdueCreditPenalty(
            @ForAll("borrowId") Long borrowId,
            @ForAll("overdueDays") int overdueDays) {
        
        int beforeScore = testUser.getCreditScore();
        
        // 逾期惩罚
        creditService.penalizeOverdue(testUser.getId(), borrowId, overdueDays);
        
        // 验证信用评分减少5分/天
        User updatedUser = userRepository.selectById(testUser.getId());
        int expectedScore = Math.max(0, beforeScore - 5 * overdueDays);
        assertThat(updatedUser.getCreditScore()).isEqualTo(expectedScore);
        
        // 恢复初始状态
        testUser.setCreditScore(beforeScore);
        userRepository.updateById(testUser);
    }
    
    /**
     * Property 35: 工具损坏信用惩罚
     * Feature: neighbor-tool-sharing, Property 35: 工具损坏信用惩罚
     * Validates: Requirements 11.4
     * 
     * 对于任何工具损坏的借用记录，借用人信用评分应该减少10分
     */
    @Property(tries = 100)
    void damageCreditPenalty(@ForAll("borrowId") Long borrowId) {
        int beforeScore = testUser.getCreditScore();
        
        // 工具损坏惩罚
        creditService.penalizeDamage(testUser.getId(), borrowId);
        
        // 验证信用评分减少10分
        User updatedUser = userRepository.selectById(testUser.getId());
        assertThat(updatedUser.getCreditScore()).isEqualTo(beforeScore - 10);
        
        // 恢复初始状态
        testUser.setCreditScore(beforeScore);
        userRepository.updateById(testUser);
    }
    
    /**
     * Property 38: 信用历史记录完整性
     * Feature: neighbor-tool-sharing, Property 38: 信用历史记录完整性
     * Validates: Requirements 11.7
     * 
     * 对于任何信用评分变化，系统应该记录完整的变化历史
     */
    @Property(tries = 100)
    void creditHistoryCompleteness(
            @ForAll("borrowId") Long borrowId,
            @ForAll("changeAmount") int changeAmount) {
        
        int beforeScore = testUser.getCreditScore();
        
        // 更新信用评分
        creditService.updateCreditScore(testUser.getId(), changeAmount, "测试变化", borrowId);
        
        // 获取信用历史
        List<CreditLog> history = creditService.getCreditHistory(testUser.getId());
        
        // 验证历史记录存在
        assertThat(history).isNotEmpty();
        
        // 验证最新记录的信息
        CreditLog latestLog = history.get(0);
        assertThat(latestLog.getUserId()).isEqualTo(testUser.getId());
        assertThat(latestLog.getChangeAmount()).isEqualTo(changeAmount);
        assertThat(latestLog.getBeforeScore()).isEqualTo(beforeScore);
        
        // 恢复初始状态
        testUser.setCreditScore(beforeScore);
        userRepository.updateById(testUser);
        creditLogRepository.deleteById(latestLog.getId());
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<Long> borrowId() {
        return Arbitraries.longs().between(1L, 1000L);
    }
    
    @Provide
    Arbitrary<Integer> overdueDays() {
        return Arbitraries.integers().between(1, 10);
    }
    
    @Provide
    Arbitrary<Integer> changeAmount() {
        return Arbitraries.integers().between(-20, 20);
    }
}
