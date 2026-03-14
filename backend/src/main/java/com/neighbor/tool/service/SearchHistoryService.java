package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.SearchHistory;
import com.neighbor.tool.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索历史服务
 * 负责搜索历史的保存、查询和管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    
    private final SearchHistoryRepository searchHistoryRepository;
    
    private static final int MAX_HISTORY_COUNT = 10;
    
    /**
     * 保存搜索历史
     */
    @Transactional
    public void saveSearchHistory(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        keyword = keyword.trim();
        
        // 检查是否已存在相同关键词
        LambdaQueryWrapper<SearchHistory> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(SearchHistory::getUserId, userId)
                   .eq(SearchHistory::getKeyword, keyword);
        
        SearchHistory existing = searchHistoryRepository.selectOne(existWrapper);
        if (existing != null) {
            // 如果已存在，删除旧记录
            searchHistoryRepository.deleteById(existing.getId());
        }
        
        // 检查历史记录数量
        LambdaQueryWrapper<SearchHistory> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(SearchHistory::getUserId, userId);
        
        Long count = searchHistoryRepository.selectCount(countWrapper);
        
        // 如果超过最大数量，删除最旧的记录
        if (count >= MAX_HISTORY_COUNT) {
            LambdaQueryWrapper<SearchHistory> oldestWrapper = new LambdaQueryWrapper<>();
            oldestWrapper.eq(SearchHistory::getUserId, userId)
                        .orderByAsc(SearchHistory::getCreatedAt)
                        .last("LIMIT 1");
            
            SearchHistory oldest = searchHistoryRepository.selectOne(oldestWrapper);
            if (oldest != null) {
                searchHistoryRepository.deleteById(oldest.getId());
            }
        }
        
        // 保存新记录
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        
        searchHistoryRepository.insert(history);
        log.info("保存搜索历史: userId={}, keyword={}", userId, keyword);
    }
    
    /**
     * 获取用户搜索历史
     */
    public List<String> getUserSearchHistory(Long userId) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
               .orderByDesc(SearchHistory::getCreatedAt)
               .last("LIMIT " + MAX_HISTORY_COUNT);
        
        List<SearchHistory> histories = searchHistoryRepository.selectList(wrapper);
        
        return histories.stream()
                       .map(SearchHistory::getKeyword)
                       .collect(Collectors.toList());
    }
    
    /**
     * 清空用户搜索历史
     */
    @Transactional
    public void clearUserSearchHistory(Long userId) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        
        searchHistoryRepository.delete(wrapper);
        log.info("清空搜索历史: userId={}", userId);
    }
    
    /**
     * 获取热门搜索关键词
     */
    public List<String> getHotSearchKeywords(int limit) {
        // 统计最近7天的搜索关键词频率
        List<SearchHistory> recentHistories = searchHistoryRepository.selectList(
            new LambdaQueryWrapper<SearchHistory>()
                .ge(SearchHistory::getCreatedAt, 
                    java.time.LocalDateTime.now().minusDays(7))
        );
        
        // 统计关键词频率
        Map<String, Long> keywordCount = new HashMap<>();
        for (SearchHistory history : recentHistories) {
            String keyword = history.getKeyword();
            keywordCount.put(keyword, keywordCount.getOrDefault(keyword, 0L) + 1);
        }
        
        // 按频率排序并返回前N个
        return keywordCount.entrySet().stream()
                          .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                          .limit(limit)
                          .map(Map.Entry::getKey)
                          .collect(Collectors.toList());
    }
}
