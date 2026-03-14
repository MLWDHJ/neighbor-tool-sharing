package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.SearchHistory;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.SearchHistoryRepository;
import com.neighbor.tool.repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 搜索历史服务属性测试
 * Feature: neighbor-tool-sharing
 */
@SpringBootTest
class SearchHistoryServicePropertyTest {
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        searchHistoryRepository.delete(null);
        
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
     * Property 58: 搜索历史保存
     * Feature: neighbor-tool-sharing, Property 58: 搜索历史保存
     * Validates: Requirements 19.1
     * 
     * 对于任何搜索关键词，系统应该自动保存到用户的搜索历史中
     */
    @Property(tries = 100)
    void searchHistorySave(@ForAll("validKeyword") String keyword) {
        // 保存搜索历史
        searchHistoryService.saveSearchHistory(testUser.getId(), keyword);
        
        // 获取搜索历史
        List<String> history = searchHistoryService.getUserSearchHistory(testUser.getId());
        
        // 验证关键词已保存
        assertThat(history).contains(keyword);
        
        // 清理
        searchHistoryService.clearUserSearchHistory(testUser.getId());
    }
    
    /**
     * Property 59: 搜索历史数量限制
     * Feature: neighbor-tool-sharing, Property 59: 搜索历史数量限制
     * Validates: Requirements 19.2
     * 
     * 对于任何用户，搜索历史最多保存10条，超过后自动删除最旧的记录
     */
    @Property(tries = 100)
    void searchHistoryLimit(@ForAll("keywordList") List<String> keywords) {
        // 保存多条搜索历史
        for (String keyword : keywords) {
            searchHistoryService.saveSearchHistory(testUser.getId(), keyword);
        }
        
        // 获取搜索历史
        List<String> history = searchHistoryService.getUserSearchHistory(testUser.getId());
        
        // 验证数量不超过10条
        assertThat(history.size()).isLessThanOrEqualTo(10);
        
        // 清理
        searchHistoryService.clearUserSearchHistory(testUser.getId());
    }
    
    /**
     * Property 60: 搜索历史清空
     * Feature: neighbor-tool-sharing, Property 60: 搜索历史清空
     * Validates: Requirements 19.4
     * 
     * 对于任何用户，清空搜索历史后，历史记录应该为空
     */
    @Property(tries = 100)
    void searchHistoryClear(@ForAll("keywordList") List<String> keywords) {
        // 保存多条搜索历史
        for (String keyword : keywords) {
            searchHistoryService.saveSearchHistory(testUser.getId(), keyword);
        }
        
        // 清空搜索历史
        searchHistoryService.clearUserSearchHistory(testUser.getId());
        
        // 获取搜索历史
        List<String> history = searchHistoryService.getUserSearchHistory(testUser.getId());
        
        // 验证历史记录为空
        assertThat(history).isEmpty();
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<String> validKeyword() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<List<String>> keywordList() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(20)
                .list()
                .ofMinSize(5)
                .ofMaxSize(15);
    }
}
