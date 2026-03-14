package com.neighbor.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.model.entity.Tool;
import com.neighbor.tool.model.entity.User;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.repository.ToolRepository;
import com.neighbor.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 借用服务
 * 负责借用申请、审批、归还等流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {
    
    private final BorrowRepository borrowRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final CreditService creditService;
    
    /**
     * 创建借用申请
     */
    @Transactional
    public Borrow createBorrowRequest(Long borrowerId, Long toolId, LocalDate startDate, 
                                     LocalDate endDate, String note) {
        // 验证日期
        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("开始日期不能早于今天");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("归还日期不能早于开始日期");
        }
        
        // 验证工具
        Tool tool = toolRepository.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("工具不存在");
        }
        if (!"available".equals(tool.getStatus())) {
            throw new RuntimeException("工具当前不可借用");
        }
        
        // 计算借用天数
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        // 验证借用天数
        if (days > tool.getMaxDays()) {
            throw new RuntimeException("借用天数超过最长借用天数");
        }
        
        // 验证借用说明长度
        if (note != null && note.length() > 100) {
            throw new RuntimeException("借用说明不能超过100字");
        }
        
        // 验证借用人信用评分
        User borrower = userRepository.selectById(borrowerId);
        if (borrower.getCreditScore() < 60) {
            throw new RuntimeException("信用评分过低，无法借用");
        }
        
        // 计算费用
        BigDecimal rentAmount = tool.getRentFee().multiply(BigDecimal.valueOf(days));
        
        // 创建借用记录
        Borrow borrow = new Borrow();
        borrow.setToolId(toolId);
        borrow.setBorrowerId(borrowerId);
        borrow.setLenderId(tool.getUserId());
        borrow.setStartDate(startDate);
        borrow.setEndDate(endDate);
        borrow.setDays(days);
        borrow.setNote(note);
        borrow.setStatus("pending");
        borrow.setDepositAmount(tool.getDeposit());
        borrow.setRentAmount(rentAmount);
        borrow.setDeductAmount(BigDecimal.ZERO);
        borrow.setIsDamaged(false);
        
        borrowRepository.insert(borrow);
        log.info("借用申请创建成功: borrowId={}, toolId={}, borrowerId={}", 
                borrow.getId(), toolId, borrowerId);
        
        // 发送通知
        User lender = userRepository.selectById(tool.getUserId());
        messageService.createBorrowRequestMessage(lender.getId(), borrow.getId(), 
                borrower.getNickname(), tool.getName());
        
        return borrow;
    }
    
    /**
     * 同意借用申请
     */
    @Transactional
    public void approveBorrowRequest(Long borrowId, Long lenderId) {
        Borrow borrow = borrowRepository.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        if (!borrow.getLenderId().equals(lenderId)) {
            throw new RuntimeException("无权操作此借用申请");
        }
        
        if (!"pending".equals(borrow.getStatus())) {
            throw new RuntimeException("借用申请已处理");
        }
        
        // 更新借用状态为使用中
        borrow.setStatus("in_use");
        borrowRepository.updateById(borrow);
        
        // 更新工具状态
        Tool tool = toolRepository.selectById(borrow.getToolId());
        tool.setStatus("borrowed");
        toolRepository.updateById(tool);
        
        log.info("借用申请已同意: borrowId={}", borrowId);
        
        // 发送通知
        messageService.createRequestApprovedMessage(borrow.getBorrowerId(), borrowId, tool.getName());
    }
    
    /**
     * 拒绝借用申请
     */
    @Transactional
    public void rejectBorrowRequest(Long borrowId, Long lenderId, String reason) {
        Borrow borrow = borrowRepository.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        if (!borrow.getLenderId().equals(lenderId)) {
            throw new RuntimeException("无权操作此借用申请");
        }
        
        if (!"pending".equals(borrow.getStatus())) {
            throw new RuntimeException("借用申请已处理");
        }
        
        // 更新借用状态
        borrow.setStatus("rejected");
        borrowRepository.updateById(borrow);
        
        log.info("借用申请已拒绝: borrowId={}, reason={}", borrowId, reason);
        
        // 发送通知
        Tool tool = toolRepository.selectById(borrow.getToolId());
        messageService.createRequestRejectedMessage(borrow.getBorrowerId(), borrowId, 
                tool.getName(), reason != null ? reason : "未说明");
    }
    
    /**
     * 标记已归还
     */
    @Transactional
    public void markAsReturned(Long borrowId, Long borrowerId) {
        Borrow borrow = borrowRepository.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        if (!borrow.getBorrowerId().equals(borrowerId)) {
            throw new RuntimeException("无权操作此借用记录");
        }
        
        if (!"in_use".equals(borrow.getStatus())) {
            throw new RuntimeException("借用记录状态不正确");
        }
        
        // 更新状态为待确认归还
        borrow.setStatus("returned");
        borrow.setActualReturnDate(LocalDate.now());
        borrowRepository.updateById(borrow);
        
        log.info("借用人已标记归还: borrowId={}", borrowId);
        
        // 发送通知
        Tool tool = toolRepository.selectById(borrow.getToolId());
        User borrower = userRepository.selectById(borrowerId);
        messageService.createReturnConfirmMessage(borrow.getLenderId(), borrowId, 
                borrower.getNickname(), tool.getName());
    }
    
    /**
     * 确认归还
     */
    @Transactional
    public void confirmReturn(Long borrowId, Long lenderId, Boolean isDamaged, 
                             String damageNote, BigDecimal deductAmount) {
        Borrow borrow = borrowRepository.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        if (!borrow.getLenderId().equals(lenderId)) {
            throw new RuntimeException("无权操作此借用记录");
        }
        
        if (!"returned".equals(borrow.getStatus())) {
            throw new RuntimeException("借用记录状态不正确");
        }
        
        // 更新借用记录（归还确认后状态保持returned）
        borrow.setStatus("returned");
        borrow.setIsDamaged(isDamaged != null && isDamaged);
        borrow.setDamageNote(damageNote);
        borrow.setDeductAmount(deductAmount != null ? deductAmount : BigDecimal.ZERO);
        borrowRepository.updateById(borrow);
        
        // 更新工具状态
        Tool tool = toolRepository.selectById(borrow.getToolId());
        tool.setStatus("available");
        tool.setBorrowCount(tool.getBorrowCount() + 1);
        toolRepository.updateById(tool);
        
        // 更新信用评分
        boolean isOnTime = borrow.getActualReturnDate().isBefore(borrow.getEndDate().plusDays(1));
        if (isOnTime) {
            creditService.rewardOnTimeReturn(borrow.getBorrowerId(), borrowId);
        } else {
            int overdueDays = (int) ChronoUnit.DAYS.between(borrow.getEndDate(), borrow.getActualReturnDate());
            creditService.penalizeOverdue(borrow.getBorrowerId(), borrowId, overdueDays);
        }
        
        if (isDamaged != null && isDamaged) {
            creditService.penalizeDamage(borrow.getBorrowerId(), borrowId);
        }
        
        log.info("归还已确认: borrowId={}, isDamaged={}", borrowId, isDamaged);
    }
    
    /**
     * 获取借用记录详情（带权限验证）
     */
    public Borrow getBorrowDetail(Long borrowId, Long userId) {
        Borrow borrow = borrowRepository.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        // 验证权限（只有借用人或出借人可以查看）
        if (!borrow.getBorrowerId().equals(userId) && !borrow.getLenderId().equals(userId)) {
            throw new RuntimeException("无权查看此借用记录");
        }
        
        return borrow;
    }
    
    /**
     * 获取借用记录详情VO（包含工具和用户信息）
     */
    public java.util.Map<String, Object> getBorrowDetailVO(Long borrowId, Long userId) {
        Borrow borrow = getBorrowDetail(borrowId, userId);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", borrow.getId());
        result.put("toolId", borrow.getToolId());
        result.put("borrowerId", borrow.getBorrowerId());
        result.put("lenderId", borrow.getLenderId());
        result.put("startDate", borrow.getStartDate());
        result.put("endDate", borrow.getEndDate());
        result.put("actualReturnDate", borrow.getActualReturnDate());
        result.put("days", borrow.getDays());
        result.put("note", borrow.getNote());
        result.put("status", borrow.getStatus());
        result.put("depositAmount", borrow.getDepositAmount());
        result.put("rentAmount", borrow.getRentAmount());
        result.put("deductAmount", borrow.getDeductAmount());
        result.put("isDamaged", borrow.getIsDamaged());
        result.put("damageNote", borrow.getDamageNote());
        result.put("createdAt", borrow.getCreatedAt());
        
        // 工具信息
        Tool tool = toolRepository.selectById(borrow.getToolId());
        if (tool != null) {
            java.util.Map<String, Object> toolInfo = new java.util.HashMap<>();
            toolInfo.put("id", tool.getId());
            toolInfo.put("name", tool.getName());
            toolInfo.put("category", tool.getCategory());
            toolInfo.put("images", parseImagesJson(tool.getImages()));
            toolInfo.put("isFree", tool.getIsFree());
            toolInfo.put("rentFee", tool.getRentFee());
            result.put("tool", toolInfo);
        }
        
        // 借用人信息
        User borrower = userRepository.selectById(borrow.getBorrowerId());
        if (borrower != null) {
            java.util.Map<String, Object> borrowerInfo = new java.util.HashMap<>();
            borrowerInfo.put("id", borrower.getId());
            borrowerInfo.put("nickname", borrower.getNickname());
            borrowerInfo.put("avatar", borrower.getAvatar());
            borrowerInfo.put("phone", borrower.getPhone());
            borrowerInfo.put("creditScore", borrower.getCreditScore());
            result.put("borrower", borrowerInfo);
        }
        
        // 出借人信息
        User lender = userRepository.selectById(borrow.getLenderId());
        if (lender != null) {
            java.util.Map<String, Object> lenderInfo = new java.util.HashMap<>();
            lenderInfo.put("id", lender.getId());
            lenderInfo.put("nickname", lender.getNickname());
            lenderInfo.put("avatar", lender.getAvatar());
            lenderInfo.put("phone", lender.getPhone());
            lenderInfo.put("creditScore", lender.getCreditScore());
            result.put("lender", lenderInfo);
        }
        
        return result;
    }
    
    /**
     * 获取我的借用列表（分页，包含工具和用户信息）
     */
    public java.util.List<java.util.Map<String, Object>> getMyBorrows(Long userId, String status, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Borrow> pageParam = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getBorrowerId, userId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Borrow::getStatus, status);
        }
        
        wrapper.orderByDesc(Borrow::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Borrow> result = 
            borrowRepository.selectPage(pageParam, wrapper);
        
        return enrichBorrowList(result.getRecords());
    }
    
    /**
     * 获取我的出借列表（分页，包含工具和用户信息）
     */
    public java.util.List<java.util.Map<String, Object>> getMyLends(Long userId, String status, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Borrow> pageParam = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getLenderId, userId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Borrow::getStatus, status);
        }
        
        wrapper.orderByDesc(Borrow::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Borrow> result = 
            borrowRepository.selectPage(pageParam, wrapper);
        
        return enrichBorrowList(result.getRecords());
    }
    
    /**
     * 解析images JSON字符串为List
     */
    private java.util.List<String> parseImagesJson(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        try {
            java.util.List<String> images = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(imagesJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
            // 清理旧数据中的 http://localhost:xxxx 前缀
            return images.stream().map(url -> {
                if (url != null && url.startsWith("http://localhost")) {
                    int idx = url.indexOf("/uploads/");
                    if (idx >= 0) {
                        return url.substring(idx);
                    }
                }
                return url;
            }).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("解析images JSON失败: {}", imagesJson, e);
            return new java.util.ArrayList<>();
        }
    }
    
    private java.util.List<java.util.Map<String, Object>> enrichBorrowList(java.util.List<Borrow> borrows) {
        java.util.List<java.util.Map<String, Object>> enrichedList = new java.util.ArrayList<>();
        
        for (Borrow borrow : borrows) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", borrow.getId());
            item.put("toolId", borrow.getToolId());
            item.put("borrowerId", borrow.getBorrowerId());
            item.put("lenderId", borrow.getLenderId());
            item.put("startDate", borrow.getStartDate());
            item.put("endDate", borrow.getEndDate());
            item.put("days", borrow.getDays());
            item.put("note", borrow.getNote());
            item.put("status", borrow.getStatus());
            item.put("depositAmount", borrow.getDepositAmount());
            item.put("rentAmount", borrow.getRentAmount());
            item.put("createdAt", borrow.getCreatedAt());
            
            // 工具信息
            Tool tool = toolRepository.selectById(borrow.getToolId());
            if (tool != null) {
                java.util.Map<String, Object> toolInfo = new java.util.HashMap<>();
                toolInfo.put("id", tool.getId());
                toolInfo.put("name", tool.getName());
                toolInfo.put("category", tool.getCategory());
                toolInfo.put("images", parseImagesJson(tool.getImages()));
                toolInfo.put("isFree", tool.getIsFree());
                toolInfo.put("rentFee", tool.getRentFee());
                item.put("tool", toolInfo);
            }
            
            // 借用人信息
            User borrower = userRepository.selectById(borrow.getBorrowerId());
            if (borrower != null) {
                java.util.Map<String, Object> borrowerInfo = new java.util.HashMap<>();
                borrowerInfo.put("id", borrower.getId());
                borrowerInfo.put("nickname", borrower.getNickname());
                borrowerInfo.put("avatar", borrower.getAvatar());
                item.put("borrower", borrowerInfo);
            }
            
            // 出借人信息
            User lender = userRepository.selectById(borrow.getLenderId());
            if (lender != null) {
                java.util.Map<String, Object> lenderInfo = new java.util.HashMap<>();
                lenderInfo.put("id", lender.getId());
                lenderInfo.put("nickname", lender.getNickname());
                lenderInfo.put("avatar", lender.getAvatar());
                item.put("lender", lenderInfo);
            }
            
            enrichedList.add(item);
        }
        
        return enrichedList;
    }
}
