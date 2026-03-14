package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借用记录实体类
 */
@Data
@TableName("borrows")
public class Borrow {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long toolId;
    
    private Long borrowerId;
    
    private Long lenderId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDate actualReturnDate;
    
    private Integer days;
    
    private String note;
    
    private String status;
    
    private BigDecimal depositAmount;
    
    private BigDecimal rentAmount;
    
    private BigDecimal deductAmount;
    
    private Boolean isDamaged;
    
    private String damageNote;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
