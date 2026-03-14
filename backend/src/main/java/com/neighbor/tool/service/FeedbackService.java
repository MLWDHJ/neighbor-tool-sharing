package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.Feedback;
import com.neighbor.tool.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Feedback Service
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    /**
     * 提交反馈
     */
    public Feedback submitFeedback(Long userId, String type, String content, String images, String contact) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setContent(content);
        feedback.setImages(images);
        feedback.setContact(contact);
        feedback.setStatus("pending");
        
        feedbackRepository.insert(feedback);
        return feedback;
    }
}
