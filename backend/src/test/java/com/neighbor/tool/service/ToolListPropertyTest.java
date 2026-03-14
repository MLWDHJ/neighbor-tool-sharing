package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.model.vo.ToolVO;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import com.neighbor.tool.util.LocationUtil;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 工具列表服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class ToolListPropertyTest {
    
    @Autowired
    private ToolService toolService;
    
    @Autowired
    private ToolRepository toolRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private List<Tool> testTools;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        toolRepository.delete(null);
        userRepository.delete(null);
        
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
        
        testTools = new ArrayList<>();
    }
    
    /**
     * Property 9: 工具列表距离排序
     * Feature: neighbor-tool-sharing, Property 9: 工具列表距离排序
     * Validates: Requirements 4.1
     * 
     * 对于任何用户位置和工具列表查询，返回的工具列表应该按照与用户的距离从近到远排序
     */
    @Property(tries = 100)
    void toolListSortedByDistance(
            @ForAll("userLocation") LocationPoint userLocation,
            @ForAll("toolLocations") List<LocationPoint> toolLocations) {
        
        // 清理之前的测试数据
        for (Tool tool : testTools) {
            toolRepository.deleteById(tool.getId());
        }
        testTools.clear();
        
        // 创建测试工具，每个工具在不同位置
        for (int i = 0; i < toolLocations.size() && i < 5; i++) {
            LocationPoint loc = toolLocations.get(i);
            Tool tool = createTestTool("工具" + i, loc.latitude, loc.longitude);
            toolRepository.insert(tool);
            testTools.add(tool);
        }
        
        // 查询工具列表
        List<ToolVO> tools = toolService.getToolList(
                userLocation.latitude, userLocation.longitude,
                null, null, null, "available", null, 1, 20);
        
        // 验证列表按距离排序（从近到远）
        for (int i = 0; i < tools.size() - 1; i++) {
            assertThat(tools.get(i).getDistance())
                    .isLessThanOrEqualTo(tools.get(i + 1).getDistance());
        }
    }
    
    /**
     * Property 10: 工具列表数据完整性
     * Feature: neighbor-tool-sharing, Property 10: 工具列表数据完整性
     * Validates: Requirements 4.2
     * 
     * 对于任何工具列表查询返回的每个工具，应该包含图片、名称、距离、价格、状态和出借人信用评分字段
     */
    @Property(tries = 100)
    void toolListDataCompleteness(
            @ForAll("userLocation") LocationPoint userLocation) {
        
        // 清理之前的测试数据
        for (Tool tool : testTools) {
            toolRepository.deleteById(tool.getId());
        }
        testTools.clear();
        
        // 创建一个测试工具
        Tool tool = createTestTool("测试工具", 
                new BigDecimal("39.9100"), new BigDecimal("116.4100"));
        toolRepository.insert(tool);
        testTools.add(tool);
        
        // 查询工具列表
        List<ToolVO> tools = toolService.getToolList(
                userLocation.latitude, userLocation.longitude,
                null, null, null, null, null, 1, 20);
        
        // 验证每个工具都包含必要字段
        for (ToolVO toolVO : tools) {
            assertThat(toolVO.getName()).isNotNull().isNotEmpty();
            assertThat(toolVO.getImages()).isNotNull().isNotEmpty();
            assertThat(toolVO.getDistance()).isNotNull();
            assertThat(toolVO.getPrice()).isNotNull();
            assertThat(toolVO.getStatus()).isNotNull().isNotEmpty();
            assertThat(toolVO.getLender()).isNotNull();
        }
    }
    
    /**
     * Property 11: 工具筛选正确性
     * Feature: neighbor-tool-sharing, Property 11: 工具筛选正确性
     * Validates: Requirements 4.3
     * 
     * 对于任何筛选条件(距离、分类、是否免费、是否可借用)，
     * 返回的工具列表中的所有工具都应该满足所有筛选条件
     */
    @Property(tries = 100)
    void toolListFilterCorrectness(
            @ForAll("userLocation") LocationPoint userLocation,
            @ForAll("validCategory") String category,
            @ForAll boolean isFree) {
        
        // 清理之前的测试数据
        for (Tool tool : testTools) {
            toolRepository.deleteById(tool.getId());
        }
        testTools.clear();
        
        // 创建符合条件的工具
        Tool matchingTool = createTestTool("匹配工具", 
                new BigDecimal("39.9100"), new BigDecimal("116.4100"));
        matchingTool.setCategory(category);
        matchingTool.setIsFree(isFree);
        matchingTool.setStatus("available");
        toolRepository.insert(matchingTool);
        testTools.add(matchingTool);
        
        // 创建不符合条件的工具
        Tool nonMatchingTool = createTestTool("不匹配工具", 
                new BigDecimal("39.9200"), new BigDecimal("116.4200"));
        nonMatchingTool.setCategory(category.equals("electric") ? "manual" : "electric");
        nonMatchingTool.setIsFree(!isFree);
        nonMatchingTool.setStatus("borrowed");
        toolRepository.insert(nonMatchingTool);
        testTools.add(nonMatchingTool);
        
        // 使用筛选条件查询
        List<ToolVO> tools = toolService.getToolList(
                userLocation.latitude, userLocation.longitude,
                null, category, isFree, "available", null, 1, 20);
        
        // 验证所有返回的工具都符合筛选条件
        for (ToolVO tool : tools) {
            assertThat(tool.getCategory()).isEqualTo(category);
            assertThat(tool.getIsFree()).isEqualTo(isFree);
            assertThat(tool.getStatus()).isEqualTo("available");
        }
    }
    
    /**
     * Property 12: 工具搜索正确性
     * Feature: neighbor-tool-sharing, Property 12: 工具搜索正确性
     * Validates: Requirements 4.4
     * 
     * 对于任何搜索关键词，返回的工具列表中的所有工具的名称或描述都应该包含该关键词(不区分大小写)
     */
    @Property(tries = 100)
    void toolSearchCorrectness(
            @ForAll("userLocation") LocationPoint userLocation,
            @ForAll("searchKeyword") String keyword) {
        
        // 清理之前的测试数据
        for (Tool tool : testTools) {
            toolRepository.deleteById(tool.getId());
        }
        testTools.clear();
        
        // 创建包含关键词的工具
        Tool matchingTool1 = createTestTool(keyword + "工具", 
                new BigDecimal("39.9100"), new BigDecimal("116.4100"));
        matchingTool1.setDescription("这是一个测试工具");
        toolRepository.insert(matchingTool1);
        testTools.add(matchingTool1);
        
        Tool matchingTool2 = createTestTool("测试工具", 
                new BigDecimal("39.9150"), new BigDecimal("116.4150"));
        matchingTool2.setDescription("描述中包含" + keyword);
        toolRepository.insert(matchingTool2);
        testTools.add(matchingTool2);
        
        // 创建不包含关键词的工具
        Tool nonMatchingTool = createTestTool("其他工具", 
                new BigDecimal("39.9200"), new BigDecimal("116.4200"));
        nonMatchingTool.setDescription("完全不相关的描述");
        toolRepository.insert(nonMatchingTool);
        testTools.add(nonMatchingTool);
        
        // 使用关键词搜索
        List<ToolVO> tools = toolService.getToolList(
                userLocation.latitude, userLocation.longitude,
                null, null, null, null, keyword, 1, 20);
        
        // 验证所有返回的工具名称或描述都包含关键词（不区分大小写）
        String lowerKeyword = keyword.toLowerCase();
        for (ToolVO tool : tools) {
            boolean nameContains = tool.getName().toLowerCase().contains(lowerKeyword);
            boolean descContains = tool.getDescription() != null && 
                    tool.getDescription().toLowerCase().contains(lowerKeyword);
            assertThat(nameContains || descContains).isTrue();
        }
    }
    
    // ========== Helper Methods ==========
    
    private Tool createTestTool(String name, BigDecimal latitude, BigDecimal longitude) {
        Tool tool = new Tool();
        tool.setUserId(testUser.getId());
        tool.setName(name);
        tool.setCategory("electric");
        tool.setImages("[\"http://example.com/image1.jpg\"]");
        tool.setPrice(new BigDecimal("100.00"));
        tool.setCondition("good");
        tool.setIsFree(true);
        tool.setRentFee(BigDecimal.ZERO);
        tool.setDeposit(new BigDecimal("50.00"));
        tool.setMaxDays(7);
        tool.setDescription("测试工具描述");
        tool.setStatus("available");
        tool.setLatitude(latitude);
        tool.setLongitude(longitude);
        tool.setBorrowCount(0);
        return tool;
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<LocationPoint> userLocation() {
        return Arbitraries.doubles()
                .between(39.8, 40.0)
                .flatMap(lat -> Arbitraries.doubles()
                        .between(116.3, 116.5)
                        .map(lon -> new LocationPoint(
                                new BigDecimal(lat.toString()),
                                new BigDecimal(lon.toString())
                        ))
                );
    }
    
    @Provide
    Arbitrary<List<LocationPoint>> toolLocations() {
        return userLocation().list().ofMinSize(2).ofMaxSize(5);
    }
    
    @Provide
    Arbitrary<String> validCategory() {
        return Arbitraries.of("electric", "manual", "outdoor", "digital", "daily");
    }
    
    @Provide
    Arbitrary<String> searchKeyword() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(5);
    }
    
    // ========== Helper Classes ==========
    
    static class LocationPoint {
        final BigDecimal latitude;
        final BigDecimal longitude;
        
        LocationPoint(BigDecimal latitude, BigDecimal longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
