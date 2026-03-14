package com.neighbor.tool.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
    private Long id;
    private Long borrowId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer rating;
    private List<String> tags;
    private String comment;
    private UserVO reviewer;
    private UserVO reviewee;
    private LocalDateTime createdAt;
}
