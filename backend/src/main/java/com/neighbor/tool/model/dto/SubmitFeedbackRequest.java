package com.neighbor.tool.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交反馈请求
 */
@Data
public class SubmitFeedbackRequest {
    
    @NotBlank(message = "反馈类型不能为空")
    private String type;
    
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String content;
    
    private String images;
    
    private String contact;
}
