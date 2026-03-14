package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("reviews")
public class Review {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long borrowId;
    
    private Long reviewerId;
    
    private Long revieweeId;
    
    private Integer rating;
    
    private String tags; // JSON 字符串
    
    private String comment;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
