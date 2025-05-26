package com.example.loan.service;

import com.example.loan.dto.LoanSummaryDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanApprovalResponse;
import com.example.loan.entity.Officer;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.OfficerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    @Autowired
    private OfficerRepository officerRepository;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public boolean hasTwoActiveLoans(String username) {
        List<Loan> activeLoans = loanRepository.findByUserUsernameAndStatus(username, "APPROVED");
        return activeLoans.size() >= 2;
    }
    @Async
    public CompletableFuture<LoanApprovalResponse> processLoanApproval(Loan loan) {
        return CompletableFuture.supplyAsync(() -> {
            if (hasTwoActiveLoans(loan.getUser().getUsername())) {
                loan.setStatus("REJECTED");
                loan.setRemarks("Customer already has 2 active loans.");
                loanRepository.save(loan);
                return new LoanApprovalResponse(loan.getStatus(),0.0,loan.getRemarks());
            }
            double emi=calculateEMI(loan.getAmount(),loan.getTenure());
            loan.setEmi(emi);
            int score = calculateLoanApprovalScore(loan);
            loan.setLoanApprovalScore(score);
            if (score >= 75) {
                loan.setStatus("APPROVED");
                loan.setRemarks("Approved as per policy");
            } else if (score >= 70) {
                loan.setPreviousStatus(loan.getStatus());
                loan.setStatus("PENDING_ADMIN");
                loan.setRemarks("Pending with officer approval");
                Officer officer=assignLoanToRandomOfficer();
                loan.setAssignedOfficer(officer);
            } else {
                loan.setStatus("REJECTED");
                loan.setRemarks("Approval score is less than 71");
            }
            loanRepository.save(loan);
            return new LoanApprovalResponse(loan.getStatus(),emi,loan.getRemarks());
        }, executor);
    }
    public Officer assignLoanToRandomOfficer(){
        List<Officer> officers=officerRepository.findAll();
        if(officers.isEmpty()){
            throw new RuntimeException("No officer available to assign the loan approval");
        }
        Random random=new Random();
        return officers.get(random.nextInt(officers.size()));
    }
    private double calculateEMI(Double amount, Integer tenure) {
        double monthlyInterestRate= 0.12/12;
        return Math.round((amount*monthlyInterestRate*Math.pow(1+monthlyInterestRate,tenure))/(Math.pow(1+monthlyInterestRate,tenure)-1)*100.0)/100.0;
    }

    private int calculateLoanApprovalScore(Loan loan) {
        double emi = (loan.getAmount() / loan.getTenure()) * 0.1;
        double affordability = loan.getMonthlyIncome() - (loan.getOtherExpenses() + emi);
        return affordability > 0 ? (int) ((affordability / loan.getMonthlyIncome()) * 100) : 0;
    }

    public List<Loan> findLoansByStatus(String status) {
        return loanRepository.findByStatus(status);
    }

    public void processOfficerDecision(UUID loanId, String decision,String remarks) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setPreviousStatus(loan.getStatus());
        if ("APPROVE".equalsIgnoreCase(decision)) {
            loan.setStatus("APPROVED");
            loan.setRemarks("Approved loan by loan officer");
        } else if ("REJECT".equalsIgnoreCase(decision)) {
            loan.setStatus("REJECTED");
            loan.setRemarks(remarks);
        } else {
            throw new IllegalArgumentException("Invalid decision");
        }
        loanRepository.save(loan);
    }

    public List<Loan> findLoansByUsername(String username) {
        return loanRepository.findByUserUsername(username);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
    public List<LoanSummaryDto> getLoanAmountSummary() {
        return loanRepository.getLoanAmountSummary();
    }

    public List<Loan> findLoansByAssignedOfficerAndStatus(Officer officer, String status) {
        return loanRepository.findByAssignedOfficerAndStatus(officer, status);
    }

}