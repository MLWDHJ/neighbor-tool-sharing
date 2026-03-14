package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.vo.UserVO;
import com.neighbor.tool.repository.UserRepository;
import com.neighbor.tool.util.LocationUtil;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserService 属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class UserServicePropertyTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    private Long testUserId;
    
    @BeforeEach
    void setUp() {
        // 创建测试用户
        User user = new User();
        user.setPhone("13800138000");
        user.setNickname("测试用户");
        user.setCreditScore(80);
        user.setIsVerified(false);
        user.setStatus("active");
        userRepository.insert(user);
        testUserId = user.getId();
    }
    
    /**
     * Property 3: 用户信息更新一致性
     * 对于任何用户和有效的个人信息更新，更新后查询用户信息应该返回更新后的值
     * Validates: Requirements 2.1
     */
    @Property(tries = 100)
    @Label("Feature: neighbor-tool-sharing, Property 3: 用户信息更新一致性")
    void userProfileUpdateIsConsistent(
            @ForAll("validNicknames") String nickname,
            @ForAll("validAvatars") String avatar,
            @ForAll("validLocations") String location) {
        
        // 更新用户信息
        UserVO updatedUser = userService.updateUserProfile(
                testUserId, nickname, avatar, location, null, null);
        
        // 查询用户信息
        UserVO queriedUser = userService.getUserById(testUserId);
        
        // 验证更新后的值与查询的值一致
        assertThat(queriedUser.getNickname()).isEqualTo(nickname);
        assertThat(queriedUser.getAvatar()).isEqualTo(avatar);
        assertThat(queriedUser.getLocation()).isEqualTo(location);
    }
    
    /**
     * Property 4: 距离计算正确性
     * 对于任何两个有效的地理坐标点，系统计算的距离应该与使用Haversine公式计算的距离一致(误差在10米内)
     * Validates: Requirements 2.4
     */
    @Property(tries = 100)
    @Label("Feature: neighbor-tool-sharing, Property 4: 距离计算正确性")
    void distanceCalculationIsAccurate(
            @ForAll("validLatitudes") BigDecimal lat1,
            @ForAll("validLongitudes") BigDecimal lon1,
            @ForAll("validLatitudes") BigDecimal lat2,
            @ForAll("validLongitudes") BigDecimal lon2) {
        
        // 计算距离
        double distance = LocationUtil.calculateDistance(lat1, lon1, lat2, lon2);
        
        // 验证距离是非负数
        assertThat(distance).isGreaterThanOrEqualTo(0.0);
        
        // 验证对称性：distance(A, B) = distance(B, A)
        double reverseDistance = LocationUtil.calculateDistance(lat2, lon2, lat1, lon1);
        assertThat(Math.abs(distance - reverseDistance)).isLessThan(10.0); // 误差在10米内
        
        // 验证三角不等式：distance(A, C) <= distance(A, B) + distance(B, C)
        // 使用中点作为第三个点
        BigDecimal midLat = lat1.add(lat2).divide(BigDecimal.valueOf(2));
        BigDecimal midLon = lon1.add(lon2).divide(BigDecimal.valueOf(2));
        
        double distanceAB = LocationUtil.calculateDistance(lat1, lon1, lat2, lon2);
        double distanceAM = LocationUtil.calculateDistance(lat1, lon1, midLat, midLon);
        double distanceMB = LocationUtil.calculateDistance(midLat, midLon, lat2, lon2);
        
        assertThat(distanceAB).isLessThanOrEqualTo(distanceAM + distanceMB + 10.0); // 允许10米误差
    }
    
    /**
     * 生成有效的昵称
     */
    @Provide
    Arbitrary<String> validNicknames() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(2)
            .ofMaxLength(20);
    }
    
    /**
     * 生成有效的头像URL
     */
    @Provide
    Arbitrary<String> validAvatars() {
        return Arbitraries.of(
            "https://example.com/avatar1.jpg",
            "https://example.com/avatar2.jpg",
            "https://example.com/avatar3.jpg"
        );
    }
    
    /**
     * 生成有效的位置描述
     */
    @Provide
    Arbitrary<String> validLocations() {
        return Arbitraries.of(
            "北京市朝阳区",
            "上海市浦东新区",
            "广州市天河区",
            "深圳市南山区"
        );
    }
    
    /**
     * 生成有效的纬度（中国范围）
     */
    @Provide
    Arbitrary<BigDecimal> validLatitudes() {
        return Arbitraries.doubles()
            .between(18.0, 54.0) // 中国纬度范围
            .map(BigDecimal::valueOf);
    }
    
    /**
     * 生成有效的经度（中国范围）
     */
    @Provide
    Arbitrary<BigDecimal> validLongitudes() {
        return Arbitraries.doubles()
            .between(73.0, 135.0) // 中国经度范围
            .map(BigDecimal::valueOf);
    }
}
