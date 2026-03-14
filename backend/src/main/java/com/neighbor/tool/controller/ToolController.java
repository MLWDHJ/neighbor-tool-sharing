package com.neighbor.tool.controller;

import com.neighbor.tool.model.dto.PublishToolRequest;
import com.neighbor.tool.model.vo.ToolVO;
import com.neighbor.tool.service.CollectionService;
import com.neighbor.tool.service.SearchHistoryService;
import com.neighbor.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具控制器
 * 处理工具相关请求
 */
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {
    
    private final ToolService toolService;
    private final CollectionService collectionService;
    private final SearchHistoryService searchHistoryService;
    
    /**
     * 发布工具
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> publishTool(
            Authentication authentication,
            @Valid @RequestBody PublishToolRequest request) {
        
        Long userId = (Long) authentication.getPrincipal();
        
        ToolVO tool = toolService.publishTool(
                userId,
                request.getName(),
                request.getCategory(),
                request.getImages(),
                request.getPrice(),
                request.getCondition(),
                request.getIsFree(),
                request.getRentFee(),
                request.getDeposit(),
                request.getMaxDays(),
                request.getDescription()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tool", tool);
        response.put("message", "工具发布成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 查询工具列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getToolList(
            Authentication authentication,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) Double distance,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // 如果有关键词搜索，保存搜索历史
        if (keyword != null && !keyword.trim().isEmpty() && authentication != null) {
            Long userId = (Long) authentication.getPrincipal();
            searchHistoryService.saveSearchHistory(userId, keyword);
        }
        
        List<ToolVO> tools = toolService.getToolList(
                latitude, longitude, distance, category, isFree, status, keyword, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tools", tools);
        response.put("total", tools.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取搜索历史
     */
    @GetMapping("/search-history")
    public ResponseEntity<Map<String, Object>> getSearchHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<String> history = searchHistoryService.getUserSearchHistory(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("history", history);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 清空搜索历史
     */
    @DeleteMapping("/search-history")
    public ResponseEntity<Map<String, Object>> clearSearchHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        searchHistoryService.clearUserSearchHistory(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "搜索历史已清空");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取热门搜索
     */
    @GetMapping("/hot-search")
    public ResponseEntity<Map<String, Object>> getHotSearch() {
        List<String> hotKeywords = searchHistoryService.getHotSearchKeywords(10);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("keywords", hotKeywords);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取收藏列表
     */
    @GetMapping("/collections")
    public ResponseEntity<Map<String, Object>> getCollections(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ToolVO> tools = collectionService.getCollections(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tools", tools);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 查询工具详情
     */
    @GetMapping("/{toolId}")
    public ResponseEntity<Map<String, Object>> getToolDetail(
            @PathVariable Long toolId,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude) {
        
        ToolVO tool = toolService.getToolDetail(toolId, latitude, longitude);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tool", tool);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 查询我的工具列表
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyTools(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ToolVO> tools = toolService.getMyTools(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tools", tools);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 编辑工具
     */
    @PutMapping("/{toolId}")
    public ResponseEntity<Map<String, Object>> updateTool(
            Authentication authentication,
            @PathVariable Long toolId,
            @RequestBody Map<String, Object> request) {
        
        Long userId = (Long) authentication.getPrincipal();
        
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String category = (String) request.get("category");
        String condition = (String) request.get("condition");
        Boolean isFree = request.get("isFree") != null ? (Boolean) request.get("isFree") : null;
        Integer maxDays = request.get("maxDays") != null ? 
                Integer.valueOf(request.get("maxDays").toString()) : null;
        BigDecimal price = request.get("price") != null ? 
                new BigDecimal(request.get("price").toString()) : null;
        BigDecimal rentFee = request.get("rentFee") != null ? 
                new BigDecimal(request.get("rentFee").toString()) : null;
        BigDecimal deposit = request.get("deposit") != null ? 
                new BigDecimal(request.get("deposit").toString()) : null;
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) request.get("images");
        
        ToolVO tool = toolService.updateTool(toolId, userId, name, category, images, price,
                condition, isFree, rentFee, deposit, maxDays, description);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tool", tool);
        response.put("message", "工具编辑成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 工具上下架
     */
    @PutMapping("/{toolId}/status")
    public ResponseEntity<Map<String, Object>> updateToolStatus(
            Authentication authentication,
            @PathVariable Long toolId,
            @RequestBody Map<String, String> request) {
        
        Long userId = (Long) authentication.getPrincipal();
        String status = request.get("status");
        
        if (status == null || (!status.equals("available") && !status.equals("offline"))) {
            throw new RuntimeException("状态参数不正确");
        }
        
        toolService.updateToolStatus(toolId, userId, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "状态更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除工具
     */
    @DeleteMapping("/{toolId}")
    public ResponseEntity<Map<String, Object>> deleteTool(
            Authentication authentication,
            @PathVariable Long toolId) {
        
        Long userId = (Long) authentication.getPrincipal();
        toolService.deleteTool(toolId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "工具删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 收藏/取消收藏工具
     */
    @PostMapping("/{toolId}/collect")
    public ResponseEntity<Map<String, Object>> toggleCollection(
            Authentication authentication,
            @PathVariable Long toolId) {
        Long userId = (Long) authentication.getPrincipal();
        collectionService.toggleCollection(userId, toolId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作成功");
        return ResponseEntity.ok(response);
    }
}
