package com.neighbor.tool.controller;

import com.neighbor.tool.model.entity.Review;
import com.neighbor.tool.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        Long reviewerId = (Long) authentication.getPrincipal();
        Long borrowId = Long.valueOf(request.get("borrowId").toString());
        Integer rating = Integer.valueOf(request.get("rating").toString());
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");
        String comment = (String) request.get("comment");
        
        Review review = reviewService.createReview(reviewerId, borrowId, rating, tags, comment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("review", review);
        response.put("message", "评价提交成功");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tool/{toolId}")
    public ResponseEntity<Map<String, Object>> getToolReviews(@PathVariable Long toolId) {
        List<Review> reviews = reviewService.getToolReviews(toolId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reviews", reviews);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserReviews(@PathVariable Long userId) {
        List<Review> reviews = reviewService.getUserReviews(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reviews", reviews);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取我发出的评价
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReviews(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Review> reviews = reviewService.getMyReviews(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reviews", reviews);
        return ResponseEntity.ok(response);
    }
}
