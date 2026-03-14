package com.neighbor.tool.controller;

import com.neighbor.tool.model.vo.MessageVO;
import com.neighbor.tool.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMessages(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        List<MessageVO> messages = messageService.getUserMessages(userId, page, size);
        int unreadCount = messageService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("messages", messages);
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        int unreadCount = messageService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", unreadCount);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            Authentication authentication,
            @PathVariable Long messageId) {
        Long userId = (Long) authentication.getPrincipal();
        messageService.markAsRead(messageId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已标记为已读");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        messageService.markAllAsRead(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "全部已读");
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            Authentication authentication,
            @PathVariable Long messageId) {
        Long userId = (Long) authentication.getPrincipal();
        messageService.deleteMessage(messageId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "消息已删除");
        return ResponseEntity.ok(response);
    }
}
