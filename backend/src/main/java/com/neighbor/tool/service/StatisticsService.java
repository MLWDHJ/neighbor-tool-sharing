package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务
 * 负责用户和平台的数据统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final UserRepository userRepository;
    private final ToolRepository toolRepository;
    private final BorrowRepository borrowRepository;
    
    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 统计出借次数
        LambdaQueryWrapper<Borrow> lendWrapper = new LambdaQueryWrapper<>();
        lendWrapper.eq(Borrow::getLenderId, userId)
                  .eq(Borrow::getStatus, "returned");
        Long lendCount = borrowRepository.selectCount(lendWrapper);
        
        // 统计借用次数
        LambdaQueryWrapper<Borrow> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(Borrow::getBorrowerId, userId)
                    .eq(Borrow::getStatus, "returned");
        Long borrowCount = borrowRepository.selectCount(borrowWrapper);
        
        // 统计发布的工具数量
        LambdaQueryWrapper<Tool> toolWrapper = new LambdaQueryWrapper<>();
        toolWrapper.eq(Tool::getUserId, userId);
        Long toolCount = toolRepository.selectCount(toolWrapper);
        
        // 统计进行中的借用（我借的）
        LambdaQueryWrapper<Borrow> borrowingWrapper = new LambdaQueryWrapper<>();
        borrowingWrapper.eq(Borrow::getBorrowerId, userId)
                       .eq(Borrow::getStatus, "in_use");
        Long borrowingCount = borrowRepository.selectCount(borrowingWrapper);
        
        // 统计待处理的申请（别人借我的，待审批）
        LambdaQueryWrapper<Borrow> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Borrow::getLenderId, userId)
                     .eq(Borrow::getStatus, "pending");
        Long pendingCount = borrowRepository.selectCount(pendingWrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("creditScore", user.getCreditScore());
        statistics.put("lendCount", lendCount);
        statistics.put("borrowCount", borrowCount);
        statistics.put("toolCount", toolCount);
        statistics.put("borrowingCount", borrowingCount);
        statistics.put("pendingCount", pendingCount);
        
        return statistics;
    }
    
    /**
     * 获取工具借用次数统计
     */
    public Map<String, Object> getToolStatistics(Long toolId) {
        Tool tool = toolRepository.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("工具不存在");
        }
        
        // 统计借用次数
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getToolId, toolId)
               .eq(Borrow::getStatus, "returned");
        Long borrowCount = borrowRepository.selectCount(wrapper);
        
        // 统计进行中的借用
        LambdaQueryWrapper<Borrow> ongoingWrapper = new LambdaQueryWrapper<>();
        ongoingWrapper.eq(Borrow::getToolId, toolId)
                     .eq(Borrow::getStatus, "in_use");
        Long ongoingCount = borrowRepository.selectCount(ongoingWrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("borrowCount", borrowCount);
        statistics.put("ongoingCount", ongoingCount);
        statistics.put("totalViews", tool.getViewCount() != null ? tool.getViewCount() : 0);
        
        return statistics;
    }
    
    /**
     * 获取用户月度收益统计
     */
    public Map<String, Object> getUserMonthlyIncome(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // 查询该月完成的出借记录
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getLenderId, userId)
               .eq(Borrow::getStatus, "returned")
               .ge(Borrow::getEndDate, startDate)
               .le(Borrow::getEndDate, endDate);
        
        List<Borrow> borrows = borrowRepository.selectList(wrapper);
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        int count = 0;
        
        for (Borrow borrow : borrows) {
            if (borrow.getRentAmount() != null) {
                totalIncome = totalIncome.add(borrow.getRentAmount());
                count++;
            }
        }
        
        Map<String, Object> income = new HashMap<>();
        income.put("year", year);
        income.put("month", month);
        income.put("totalIncome", totalIncome);
        income.put("orderCount", count);
        
        return income;
    }
    
    /**
     * 获取平台统计数据（管理员）
     */
    public Map<String, Object> getPlatformStatistics() {
        Long totalUsers = userRepository.selectCount(null);
        Long totalTools = toolRepository.selectCount(null);
        
        LambdaQueryWrapper<Borrow> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(Borrow::getStatus, "returned");
        Long totalBorrows = borrowRepository.selectCount(borrowWrapper);
        
        // 统计活跃工具（状态为可借用）
        LambdaQueryWrapper<Tool> activeToolWrapper = new LambdaQueryWrapper<>();
        activeToolWrapper.eq(Tool::getStatus, "available");
        Long activeTools = toolRepository.selectCount(activeToolWrapper);
        
        // 统计进行中的借用
        LambdaQueryWrapper<Borrow> ongoingWrapper = new LambdaQueryWrapper<>();
        ongoingWrapper.eq(Borrow::getStatus, "in_use");
        Long ongoingBorrows = borrowRepository.selectCount(ongoingWrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("totalTools", totalTools);
        statistics.put("totalBorrows", totalBorrows);
        statistics.put("activeTools", activeTools);
        statistics.put("ongoingBorrows", ongoingBorrows);
        
        return statistics;
    }
}
