package com.neighbor.tool.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 * 提供缓存的增删改查操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存前缀
    private static final String TOOL_LIST_KEY = "tool:list:";
    private static final String TOOL_DETAIL_KEY = "tool:detail:";
    private static final String USER_INFO_KEY = "user:info:";
    private static final String HOT_SEARCH_KEY = "search:hot";
    private static final String USER_STATS_KEY = "user:stats:";

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 删除匹配的缓存
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败: pattern={}", pattern, e);
        }
    }

    /**
     * 缓存工具详情
     */
    public void cacheToolDetail(Long toolId, Object toolVO) {
        set(TOOL_DETAIL_KEY + toolId, toolVO, 10, TimeUnit.MINUTES);
    }

    /**
     * 获取工具详情缓存
     */
    public Object getToolDetailCache(Long toolId) {
        return get(TOOL_DETAIL_KEY + toolId);
    }

    /**
     * 清除工具详情缓存
     */
    public void evictToolDetailCache(Long toolId) {
        delete(TOOL_DETAIL_KEY + toolId);
    }

    /**
     * 清除所有工具列表缓存
     */
    public void evictToolListCache() {
        deleteByPattern(TOOL_LIST_KEY + "*");
    }

    /**
     * 缓存用户信息
     */
    public void cacheUserInfo(Long userId, Object userVO) {
        set(USER_INFO_KEY + userId, userVO, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取用户信息缓存
     */
    public Object getUserInfoCache(Long userId) {
        return get(USER_INFO_KEY + userId);
    }

    /**
     * 清除用户信息缓存
     */
    public void evictUserInfoCache(Long userId) {
        delete(USER_INFO_KEY + userId);
    }

    /**
     * 缓存用户统计数据
     */
    public void cacheUserStats(Long userId, Object stats) {
        set(USER_STATS_KEY + userId, stats, 5, TimeUnit.MINUTES);
    }

    /**
     * 获取用户统计缓存
     */
    public Object getUserStatsCache(Long userId) {
        return get(USER_STATS_KEY + userId);
    }

    /**
     * 清除用户统计缓存
     */
    public void evictUserStatsCache(Long userId) {
        delete(USER_STATS_KEY + userId);
    }

    /**
     * 增加热门搜索计数
     */
    public void incrementHotSearch(String keyword) {
        try {
            redisTemplate.opsForZSet().incrementScore(HOT_SEARCH_KEY, keyword, 1);
        } catch (Exception e) {
            log.error("增加热门搜索计数失败: keyword={}", keyword, e);
        }
    }

    /**
     * 获取热门搜索
     */
    public Set<Object> getHotSearchKeywords(int limit) {
        try {
            return redisTemplate.opsForZSet().reverseRange(HOT_SEARCH_KEY, 0, limit - 1);
        } catch (Exception e) {
            log.error("获取热门搜索失败", e);
            return null;
        }
    }
}
