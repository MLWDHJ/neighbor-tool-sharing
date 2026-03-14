package com.neighbor.tool.service;

import com.neighbor.tool.model.entity.Message;
import com.neighbor.tool.model.vo.MessageVO;
import com.neighbor.tool.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务
 * 负责创建和管理系统消息通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    /**
     * 创建借用申请通知
     */
    @Transactional
    public void createBorrowRequestMessage(Long lenderId, Long borrowId, String borrowerName, String toolName) {
        Message message = new Message();
        message.setUserId(lenderId);
        message.setType("borrow_request");
        message.setTitle("收到新的借用申请");
        message.setContent(String.format("%s 想借用您的 %s", borrowerName, toolName));
        message.setRelatedId(borrowId);
        message.setIsRead(false);
        
        messageRepository.insert(message);
        log.info("创建借用申请通知: userId={}, borrowId={}", lenderId, borrowId);
    }
    
    /**
     * 创建申请同意通知
     */
    @Transactional
    public void createRequestApprovedMessage(Long borrowerId, Long borrowId, String toolName) {
        Message message = new Message();
        message.setUserId(borrowerId);
        message.setType("request_approved");
        message.setTitle("借用申请已同意");
        message.setContent(String.format("您的 %s 借用申请已被同意", toolName));
        message.setRelatedId(borrowId);
        message.setIsRead(false);
        
        messageRepository.insert(message);
        log.info("创建申请同意通知: userId={}, borrowId={}", borrowerId, borrowId);
    }
    
    /**
     * 创建申请拒绝通知
     */
    @Transactional
    public void createRequestRejectedMessage(Long borrowerId, Long borrowId, String toolName, String reason) {
        Message message = new Message();
        message.setUserId(borrowerId);
        message.setType("request_rejected");
        message.setTitle("借用申请已拒绝");
        message.setContent(String.format("您的 %s 借用申请已被拒绝。原因：%s", toolName, reason));
        message.setRelatedId(borrowId);
        message.setIsRead(false);
        
        messageRepository.insert(message);
        log.info("创建申请拒绝通知: userId={}, borrowId={}", borrowerId, borrowId);
    }
    
    /**
     * 创建归还确认通知
     */
    @Transactional
    public void createReturnConfirmMessage(Long lenderId, Long borrowId, String borrowerName, String toolName) {
        Message message = new Message();
        message.setUserId(lenderId);
        message.setType("return_reminder");
        message.setTitle("工具归还确认");
        message.setContent(String.format("%s 已标记归还 %s，请确认", borrowerName, toolName));
        message.setRelatedId(borrowId);
        message.setIsRead(false);
        
        messageRepository.insert(message);
        log.info("创建归还确认通知: userId={}, borrowId={}", lenderId, borrowId);
    }
    
    /**
     * 查询用户消息列表
     */
    public List<MessageVO> getUserMessages(Long userId, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Message> pageParam = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .orderByDesc(Message::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Message> result = 
            messageRepository.selectPage(pageParam, wrapper);
        
        List<MessageVO> voList = new ArrayList<>();
        for (Message message : result.getRecords()) {
            MessageVO vo = new MessageVO();
            vo.setId(message.getId());
            vo.setType(message.getType());
            vo.setTitle(message.getTitle());
            vo.setContent(message.getContent());
            vo.setRelatedId(message.getRelatedId());
            vo.setIsRead(message.getIsRead());
            vo.setCreatedAt(message.getCreatedAt());
            voList.add(vo);
        }
        
        return voList;
    }
    
    /**
     * 获取未读消息数量
     */
    public int getUnreadCount(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .eq(Message::getIsRead, false);
        
        return messageRepository.selectCount(wrapper).intValue();
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.selectById(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            message.setIsRead(true);
            messageRepository.updateById(message);
        }
    }
    
    /**
     * 全部标记为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .eq(Message::getIsRead, false);
        
        List<Message> messages = messageRepository.selectList(wrapper);
        for (Message message : messages) {
            message.setIsRead(true);
            messageRepository.updateById(message);
        }
    }
    
    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.selectById(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            messageRepository.deleteById(messageId);
        }
    }
}
