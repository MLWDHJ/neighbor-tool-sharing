package com.neighbor.tool.controller;

import com.neighbor.tool.model.dto.SubmitFeedbackRequest;
import com.neighbor.tool.model.entity.Faq;
import com.neighbor.tool.model.entity.Feedback;
import com.neighbor.tool.service.FaqService;
import com.neighbor.tool.service.FeedbackService;
import com.neighbor.tool.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帮助中心和反馈 Controller
 */
@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpController {
    
    private final FaqService faqService;
    private final FeedbackService feedbackService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取所有FAQ
     */
    @GetMapping("/faqs")
    public ResponseEntity<List<Faq>> getAllFaqs() {
        return ResponseEntity.ok(faqService.getAllFaqs());
    }
    
    /**
     * 按分类获取FAQ
     */
    @GetMapping("/faqs/{category}")
    public ResponseEntity<List<Faq>> getFaqsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(faqService.getFaqsByCategory(category));
    }
    
    /**
     * 提交反馈
     */
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        
        Feedback feedback = feedbackService.submitFeedback(
            userId,
            request.getType(),
            request.getContent(),
            request.getImages(),
            request.getContact()
        );
        
        return ResponseEntity.ok(Map.of(
            "message", "反馈提交成功",
            "feedbackId", feedback.getId()
        ));
    }
}
