package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.UserRepository;
import com.neighbor.tool.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 * 负责用户登录、注册和Token管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOGIN_LOCK_MINUTES = 30;
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String phone, String code) {
        // 检查登录失败次数
        String failKey = LOGIN_FAIL_PREFIX + phone;
        String failCount = redisTemplate.opsForValue().get(failKey);
        if (failCount != null && Integer.parseInt(failCount) >= MAX_LOGIN_ATTEMPTS) {
            throw new RuntimeException("登录失败次数过多，账号已锁定30分钟");
        }
        
        // 验证验证码
        if (!smsService.verifyCode(phone, code)) {
            // 记录失败次数
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 查询或创建用户
        User user = getOrCreateUser(phone);
        
        // 清除登录失败记录
        redisTemplate.delete(failKey);
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId());
        
        // 返回登录结果，包含用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("isFirstLogin", isFirstLogin(user));
        
        // 返回用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("location", user.getLocation());
        userInfo.put("creditScore", user.getCreditScore());
        userInfo.put("isVerified", user.getIsVerified());
        result.put("user", userInfo);
        
        return result;
    }
    
    /**
     * 判断是否首次登录
     */
    private boolean isFirstLogin(User user) {
        // 如果昵称是默认生成的（"用户" + 手机号后4位），则认为是首次登录
        String defaultNickname = "用户" + user.getPhone().substring(7);
        return user.getNickname().equals(defaultNickname) && user.getAvatar() == null;
    }
    
    /**
     * 获取或创建用户
     */
    private User getOrCreateUser(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userRepository.selectOne(wrapper);
        
        if (user == null) {
            // 首次登录，创建新用户
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(7)); // 默认昵称
            user.setCreditScore(80); // 初始信用评分80分
            user.setIsVerified(false);
            user.setStatus("active");
            userRepository.insert(user);
            log.info("创建新用户: {}", phone);
        }
        
        return user;
    }
}
