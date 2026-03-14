package com.neighbor.tool.controller;

import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借用控制器
 * 处理借用相关请求
 */
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {
    
    private final BorrowService borrowService;
    
    /**
     * 创建借用申请
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBorrowRequest(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        
        Long borrowerId = (Long) authentication.getPrincipal();
        Long toolId = Long.valueOf(request.get("toolId").toString());
        LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(request.get("endDate").toString());
        String note = (String) request.get("note");
        
        Borrow borrow = borrowService.createBorrowRequest(borrowerId, toolId, startDate, endDate, note);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("borrow", borrow);
        response.put("message", "借用申请提交成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 同意借用申请
     */
    @PutMapping("/{borrowId}/approve")
    public ResponseEntity<Map<String, Object>> approveBorrowRequest(
            Authentication authentication,
            @PathVariable Long borrowId) {
        
        Long lenderId = (Long) authentication.getPrincipal();
        borrowService.approveBorrowRequest(borrowId, lenderId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "借用申请已同意");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 拒绝借用申请
     */
    @PutMapping("/{borrowId}/reject")
    public ResponseEntity<Map<String, Object>> rejectBorrowRequest(
            Authentication authentication,
            @PathVariable Long borrowId,
            @RequestBody Map<String, String> request) {
        
        Long lenderId = (Long) authentication.getPrincipal();
        String reason = request.get("reason");
        
        borrowService.rejectBorrowRequest(borrowId, lenderId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "借用申请已拒绝");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 标记已归还
     */
    @PutMapping("/{borrowId}/return")
    public ResponseEntity<Map<String, Object>> markAsReturned(
            Authentication authentication,
            @PathVariable Long borrowId) {
        
        Long borrowerId = (Long) authentication.getPrincipal();
        borrowService.markAsReturned(borrowId, borrowerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已标记归还，等待出借人确认");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 确认归还
     */
    @PutMapping("/{borrowId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReturn(
            Authentication authentication,
            @PathVariable Long borrowId,
            @RequestBody Map<String, Object> request) {
        
        Long lenderId = (Long) authentication.getPrincipal();
        Boolean isDamaged = (Boolean) request.get("isDamaged");
        String damageNote = (String) request.get("damageNote");
        BigDecimal deductAmount = request.get("deductAmount") != null ? 
                new BigDecimal(request.get("deductAmount").toString()) : null;
        
        borrowService.confirmReturn(borrowId, lenderId, isDamaged, damageNote, deductAmount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "归还已确认");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取借用记录详情
     */
    @GetMapping("/{borrowId}")
    public ResponseEntity<Map<String, Object>> getBorrowDetail(
            Authentication authentication,
            @PathVariable Long borrowId) {
        
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> borrowDetail = borrowService.getBorrowDetailVO(borrowId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("borrow", borrowDetail);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取我的借用列表
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyBorrows(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = (Long) authentication.getPrincipal();
        List<Map<String, Object>> borrows = borrowService.getMyBorrows(userId, status, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("borrows", borrows);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取我的出借列表
     */
    @GetMapping("/my-lends")
    public ResponseEntity<Map<String, Object>> getMyLends(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = (Long) authentication.getPrincipal();
        List<Map<String, Object>> lends = borrowService.getMyLends(userId, status, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("lends", lends);
        
        return ResponseEntity.ok(response);
    }
}
