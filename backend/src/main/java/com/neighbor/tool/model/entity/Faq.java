package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * FAQ（常见问题）实体
 */
@Data
@TableName("faqs")
public class Faq {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String category;
    
    private String question;
    
    private String answer;
    
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
