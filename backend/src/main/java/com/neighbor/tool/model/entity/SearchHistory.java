package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史实体
 */
@Data
@TableName("search_history")
public class SearchHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String keyword;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
