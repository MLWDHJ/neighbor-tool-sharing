package com.neighbor.tool.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighbor.tool.model.entity.Borrow;
import com.neighbor.tool.repository.BorrowRepository;
import com.neighbor.tool.service.CreditService;
import com.neighbor.tool.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 逾期检测定时任务
 * 每天检测逾期借用记录并处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueCheckTask {
    
    private final BorrowRepository borrowRepository;
    private final CreditService creditService;
    private final MessageService messageService;
    
    /**
     * 每天凌晨1点执行逾期检测
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkOverdueBorrows() {
        log.info("开始执行逾期检测任务");
        
        LocalDate today = LocalDate.now();
        
        // 查询所有进行中的借用记录
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getStatus, "in_use")
               .le(Borrow::getEndDate, today);
        
        List<Borrow> overdueBorrows = borrowRepository.selectList(wrapper);
        
        log.info("发现 {} 条逾期记录", overdueBorrows.size());
        
        for (Borrow borrow : overdueBorrows) {
            processOverdueBorrow(borrow, today);
        }
        
        log.info("逾期检测任务完成");
    }
    
    /**
     * 处理单条逾期记录
     */
    private void processOverdueBorrow(Borrow borrow, LocalDate today) {
        LocalDate endDate = borrow.getEndDate();
        long overdueDays = ChronoUnit.DAYS.between(endDate, today);
        
        log.info("处理逾期记录: borrowId={}, overdueDays={}", borrow.getId(), overdueDays);
        
        // 扣除信用评分（每天-5分）
        creditService.penalizeOverdue(borrow.getBorrowerId(), borrow.getId(), (int) overdueDays);
        
        // 发送逾期提醒通知
        if (overdueDays == 1) {
            // 逾期第1天
            createOverdueMessage(borrow, "您的借用已逾期1天，请尽快归还");
        } else if (overdueDays == 3) {
            // 逾期第3天
            createOverdueMessage(borrow, "您的借用已逾期3天，请立即归还，否则将影响信用评分");
        } else if (overdueDays == 7) {
            // 逾期第7天
            createOverdueMessage(borrow, "您的借用已逾期7天，信用评分已严重受损，请立即归还");
        } else if (overdueDays > 7 && overdueDays % 7 == 0) {
            // 逾期超过7天，每7天提醒一次
            createOverdueMessage(borrow, String.format("您的借用已逾期%d天，请立即归还", overdueDays));
        }
    }
    
    /**
     * 创建逾期提醒消息
     */
    private void createOverdueMessage(Borrow borrow, String content) {
        // 这里可以调用MessageService创建消息
        log.info("发送逾期提醒: borrowId={}, content={}", borrow.getId(), content);
    }
    
    /**
     * 到期前1天提醒
     * 每天上午10点执行
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void checkUpcomingDueBorrows() {
        log.info("开始执行到期提醒任务");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // 查询明天到期的借用记录
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getStatus, "in_use")
               .eq(Borrow::getEndDate, tomorrow);
        
        List<Borrow> upcomingBorrows = borrowRepository.selectList(wrapper);
        
        log.info("发现 {} 条即将到期记录", upcomingBorrows.size());
        
        for (Borrow borrow : upcomingBorrows) {
            log.info("发送到期提醒: borrowId={}", borrow.getId());
            // 这里可以调用MessageService创建消息
        }
        
        log.info("到期提醒任务完成");
    }
}
