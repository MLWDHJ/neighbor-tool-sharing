package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collections")
public class Collection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long toolId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
