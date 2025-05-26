package com.example.loan.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LoanStatus {
    private UUID loanId;
    private String status;
    private Integer loanApprovalScore;

}