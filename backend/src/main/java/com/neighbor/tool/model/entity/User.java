package com.neighbor.tool.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String phone;
    
    private String nickname;
    
    private String avatar;
    
    private String location;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private Integer creditScore;
    
    private Boolean isVerified;
    
    private String idCardFront;
    
    private String idCardBack;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
