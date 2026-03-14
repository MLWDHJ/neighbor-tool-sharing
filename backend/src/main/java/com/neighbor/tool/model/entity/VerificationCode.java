package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 */
@Data
@TableName("verification_codes")
public class VerificationCode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String phone;
    
    private String code;
    
    private LocalDateTime expiresAt;
    
    private Boolean isUsed;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
