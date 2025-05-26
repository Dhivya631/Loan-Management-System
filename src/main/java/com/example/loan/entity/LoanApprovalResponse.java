package com.example.loan.entity;

import lombok.Data;

@Data
public class LoanApprovalResponse{
    private String status;
    private double emi;

    private String remarks;

    public LoanApprovalResponse(String status, double emi,String remarks) {
        this.status=status;
        this.emi=emi;
        this.remarks=remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getEmi() {
        return emi;
    }

    public void setEmi(double emi) {
        this.emi = emi;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}