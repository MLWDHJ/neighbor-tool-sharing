package com.neighbor.tool.service;

import com.neighbor.tool.repository.VerificationCodeRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SmsService 属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class SmsServicePropertyTest {
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    
    @BeforeEach
    void setUp() {
        // 清理 Redis 测试数据
        redisTemplate.keys("sms:*").forEach(key -> redisTemplate.delete(key));
    }
    
    /**
     * Property 1: 验证码格式正确性
     * 对于任何手机号，当系统发送验证码时，生成的验证码应该是6位数字
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    @Label("Feature: neighbor-tool-sharing, Property 1: 验证码格式正确性")
    void verificationCodeFormatIsCorrect(@ForAll("validPhoneNumbers") String phone) {
        // 发送验证码
        smsService.sendVerificationCode(phone);
        
        // 从 Redis 获取验证码
        String code = redisTemplate.opsForValue().get("sms:code:" + phone);
        
        // 验证码应该是6位数字
        assertThat(code).isNotNull();
        assertThat(code).matches("^\\d{6}$");
        assertThat(code.length()).isEqualTo(6);
    }
    
    /**
     * Property 2: 验证码防刷机制
     * 对于任何手机号，在发送验证码后的60秒内，系统不应允许向同一手机号再次发送验证码
     * Validates: Requirements 1.4
     */
    @Property(tries = 100)
    @Label("Feature: neighbor-tool-sharing, Property 2: 验证码防刷机制")
    void verificationCodeRateLimitWorks(@ForAll("validPhoneNumbers") String phone) {
        // 第一次发送验证码应该成功
        smsService.sendVerificationCode(phone);
        
        // 立即再次发送应该失败
        assertThatThrownBy(() -> smsService.sendVerificationCode(phone))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("发送过于频繁");
        
        // 验证 Redis 中存在防刷限制
        String rateLimitKey = "sms:limit:" + phone;
        assertThat(redisTemplate.hasKey(rateLimitKey)).isTrue();
    }
    
    /**
     * 生成有效的手机号
     */
    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Arbitraries.longs()
            .between(13000000000L, 19999999999L)
            .map(String::valueOf);
    }
}
