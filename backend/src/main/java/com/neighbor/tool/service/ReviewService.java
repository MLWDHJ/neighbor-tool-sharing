package com.neighbor.tool.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Review;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价服务
 * 负责评价的创建和查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final BorrowRepository borrowRepository;
    private final CreditService creditService;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建评价
     */
    @Transactional
    public Review createReview(Long reviewerId, Long borrowId, Integer rating, 
                              List<String> tags, String comment) {
        // 验证评分
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5星之间");
        }
        
        // 验证评价内容长度
        if (comment != null && comment.length() > 200) {
            throw new RuntimeException("评价内容不能超过200字");
        }
        
        // 验证借用记录
        Borrow borrow = borrowRepository.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        if (!"returned".equals(borrow.getStatus())) {
            throw new RuntimeException("只能评价已完成的借用记录");
        }
        
        // 确定被评价人
        Long revieweeId;
        if (borrow.getBorrowerId().equals(reviewerId)) {
            revieweeId = borrow.getLenderId();
        } else if (borrow.getLenderId().equals(reviewerId)) {
            revieweeId = borrow.getBorrowerId();
        } else {
            throw new RuntimeException("无权评价此借用记录");
        }
        
        // 创建评价
        Review review = new Review();
        review.setBorrowId(borrowId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(rating);
        review.setTags(convertTagsToJson(tags));
        review.setComment(comment);
        
        reviewRepository.insert(review);
        log.info("评价创建成功: reviewId={}, borrowId={}, rating={}", 
                review.getId(), borrowId, rating);
        
        // 更新信用评分
        if (rating >= 4) {
            creditService.rewardGoodReview(revieweeId, review.getId());
        } else if (rating <= 2) {
            creditService.penalizeBadReview(revieweeId, review.getId());
        }
        
        return review;
    }
    
    /**
     * 查询工具的评价列表
     */
    public List<Review> getToolReviews(Long toolId) {
        // 通过借用记录查询工具的所有评价
        List<Borrow> borrows = borrowRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getToolId, toolId)
        );
        
        if (borrows.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<Long> borrowIds = borrows.stream()
            .map(Borrow::getId)
            .collect(java.util.stream.Collectors.toList());
        
        return reviewRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review>()
                .in(Review::getBorrowId, borrowIds)
                .orderByDesc(Review::getCreatedAt)
        );
    }
    
    /**
     * 查询用户收到的评价
     */
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review>()
                .eq(Review::getRevieweeId, userId)
                .orderByDesc(Review::getCreatedAt)
        );
    }
    
    /**
     * 查询用户发出的评价
     */
    public List<Review> getMyReviews(Long userId) {
        return reviewRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Review>()
                .eq(Review::getReviewerId, userId)
                .orderByDesc(Review::getCreatedAt)
        );
    }
    
    /**
     * 标签列表转 JSON
     */
    private String convertTagsToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            log.error("标签列表转JSON失败", e);
            return "[]";
        }
    }
}
