package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用记录实体类
 */
@Data
@TableName("credit_logs")
public class CreditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer changeAmount;
    
    private String reason;
    
    private Long relatedId;
    
    private Integer beforeScore;
    
    private Integer afterScore;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
