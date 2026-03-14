package com.neighbor.tool.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowVO {
    private Long id;
    private Long toolId;
    private Long borrowerId;
    private Long lenderId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualReturnDate;
    private Integer days;
    private String note;
    private String status;
    private BigDecimal depositAmount;
    private BigDecimal rentAmount;
    private BigDecimal deductAmount;
    private Boolean isDamaged;
    private String damageNote;
    private ToolVO tool;
    private UserVO borrower;
    private UserVO lender;
    private LocalDateTime createdAt;
}
