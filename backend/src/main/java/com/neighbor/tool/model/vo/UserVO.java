package com.neighbor.tool.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    
    private Long id;
    
    private String phone;
    
    private String nickname;
    
    private String avatar;
    
    private String location;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private Integer creditScore;
    
    private Boolean isVerified;
    
    private String status;
    
    private LocalDateTime createdAt;
}
