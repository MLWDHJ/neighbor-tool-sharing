package com.neighbor.tool.controller;

import com.neighbor.tool.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 * 处理数据统计相关请求
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * 获取用户统计数据
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserStatistics(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> statistics = statisticsService.getUserStatistics(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", statistics);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取工具统计数据
     */
    @GetMapping("/tool/{toolId}")
    public ResponseEntity<Map<String, Object>> getToolStatistics(@PathVariable Long toolId) {
        Map<String, Object> statistics = statisticsService.getToolStatistics(toolId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", statistics);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户月度收益统计
     */
    @GetMapping("/user/income")
    public ResponseEntity<Map<String, Object>> getUserMonthlyIncome(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> income = statisticsService.getUserMonthlyIncome(userId, year, month);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("income", income);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取平台统计数据（管理员）
     */
    @GetMapping("/platform")
    public ResponseEntity<Map<String, Object>> getPlatformStatistics() {
        Map<String, Object> statistics = statisticsService.getPlatformStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", statistics);
        
        return ResponseEntity.ok(response);
    }
}
