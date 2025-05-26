package com.example.loan.service;

import com.example.loan.dto.LoanSummaryDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanApprovalResponse;
import com.example.loan.entity.Officer;
import com.example.loan.entity.User;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.OfficerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private OfficerRepository officerRepository;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private User user;
    private Officer officer1,officer2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");

        officer1 = new Officer();
        officer1.setId(1L);
        officer1.setName("raju");
        officer1.setEmail("raju@gmail.com");

        officer2=new Officer();
        officer2.setId(2L);
        officer2.setName("dhivya");
        officer2.setEmail("dhiv@gmail.com");

        loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setUser(user);
        loan.setAmount(50000.0);
        loan.setTenure(12);
        loan.setMonthlyIncome(50000.0);
        loan.setOtherExpenses(10000.0);
        loan.setAssignedOfficer(officer1);
        loan.setStatus("APPROVED");
    }

    @Test
    @DisplayName("Checking two active loans")
    void testHasTwoActiveLoans_True() {
        when(loanRepository.findByUserUsernameAndStatus("testUser", "APPROVED"))
                .thenReturn(Arrays.asList(new Loan(), new Loan()));

        boolean result = loanService.hasTwoActiveLoans("testUser");

        assertTrue(result);
    }

    @Test
    @DisplayName("Checking active loan is false")
    void testHasTwoActiveLoans_False() {
        when(loanRepository.findByUserUsernameAndStatus("testUser", "APPROVED"))
                .thenReturn(Collections.singletonList(new Loan()));

        boolean result = loanService.hasTwoActiveLoans("testUser");

        assertFalse(result);
    }

    @Test
    @DisplayName("Process loan approval")
    void testProcessLoanApproval_Approved() {
        when(loanRepository.findByUserUsernameAndStatus(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        CompletableFuture<LoanApprovalResponse> future = loanService.processLoanApproval(loan);
        LoanApprovalResponse response = future.join();

        assertEquals("APPROVED", response.getStatus());
        assertNotNull(response.getEmi());
        assertEquals("Approved as per policy", response.getRemarks());
    }

    @Test
    @DisplayName("Rejected loan approval")
    void testProcessLoanApproval_Rejected_TwoActiveLoans() {
        when(loanRepository.findByUserUsernameAndStatus(anyString(), eq("APPROVED")))
                .thenReturn(Arrays.asList(new Loan(), new Loan()));

        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        CompletableFuture<LoanApprovalResponse> future = loanService.processLoanApproval(loan);
        LoanApprovalResponse response = future.join();

        assertEquals("REJECTED", response.getStatus());
        assertEquals("Customer already has 2 active loans.", response.getRemarks());
    }

    @Test
    @DisplayName("Officer approved loan")
    void testProcessOfficerDecision_Approve() {
        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.of(loan));

        loanService.processOfficerDecision(loan.getId(), "APPROVE", "");

        assertEquals("APPROVED", loan.getStatus());
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    @DisplayName("Officer rejected loan")
    void testProcessOfficerDecision_Reject() {
        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.of(loan));

        loanService.processOfficerDecision(loan.getId(), "REJECT", "Not eligible");

        assertEquals("REJECTED", loan.getStatus());
        assertEquals("Not eligible", loan.getRemarks());
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    @DisplayName("Invalid officer decision")
    void testProcessOfficerDecision_Invalid() {
        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.of(loan));

        assertThrows(IllegalArgumentException.class, () ->
                loanService.processOfficerDecision(loan.getId(), "INVALID", ""));
    }

    @Test
    @DisplayName("Find loan using status")
    void testFindLoansByStatus() {
        when(loanRepository.findByStatus("APPROVED")).thenReturn(Collections.singletonList(loan));

        List<Loan> loans = loanService.findLoansByStatus("APPROVED");

        assertEquals(1, loans.size());
        assertEquals("APPROVED", loans.get(0).getStatus());
    }

    @Test
    @DisplayName("Find loans using username")
    void testFindLoansByUsername() {
        when(loanRepository.findByUserUsername("testUser")).thenReturn(Collections.singletonList(loan));

        List<Loan> loans = loanService.findLoansByUsername("testUser");

        assertEquals(1, loans.size());
        assertEquals("testUser", loans.get(0).getUser().getUsername());
    }

    @Test
    @DisplayName("Get all loans")
    void testGetAllLoans() {
        when(loanRepository.findAll()).thenReturn(Collections.singletonList(loan));

        List<Loan> loans = loanService.getAllLoans();

        assertEquals(1, loans.size());
    }

    @Test
    @DisplayName("Get loan amount summary")
    void testGetLoanAmountSummary() {
        LoanSummaryDto summary = new LoanSummaryDto("Dhivya",50000.0d);
        when(loanRepository.getLoanAmountSummary()).thenReturn(Collections.singletonList(summary));

        List<LoanSummaryDto> summaries = loanService.getLoanAmountSummary();

        assertEquals(1, summaries.size());
        assertEquals(50000.0, summaries.get(0).getTotalAmount());
    }

    @Test
    @DisplayName("Find loan using assigned officer and status")
    void testFindLoansByAssignedOfficerAndStatus() {
        when(loanRepository.findByAssignedOfficerAndStatus(officer1, "PENDING_ADMIN"))
                .thenReturn(Collections.singletonList(loan));

        List<Loan> loans = loanService.findLoansByAssignedOfficerAndStatus(officer1, "PENDING_ADMIN");

        assertEquals(1, loans.size());
        assertEquals(officer1, loans.get(0).getAssignedOfficer());
    }
}