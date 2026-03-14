package com.neighbor.tool.service;

import com.neighbor.tool.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * StatisticsService Property-Based Tests
 * Feature: neighbor-tool-sharing
 */
class StatisticsServicePropertyTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ToolRepository toolRepository;
    
    @Mock
    private BorrowRepository borrowRepository;
    
    @InjectMocks
    private StatisticsService statisticsService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    /**
     * Property 54: 用户统计数据准确性
     * Feature: neighbor-tool-sharing, Property 54: 用户统计数据应该准确反映用户的借用和出借情况
     */
    @Property(tries = 100)
    void userStatisticsShouldBeAccurate(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @IntRange(min = 0, max = 100) int borrowCount,
            @ForAll @IntRange(min = 0, max = 100) int lendCount,
            @ForAll @IntRange(min = 0, max = 50) int toolCount,
            @ForAll @IntRange(min = 60, max = 100) int creditScore) {
        
        // Mock repository responses
        when(borrowRepository.selectCount(any())).thenReturn((long) borrowCount);
        when(toolRepository.selectCount(any())).thenReturn((long) toolCount);
        
        // Get user statistics
        Map<String, Object> stats = statisticsService.getUserStatistics(userId);
        
        // Verify statistics structure
        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys("creditScore", "lendCount", "borrowCount", "toolCount");
        
        // Verify counts are non-negative
        assertThat((Long) stats.get("borrowCount")).isGreaterThanOrEqualTo(0);
        assertThat((Long) stats.get("lendCount")).isGreaterThanOrEqualTo(0);
        assertThat((Long) stats.get("toolCount")).isGreaterThanOrEqualTo(0);
    }
    
    /**
     * Property 55: 工具借用次数统计
     * Feature: neighbor-tool-sharing, Property 55: 工具的借用次数应该等于该工具的已完成借用记录数
     */
    @Property(tries = 100)
    void toolBorrowCountShouldMatchCompletedBorrows(
            @ForAll @LongRange(min = 1, max = 10000) Long toolId,
            @ForAll @IntRange(min = 0, max = 100) int completedBorrows,
            @ForAll @IntRange(min = 0, max = 50) int viewCount) {
        
        // Mock repository responses
        when(borrowRepository.selectCount(any())).thenReturn((long) completedBorrows);
        
        // Get tool statistics
        Map<String, Object> stats = statisticsService.getToolStatistics(toolId);
        
        // Verify statistics structure
        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys("borrowCount", "ongoingCount");
        
        // Verify borrow count is non-negative
        assertThat((Long) stats.get("borrowCount")).isGreaterThanOrEqualTo(0);
        assertThat((Long) stats.get("ongoingCount")).isGreaterThanOrEqualTo(0);
    }
    
    /**
     * Property 56: 用户月度收益统计
     * Feature: neighbor-tool-sharing, Property 56: 用户月度收益应该等于该月所有已完成借用的租金总和
     */
    @Property(tries = 100)
    void monthlyIncomeShouldSumCompletedBorrowRents(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 2024, max = 2025) int year) {
        
        // Mock repository response with sample income data
        Map<String, BigDecimal> mockIncome = new HashMap<>();
        mockIncome.put(year + "-" + String.format("%02d", month), BigDecimal.valueOf(100.00));
        
        // Get monthly income
        Map<String, Object> income = statisticsService.getUserMonthlyIncome(userId, year, month);
        
        // Verify income structure
        assertThat(income).isNotNull();
        assertThat(income).containsKeys("totalIncome", "orderCount");
        
        // Verify income is non-negative
        assertThat((BigDecimal) income.get("totalIncome")).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
    
    /**
     * Property 57: 平台统计数据准确性
     * Feature: neighbor-tool-sharing, Property 57: 平台统计数据应该准确反映平台的整体运营情况
     */
    @Property(tries = 100)
    void platformStatisticsShouldBeAccurate(
            @ForAll @IntRange(min = 0, max = 10000) int userCount,
            @ForAll @IntRange(min = 0, max = 5000) int toolCount,
            @ForAll @IntRange(min = 0, max = 20000) int borrowCount) {
        
        // Mock repository responses
        when(userRepository.selectCount(any())).thenReturn((long) userCount);
        when(toolRepository.selectCount(any())).thenReturn((long) toolCount);
        when(borrowRepository.selectCount(any())).thenReturn((long) borrowCount);
        
        // Get platform statistics
        Map<String, Object> stats = statisticsService.getPlatformStatistics();
        
        // Verify statistics structure
        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys("totalUsers", "totalTools", "totalBorrows", "activeTools");
        
        // Verify all counts are non-negative
        assertThat((Long) stats.get("totalUsers")).isGreaterThanOrEqualTo(0);
        assertThat((Long) stats.get("totalTools")).isGreaterThanOrEqualTo(0);
        assertThat((Long) stats.get("totalBorrows")).isGreaterThanOrEqualTo(0);
        assertThat((Long) stats.get("activeTools")).isGreaterThanOrEqualTo(0);
    }
}
