package com.example.loan.dto;

import lombok.Data;

@Data
public class LoanSummaryDto {
    private String name;
    private Double totalAmount;
    public LoanSummaryDto(String name,Double totalAmount){
        this.name=name;
        this.totalAmount=totalAmount;
    }
}
