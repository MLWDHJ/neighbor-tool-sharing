package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工具实体类
 */
@Data
@TableName("tools")
public class Tool {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String name;
    
    private String category;
    
    private String images; // JSON 字符串
    
    private BigDecimal price;
    
    @TableField("`condition`")
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
    
    private Integer viewCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
