package com.neighbor.tool.controller;

import com.neighbor.tool.model.dto.LoginRequest;
import com.neighbor.tool.model.dto.SendCodeRequest;
import com.neighbor.tool.service.AuthService;
import com.neighbor.tool.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录和验证码相关请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final SmsService smsService;
    private final AuthService authService;
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@Valid @RequestBody SendCodeRequest request) {
        smsService.sendVerificationCode(request.getPhone());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "验证码已发送");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> loginResult = authService.login(request.getPhone(), request.getCode());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", loginResult.get("token"));
        response.put("userId", loginResult.get("userId"));
        response.put("isFirstLogin", loginResult.get("isFirstLogin"));
        response.put("user", loginResult.get("user"));
        response.put("message", "登录成功");
        
        return ResponseEntity.ok(response);
    }
}
