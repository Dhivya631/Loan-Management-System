package com.example.loan.controller;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.configuration.JWTAuthenticationFilter;
import com.example.loan.dto.LoanRequestDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanApprovalResponse;
import com.example.loan.entity.User;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.UserRepository;
import com.example.loan.service.LoanService;
import com.example.loan.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = LoanController.class,excludeAutoConfiguration = SecurityAutoConfiguration.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private JWTTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LoanRepository loanRepository;
    @InjectMocks
    private LoanController loanController;

    private ObjectMapper objectMapper;
    private LoanRequestDto loanRequestDto;
    private User user;
    private Loan loan;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(loanController).build();
        objectMapper=new ObjectMapper();
    }
    @Test
    @DisplayName("Display apply loan page")
    void testapply() throws Exception{
        mockMvc.perform(get("/loans/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("loan-apply"));
    }
    @Test
    @DisplayName("Display loan status")
    void testGetLoanStatus_Success() throws Exception{
        // Given
        User user = new User();
        user.setUsername("testUser");

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setStatus("PROCESSING");

        //when
        when(loanService.getAllLoans()).thenReturn(List.of(loan));
        mockMvc.perform(get("/loans/loan-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("loan"))
                .andExpect(view().name("loanStatus"));
    }
    @Test
    @DisplayName("Get loan status using username")
    void testGetStatusUsingUsername_Success() throws Exception{
        // Given
        User user=new User();
        user.setUsername("user");
        Loan loan1=new Loan();
        loan1.setUser(user);
        loan1.setStatus("APPROVED");
        String username = "testUser";
        List<Loan> mockLoans = Arrays.asList(loan1);

        when(loanService.findLoansByUsername(username)).thenReturn(mockLoans);

        mockMvc.perform(get("/loans/status")
                        .param("username", username)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertEquals("user",loan1.getUser().getUsername());
    }
    @Test
    @DisplayName("Customer applied loan successfully")
    void testApplyForLoan_Success() throws Exception {
        // Given
        LoanRequestDto request = new LoanRequestDto();
        request.setUsername("testUser");
        request.setAmount(10000.0);
        request.setMonthlyIncome(5000.0);
        request.setOtherExpenses(2000.0);
        request.setTenure(12);
        request.setAddress("123 Test St");

        User user = new User();
        user.setUsername("testUser");

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setStatus("PROCESSING");

        LoanApprovalResponse approvalResponse = new LoanApprovalResponse("APPROVED", 10000.0, "Loan approved");
        //when
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(loanService.hasTwoActiveLoans(anyString())).thenReturn(false);
        when(loanRepository.findByUserUsernameAndStatus(anyString(), anyString())).thenReturn(List.of());
        when(loanService.processLoanApproval(any(Loan.class))).thenReturn(CompletableFuture.completedFuture(approvalResponse));

        // Act
        mockMvc.perform(post("/loans/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        assertEquals("APPROVED",approvalResponse.getStatus());
        assertEquals("Loan approved",approvalResponse.getRemarks());
    }

    @Test
    @DisplayName("Rejected loan application due to already have 2 active loans")
    void testApplyForLoan_RejectedDueToActiveLoans() throws Exception {
        // Given
        LoanRequestDto request = new LoanRequestDto();
        request.setUsername("testUser");
        request.setAmount(10000.0);
        request.setMonthlyIncome(5000.0);
        request.setOtherExpenses(2000.0);
        request.setTenure(12);
        request.setAddress("123 Test St");

        User user = new User();
        user.setUsername("testUser");

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setStatus("REJECTED");
        loan.setRemarks("You already have 2 active loans. Cannot apply for a new loan.");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(loanService.hasTwoActiveLoans(anyString())).thenReturn(true);

        // Act
        mockMvc.perform(post("/loans/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        assertEquals("REJECTED",loan.getStatus());
        assertEquals("You already have 2 active loans. Cannot apply for a new loan.",loan.getRemarks());
    }
    @Test
    @DisplayName("Pending loans")
    void testApplyForLoan_HasPendingLoan() throws Exception {
        //given
        loanRequestDto = new LoanRequestDto();
        loanRequestDto.setUsername("testUser");
        loanRequestDto.setAddress("123 Street");
        loanRequestDto.setAmount(50000.0);
        loanRequestDto.setMonthlyIncome(10000.0);
        loanRequestDto.setOtherExpenses(2000.0);
        loanRequestDto.setTenure(12);

        user = new User();
        user.setUsername("testUser");

        loan = new Loan();
        loan.setUser(user);
        loan.setStatus("PENDING_ADMIN");
        //when
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(loanRepository.findByUserUsernameAndStatus("testUser", "PENDING_ADMIN"))
                .thenReturn(Collections.singletonList(loan));

        mockMvc.perform(post("/loans/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loanRequestDto)))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("In apply loan - User not found")
    void testUsernameInApplyLoan() throws Exception{
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userRepository.findByUsername("nonExistingUser")
                    .orElseThrow(() -> new RuntimeException("User not found"));
        });

        assertEquals("User not found", exception.getMessage());
    }
}