package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.VerificationCode;
import com.neighbor.tool.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 * 负责验证码的生成、发送和验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    
    private final VerificationCodeRepository verificationCodeRepository;
    private final StringRedisTemplate redisTemplate;
    
    private static final String CODE_PREFIX = "sms:code:";
    private static final String RATE_LIMIT_PREFIX = "sms:limit:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRATION_SECONDS = 300; // 5分钟
    private static final int RATE_LIMIT_SECONDS = 60; // 60秒内不允许重复发送
    
    /**
     * 发送验证码
     */
    public void sendVerificationCode(String phone) {
        // 检查防刷机制
        String rateLimitKey = RATE_LIMIT_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }
        
        // 生成6位随机验证码
        String code = generateCode();
        
        // 保存到Redis（5分钟过期）
        String codeKey = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRATION_SECONDS, TimeUnit.SECONDS);
        
        // 设置防刷限制（60秒）
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        
        // 保存到数据库
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusSeconds(CODE_EXPIRATION_SECONDS));
        verificationCode.setIsUsed(false);
        verificationCodeRepository.insert(verificationCode);
        
        // TODO: 集成腾讯云短信API发送验证码
        // 开发阶段直接打印到日志
        log.info("发送验证码到手机号 {}: {}", phone, code);
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        // 开发测试：固定验证码123456可直接通过
        if ("123456".equals(code)) {
            log.info("使用测试验证码登录: {}", phone);
            return true;
        }
        
        String codeKey = CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            return false;
        }
        
        if (storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            
            // 标记数据库中的验证码为已使用
            LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VerificationCode::getPhone, phone)
                   .eq(VerificationCode::getCode, code)
                   .eq(VerificationCode::getIsUsed, false)
                   .orderByDesc(VerificationCode::getCreatedAt)
                   .last("LIMIT 1");
            
            VerificationCode verificationCode = verificationCodeRepository.selectOne(wrapper);
            if (verificationCode != null) {
                verificationCode.setIsUsed(true);
                verificationCodeRepository.updateById(verificationCode);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成6位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
