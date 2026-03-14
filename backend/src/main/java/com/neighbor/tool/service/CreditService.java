package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.CreditLog;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.CreditLogRepository;
import com.neighbor.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 信用评分服务
 * 负责用户信用评分的变化和记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {
    
    private final UserRepository userRepository;
    private final CreditLogRepository creditLogRepository;
    
    /**
     * 更新用户信用评分
     */
    @Transactional
    public void updateCreditScore(Long userId, int changeAmount, String reason, Long relatedId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            log.error("用户不存在: userId={}", userId);
            return;
        }
        
        int beforeScore = user.getCreditScore();
        int afterScore = Math.max(0, Math.min(100, beforeScore + changeAmount));
        
        user.setCreditScore(afterScore);
        userRepository.updateById(user);
        
        // 记录信用变化
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeAmount(changeAmount);
        log.setReason(reason);
        log.setRelatedId(relatedId);
        log.setBeforeScore(beforeScore);
        log.setAfterScore(afterScore);
        creditLogRepository.insert(log);
        
        this.log.info("信用评分更新: userId={}, change={}, reason={}, before={}, after={}", 
                userId, changeAmount, reason, beforeScore, afterScore);
    }
    
    /**
     * 按时归还奖励
     */
    public void rewardOnTimeReturn(Long userId, Long borrowId) {
        updateCreditScore(userId, 2, "按时归还", borrowId);
    }
    
    /**
     * 逾期惩罚
     */
    public void penalizeOverdue(Long userId, Long borrowId, int overdueDays) {
        int penalty = -5 * overdueDays;
        updateCreditScore(userId, penalty, "逾期归还", borrowId);
    }
    
    /**
     * 工具损坏惩罚
     */
    public void penalizeDamage(Long userId, Long borrowId) {
        updateCreditScore(userId, -10, "工具损坏", borrowId);
    }
    
    /**
     * 好评奖励
     */
    public void rewardGoodReview(Long userId, Long reviewId) {
        updateCreditScore(userId, 1, "收到好评", reviewId);
    }
    
    /**
     * 差评惩罚
     */
    public void penalizeBadReview(Long userId, Long reviewId) {
        updateCreditScore(userId, -3, "收到差评", reviewId);
    }
    
    /**
     * 获取用户信用历史记录
     */
    public java.util.List<CreditLog> getCreditHistory(Long userId) {
        return creditLogRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CreditLog>()
                .eq(CreditLog::getUserId, userId)
                .orderByDesc(CreditLog::getCreatedAt)
        );
    }
}
