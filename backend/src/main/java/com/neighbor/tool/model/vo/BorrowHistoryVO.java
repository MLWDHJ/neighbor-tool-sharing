package com.neighbor.tool.model.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 借用历史视图对象（用于工具详情页）
 */
@Data
public class BorrowHistoryVO {
    
    private Long id;
    
    private String borrowerNickname;
    
    private String borrowerAvatar;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer rating; // 评价评分
    
    private String comment; // 评价内容
}
