package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Review;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 评价服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class ReviewServicePropertyTest {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private ToolRepository toolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User lender;
    private User borrower;
    private Tool tool;
    private Borrow completedBorrow;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        reviewRepository.delete(null);
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
        
        // 创建已完成的借用记录
        completedBorrow = new Borrow();
        completedBorrow.setToolId(tool.getId());
        completedBorrow.setBorrowerId(borrower.getId());
        completedBorrow.setLenderId(lender.getId());
        completedBorrow.setStartDate(LocalDate.now().minusDays(5));
        completedBorrow.setEndDate(LocalDate.now().minusDays(1));
        completedBorrow.setActualReturnDate(LocalDate.now().minusDays(1));
        completedBorrow.setDays(4);
        completedBorrow.setNote("测试借用");
        completedBorrow.setStatus("completed");
        completedBorrow.setDepositAmount(new BigDecimal("50.00"));
        completedBorrow.setRentAmount(new BigDecimal("40.00"));
        completedBorrow.setDeductAmount(BigDecimal.ZERO);
        completedBorrow.setIsDamaged(false);
        borrowRepository.insert(completedBorrow);
    }
    
    /**
     * Property 29: 评价评分验证
     * Feature: neighbor-tool-sharing, Property 29: 评价评分验证
     * Validates: Requirements 10.2
     * 
     * 对于任何评价，评分必须在1-5星之间
     */
    @Property(tries = 100)
    void reviewRatingValidation(
            @ForAll("invalidRating") int invalidRating,
            @ForAll("validComment") String comment) {
        
        // 评分不在1-5之间，应该抛出异常
        assertThatThrownBy(() -> 
            reviewService.createReview(borrower.getId(), completedBorrow.getId(), 
                    invalidRating, Arrays.asList("好评"), comment)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("评分");
    }
    
    /**
     * Property 30: 评价内容长度验证
     * Feature: neighbor-tool-sharing, Property 30: 评价内容长度验证
     * Validates: Requirements 10.4
     * 
     * 对于任何评价，评价内容不能超过200字
     */
    @Property(tries = 100)
    void reviewCommentLengthValidation(
            @ForAll("validRating") int rating,
            @ForAll("longComment") String longComment) {
        
        // 评价内容超过200字，应该抛出异常
        assertThatThrownBy(() -> 
            reviewService.createReview(borrower.getId(), completedBorrow.getId(), 
                    rating, Arrays.asList("好评"), longComment)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("评价内容");
    }
    
    /**
     * Property 31: 评价后信用评分更新
     * Feature: neighbor-tool-sharing, Property 31: 评价后信用评分更新
     * Validates: Requirements 10.5, 10.6, 10.7
     * 
     * 对于任何评价，4-5星应该增加被评价人信用评分，1-2星应该减少信用评分
     */
    @Property(tries = 100)
    void reviewCreditScoreUpdate(
            @ForAll("goodRating") int goodRating) {
        
        // 记录评价前的信用评分
        User reviewee = userRepository.selectById(lender.getId());
        int beforeScore = reviewee.getCreditScore();
        
        // 创建好评
        Review review = reviewService.createReview(borrower.getId(), completedBorrow.getId(), 
                goodRating, Arrays.asList("好评"), "非常好");
        
        // 验证信用评分增加
        reviewee = userRepository.selectById(lender.getId());
        assertThat(reviewee.getCreditScore()).isGreaterThan(beforeScore);
        
        // 清理
        reviewRepository.deleteById(review.getId());
        reviewee.setCreditScore(beforeScore);
        userRepository.updateById(reviewee);
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<Integer> invalidRating() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-10, 0),
                Arbitraries.integers().between(6, 10)
        );
    }
    
    @Provide
    Arbitrary<Integer> validRating() {
        return Arbitraries.integers().between(1, 5);
    }
    
    @Provide
    Arbitrary<Integer> goodRating() {
        return Arbitraries.integers().between(4, 5);
    }
    
    @Provide
    Arbitrary<String> validComment() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(100);
    }
    
    @Provide
    Arbitrary<String> longComment() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(201)
                .ofMaxLength(300);
    }
}
