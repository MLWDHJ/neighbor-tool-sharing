package com.neighbor.tool.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布工具请求 DTO
 */
@Data
public class PublishToolRequest {
    
    @NotBlank(message = "工具名称不能为空")
    @Size(max = 100, message = "工具名称不能超过100字")
    private String name;
    
    @NotBlank(message = "工具分类不能为空")
    @Pattern(regexp = "^(electric|manual|outdoor|digital|daily)$", message = "工具分类不正确")
    private String category;
    
    @NotNull(message = "工具图片不能为空")
    @Size(min = 1, max = 3, message = "工具图片数量必须在1-3张之间")
    private List<String> images;
    
    @NotNull(message = "购买价格不能为空")
    @DecimalMin(value = "0.01", message = "购买价格必须大于0")
    private BigDecimal price;
    
    @NotBlank(message = "新旧程度不能为空")
    @Pattern(regexp = "^(new|like_new|good|fair)$", message = "新旧程度不正确")
    private String condition;
    
    @NotNull(message = "是否免费不能为空")
    private Boolean isFree;
    
    private BigDecimal rentFee;
    
    @NotNull(message = "押金金额不能为空")
    @DecimalMin(value = "0", message = "押金金额不能为负数")
    private BigDecimal deposit;
    
    @NotNull(message = "最长借用天数不能为空")
    @Min(value = 1, message = "最长借用天数至少为1天")
    @Max(value = 365, message = "最长借用天数不能超过365天")
    private Integer maxDays;
    
    @Size(max = 500, message = "工具描述不能超过500字")
    private String description;
}
