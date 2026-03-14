package com.neighbor.tool.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工具视图对象
 */
@Data
public class ToolVO {
    
    private Long id;
    
    private Long userId;
    
    private String name;
    
    private String category;
    
    private List<String> images;
    
    private BigDecimal price;
    
    private String condition;
    
    private Boolean isFree;
    
    private BigDecimal rentFee;
    
    private BigDecimal deposit;
    
    private Integer maxDays;
    
    private String description;
    
    private String status;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private Integer borrowCount;
    
    private Double distance; // 与用户的距离（米）
    
    private UserVO lender; // 出借人信息
    
    private List<BorrowHistoryVO> borrowHistory; // 借用历史（最多3条）
    
    private LocalDateTime expectedReturnDate; // 预计归还日期（已借出时）
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
