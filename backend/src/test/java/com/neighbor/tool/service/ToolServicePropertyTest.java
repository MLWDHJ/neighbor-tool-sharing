package com.neighbor.tool.service;

import com.neighbor.tool.model.dto.PublishToolRequest;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.vo.ToolVO;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 工具服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class ToolServicePropertyTest {
    
    @Autowired
    private ToolService toolService;
    
    @Autowired
    private ToolRepository toolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        toolRepository.delete(null);
        
        // 创建测试用户
        testUser = new User();
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setAvatar("http://example.com/avatar.jpg");
        testUser.setLocation("测试位置");
        testUser.setLatitude(new BigDecimal("39.9042"));
        testUser.setLongitude(new BigDecimal("116.4074"));
        testUser.setCreditScore(80);
        testUser.setIsVerified(false);
        testUser.setStatus("active");
        userRepository.insert(testUser);
    }
    
    /**
     * Property 6: 工具必填项验证
     * Feature: neighbor-tool-sharing, Property 6: 工具必填项验证
     * Validates: Requirements 3.2
     * 
     * 对于任何工具发布请求，如果缺少必填项（名称、分类、图片、价格、新旧程度、押金、最长借用天数），
     * 系统应该拒绝发布并抛出异常
     */
    @Property(tries = 100)
    void toolRequiredFieldsValidation(
            @ForAll("validToolName") String name,
            @ForAll("validCategory") String category,
            @ForAll("validImages") List<String> images,
            @ForAll("validPrice") BigDecimal price,
            @ForAll("validCondition") String condition,
            @ForAll("validDeposit") BigDecimal deposit,
            @ForAll("validMaxDays") Integer maxDays) {
        
        // 测试缺少名称
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), null, category, images, price, 
                    condition, true, BigDecimal.ZERO, deposit, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class);
        
        // 测试缺少分类
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, null, images, price, 
                    condition, true, BigDecimal.ZERO, deposit, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class);
        
        // 测试缺少图片
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, category, null, price, 
                    condition, true, BigDecimal.ZERO, deposit, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class);
        
        // 测试缺少购买价格
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, category, images, null, 
                    condition, true, BigDecimal.ZERO, deposit, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class);
        
        // 测试缺少新旧程度
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, category, images, price, 
                    null, true, BigDecimal.ZERO, deposit, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class);
        
        // 测试缺少押金
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, category, images, price, 
                    condition, true, BigDecimal.ZERO, null, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class);
        
        // 测试缺少最长借用天数
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, category, images, price, 
                    condition, true, BigDecimal.ZERO, deposit, null, "描述")
        ).isInstanceOf(RuntimeException.class);
    }
    
    /**
     * Property 7: 收费工具租金验证
     * Feature: neighbor-tool-sharing, Property 7: 收费工具租金验证
     * Validates: Requirements 3.3
     * 
     * 对于任何收费工具（isFree=false），租金必须大于0，否则系统应该拒绝发布
     */
    @Property(tries = 100)
    void paidToolRentFeeValidation(
            @ForAll("validToolName") String name,
            @ForAll("validCategory") String category,
            @ForAll("validImages") List<String> images,
            @ForAll("validPrice") BigDecimal price,
            @ForAll("validCondition") String condition,
            @ForAll("validDeposit") BigDecimal deposit,
            @ForAll("validMaxDays") Integer maxDays,
            @ForAll("invalidRentFee") BigDecimal invalidRentFee) {
        
        // 收费工具但租金为0或负数，应该抛出异常
        assertThatThrownBy(() -> 
            toolService.publishTool(testUser.getId(), name, category, images, price, 
                    condition, false, invalidRentFee, deposit, maxDays, "描述")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("租金");
    }
    
    /**
     * Property 8: 工具发布初始状态
     * Feature: neighbor-tool-sharing, Property 8: 工具发布初始状态
     * Validates: Requirements 3.5
     * 
     * 对于任何成功发布的工具，其初始状态应该为"available"（可借用），
     * 且位置信息应该与出借人的位置信息一致
     */
    @Property(tries = 100)
    void toolInitialStatusIsAvailable(
            @ForAll("validToolName") String name,
            @ForAll("validCategory") String category,
            @ForAll("validImages") List<String> images,
            @ForAll("validPrice") BigDecimal price,
            @ForAll("validCondition") String condition,
            @ForAll("validDeposit") BigDecimal deposit,
            @ForAll("validMaxDays") Integer maxDays) {
        
        // 发布工具
        ToolVO tool = toolService.publishTool(testUser.getId(), name, category, images, 
                price, condition, true, BigDecimal.ZERO, deposit, maxDays, "测试描述");
        
        // 验证初始状态为"available"
        assertThat(tool.getStatus()).isEqualTo("available");
        
        // 验证位置信息与出借人一致
        assertThat(tool.getLatitude()).isEqualByComparingTo(testUser.getLatitude());
        assertThat(tool.getLongitude()).isEqualByComparingTo(testUser.getLongitude());
        
        // 清理
        toolRepository.deleteById(tool.getId());
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<String> validToolName() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50);
    }
    
    @Provide
    Arbitrary<String> validCategory() {
        return Arbitraries.of("electric", "manual", "outdoor", "digital", "daily");
    }
    
    @Provide
    Arbitrary<List<String>> validImages() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(10)
                .ofMaxLength(50)
                .map(s -> "http://example.com/" + s + ".jpg")
                .list()
                .ofMinSize(1)
                .ofMaxSize(3);
    }
    
    @Provide
    Arbitrary<BigDecimal> validPrice() {
        return Arbitraries.doubles()
                .between(1.0, 10000.0)
                .map(BigDecimal::valueOf);
    }
    
    @Provide
    Arbitrary<String> validCondition() {
        return Arbitraries.of("new", "like_new", "good", "fair");
    }
    
    @Provide
    Arbitrary<BigDecimal> validDeposit() {
        return Arbitraries.doubles()
                .between(0.0, 5000.0)
                .map(BigDecimal::valueOf);
    }
    
    @Provide
    Arbitrary<Integer> validMaxDays() {
        return Arbitraries.integers().between(1, 30);
    }
    
    @Provide
    Arbitrary<BigDecimal> invalidRentFee() {
        return Arbitraries.doubles()
                .between(-100.0, 0.0)
                .map(BigDecimal::valueOf);
    }
}
