package com.example.loan.controller;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.configuration.JWTAuthenticationFilter;
import com.example.loan.dto.LoanSummaryDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.Officer;
import com.example.loan.entity.User;
import com.example.loan.repository.OfficerRepository;
import com.example.loan.service.LoanService;
import com.example.loan.service.OfficerService;
import com.example.loan.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = ReportController.class,excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private JWTTokenProvider jwtTokenProvider;
    @MockitoBean
    private OfficerRepository officerRepository;
    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private OfficerService officerService;
    @InjectMocks
    private ReportController reportController;
    private User user;
    private Loan loan;
    private Officer officer;
    private LoanSummaryDto loanSummaryDto1,loanSummaryDto2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
        //loan
        user= new User();
        user.setUsername("testUser");
        user.setPassword("pass");

        loan = new Loan();
        loan.setUser(user);
        loan.setStatus("PROCESSING");

        //officer
        officer = new Officer();
        officer.setId(1L);
        officer.setUsername("officerUser");
        officer.setPassword("password123");

        //LoanSummaryDto
        loanSummaryDto1=new LoanSummaryDto("Dhivya",100000d);
        loanSummaryDto2=new LoanSummaryDto("Ramya",200000d);

    }
    @Test
    void testLoanReportPage() throws Exception{
        mockMvc.perform(get("/reports/loan"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"));
    }
    @Test
    void testUserReportPage() throws Exception{
        mockMvc.perform(get("/reports/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("userReport"));
    }
    @Test
    void testOfficerReportPage() throws Exception{
        mockMvc.perform(get("/reports/officer"))
                .andExpect(status().isOk())
                .andExpect(view().name("officerReport"));
    }
    @Test
    void testAmountSummaryReportPage() throws Exception{
        mockMvc.perform(get("/reports/amount-summary"))
                .andExpect(status().isOk())
                .andExpect(view().name("loanAmount"));
    }
    @Test
    @DisplayName("Generate Loan report in Pdf format")
    void testGenerateLoanPdfReport() throws Exception {
        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        mockMvc.perform(get("/reports/loan-summary/pdf")
                        .contentType(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk());

        verify(loanService, times(1)).getAllLoans();
    }

    @Test
    @DisplayName("Generate Loan report in Excel format")
    void testGenerateLoanExcelReport() throws Exception {
        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        mockMvc.perform(get("/reports/loan-summary/excel")
                        .contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk());

        verify(loanService, times(1)).getAllLoans();
    }
    @Test
    @DisplayName("Generate User report in Pdf format")
    void testGenerateUserPdfReport() throws Exception {
        when(userService.getAllUser()).thenReturn(List.of(user));

        mockMvc.perform(get("/reports/user-summary/pdf")
                        .contentType(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAllUser();
    }

    @Test
    @DisplayName("Generate User report in Excel format")
    void testGenerateUserExcelReport() throws Exception {
        when(userService.getAllUser()).thenReturn(List.of(user));

        mockMvc.perform(get("/reports/user-summary/excel")
                        .contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAllUser();
    }
    @Test
    @DisplayName("Generate Officer report in Pdf format")
    void testGenerateOfficerPdfReport() throws Exception {
        when(officerService.getAllOfficer()).thenReturn(List.of(officer));

        mockMvc.perform(get("/reports/officer-summary/pdf")
                        .contentType(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk());
        verify(officerService, times(1)).getAllOfficer();
    }
    @Test
    @DisplayName("Generate Officer report in Excel format")
    void testGenerateOfficerExcelReport() throws Exception {
        when(officerService.getAllOfficer()).thenReturn(List.of(officer));

        mockMvc.perform(get("/reports/officer-summary/excel")
                        .contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk());
        verify(officerService, times(1)).getAllOfficer();
    }
    @Test
    @DisplayName("Generate amount summary report in Pdf format")
    void testGenerateAmountSummaryPdfReport() throws Exception {
        when(loanService.getLoanAmountSummary()).thenReturn(List.of(loanSummaryDto1,loanSummaryDto2));

        mockMvc.perform(get("/reports/amount-summary/pdf")
                        .contentType(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk());
        verify(loanService, times(1)).getLoanAmountSummary();
    }
    @Test
    @DisplayName("Generate amount summary report in Excel format")
    void testGenerateAmountSummaryExcelReport() throws Exception {
        when(loanService.getLoanAmountSummary()).thenReturn(List.of(loanSummaryDto1,loanSummaryDto2));

        mockMvc.perform(get("/reports/amount-summary/excel")
                        .contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk());
        verify(loanService, times(1)).getLoanAmountSummary();
    }
}