package com.neighbor.tool.controller;

import com.neighbor.tool.model.entity.CreditLog;
import com.neighbor.tool.model.vo.UserVO;
import com.neighbor.tool.service.CreditService;
import com.neighbor.tool.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户信息相关请求
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final CreditService creditService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserVO user = userService.getUserById(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        
        Long userId = (Long) authentication.getPrincipal();
        
        String nickname = (String) request.get("nickname");
        String avatar = (String) request.get("avatar");
        String location = (String) request.get("location");
        BigDecimal latitude = request.get("latitude") != null ? 
                new BigDecimal(request.get("latitude").toString()) : null;
        BigDecimal longitude = request.get("longitude") != null ? 
                new BigDecimal(request.get("longitude").toString()) : null;
        
        UserVO user = userService.updateUserProfile(userId, nickname, avatar, 
                location, latitude, longitude);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        response.put("message", "更新成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取指定用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        UserVO user = userService.getUserById(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 实名认证
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyIdentity(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        
        Long userId = (Long) authentication.getPrincipal();
        String idCardFront = request.get("idCardFront");
        String idCardBack = request.get("idCardBack");
        
        if (idCardFront == null || idCardBack == null) {
            throw new RuntimeException("请上传身份证正反面照片");
        }
        
        userService.verifyIdentity(userId, idCardFront, idCardBack);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "实名认证提交成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取信用历史记录
     */
    @GetMapping("/credit-history")
    public ResponseEntity<Map<String, Object>> getCreditHistory(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<CreditLog> history = creditService.getCreditHistory(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("history", history);
        
        return ResponseEntity.ok(response);
    }
}
