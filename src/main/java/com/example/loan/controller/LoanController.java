package com.example.loan.controller;

import com.example.loan.dto.LoanRequestDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanApprovalResponse;
import com.example.loan.entity.User;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.UserRepository;
import com.example.loan.service.LoanService;
import com.example.loan.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/loans")
public class LoanController {
    private static final Logger logger= LoggerFactory.getLogger(LoanController.class);
    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoanRepository loanRepository;

    @GetMapping("/apply")
    public String showLoanApplicationForm() {
        return "loan-apply";
    }
    @GetMapping("/status")
    public ResponseEntity<List<Loan>> getLoanStatus(@RequestParam String username) {
        logger.info("Given username: "+username);
        List<Loan> loans=loanService.findLoansByUsername(username);
        logger.debug("Get loans using username: "+username+" are "+loans);
        return ResponseEntity.ok(loans);
    }
    @GetMapping("/loan-status")
    public String getLoanStatus(Model model){
        List<Loan> loan=loanService.getAllLoans();
        logger.debug("Get all loans: "+loan);
        model.addAttribute("loan",loan);
        return "loanStatus";
    }
    public boolean hasPendingLoan(String username) {
        List<Loan> pendingLoans = loanRepository.findByUserUsernameAndStatus(username, "PENDING_ADMIN");
        return !pendingLoans.isEmpty();
    }
    @PostMapping("/apply")
    public ResponseEntity<LoanApprovalResponse> applyForLoan(@RequestBody LoanRequestDto request) {
        String username = request.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Loan loan = new Loan();
        loan.setAddress(request.getAddress());
        loan.setAmount(request.getAmount());
        loan.setMonthlyIncome(request.getMonthlyIncome());
        loan.setOtherExpenses(request.getOtherExpenses());
        loan.setTenure(request.getTenure());
        loan.setUser(user);

        if (loanService.hasTwoActiveLoans(username)) {
            loan.setStatus("REJECTED");
            loan.setRemarks("You already have 2 active loans. Cannot apply for a new loan.");
            loanRepository.save(loan);
            return ResponseEntity.ok(new LoanApprovalResponse(loan.getStatus(), 0.0,loan.getRemarks()));
        }

        if (hasPendingLoan(username)) {
            loan.setStatus("REJECTED");
            loan.setRemarks("You have a pending loan application. Please wait for approval or rejection.");
            loanRepository.save(loan);
            return ResponseEntity.ok(new LoanApprovalResponse(loan.getStatus(), 0.0,loan.getRemarks()));
        }
        loan.setStatus("PROCESSING");
        loanRepository.save(loan);
        CompletableFuture<LoanApprovalResponse> result = loanService.processLoanApproval(loan);
        return ResponseEntity.ok(result.join());
    }

}
