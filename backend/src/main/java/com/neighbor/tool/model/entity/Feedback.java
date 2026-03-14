package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 */
@Data
@TableName("feedbacks")
public class Feedback {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String type;
    
    private String content;
    
    private String images;
    
    private String contact;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
