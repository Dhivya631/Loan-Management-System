package com.example.loan.controller;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.configuration.JWTAuthenticationFilter;
import com.example.loan.entity.Loan;
import com.example.loan.entity.Officer;
import com.example.loan.repository.OfficerRepository;
import com.example.loan.service.AuthService;
import com.example.loan.service.LoanService;
import com.example.loan.service.OfficerService;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = OfficerController.class,excludeAutoConfiguration = SecurityAutoConfiguration.class)
class OfficerControllerTest {
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
    private AuthService authService;
    @MockitoBean
    private OfficerService officerService;
    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private OfficerController officerController;
    private Officer officer;
    private Loan loan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(officerController).build();
        officer = new Officer();
        officer.setId(1L);
        officer.setUsername("officerUser");
        officer.setPassword("password123");

        loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setStatus("PENDING_ADMIN");
        loan.setAssignedOfficer(officer);
    }
    @Test
    @DisplayName("Officer LoginPage")
    void testOfficerLogin() throws Exception{
        mockMvc.perform(get("/officer/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("officerLogin"));
    }
    @Test
    @DisplayName("Officer Dashboard")
    void testOfficerDashboard() throws Exception{
        mockMvc.perform(get("/officer/dash")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("officerDashboard"));
    }
    @Test
    @DisplayName("Add OfficerPage")
    void testAddOfficerPage() throws Exception{
        mockMvc.perform(get("/officer/addOfficerPage")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("addOfficer"));
    }
    @Test
    @DisplayName("Officer Logined successfully")
    void testvalidateLoginPage_Success() throws Exception{
        //when
        when(officerService.validateLogin(officer.getUsername(),officer.getPassword())).thenReturn(officer);
        mockMvc.perform(post("/officer/login")
                        .param("username",officer.getUsername())
                        .param("password",officer.getPassword())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successMessage"))
                .andExpect(model().attribute("successMessage","Officer login successfully"))
                .andExpect(view().name("officerDashboard"));
    }
    @Test
    @DisplayName("Invalid login credentials for officer")
    void testvalidateLoginPage_Failure() throws Exception{
        //when
        when(officerService.validateLogin(officer.getUsername(),officer.getPassword())).thenReturn(null);
        mockMvc.perform(post("/officer/login")
                        .param("username",officer.getUsername())
                        .param("password",officer.getPassword())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage","Invalid username or password"))
                .andExpect(view().name("officerLogin"));
    }
    @Test
    @DisplayName("Added Officer details successfully")
    void testAddOfficer_Success() throws Exception {
        // when
        when(officerRepository.findByUsername(officer.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(officerRepository.save(any(Officer.class))).thenReturn(officer);

        mockMvc.perform(post("/officer/addOfficerUser")
                        .param("username", "officerUser")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/login"));
    }

    @Test
    @DisplayName("Username already exists in AddOfficer")
    void testAddOfficer_UsernameExists() throws Exception {
        // Given
        when(officerRepository.findByUsername(officer.getUsername())).thenReturn(Optional.of(officer));

        // When & Then
        mockMvc.perform(post("/officer/addOfficerUser")
                        .param("username", "officerUser")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("addOfficer"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("View all Loan officer")
    void testViewOfficer() throws Exception {
        // Given
        List<Officer> officers = List.of(officer);
        when(officerService.getAllOfficer()).thenReturn(officers);

        // When & Then
        mockMvc.perform(get("/officer/viewOfficerPage"))
                .andExpect(status().isOk())
                .andExpect(view().name("viewOfficer"))
                .andExpect(model().attributeExists("officer"));
    }

    @Test
    @DisplayName("Get Update officer page")
    void testUpdateOfficer_Success() throws Exception {
        // Given
        when(officerService.getOfficerById(1L)).thenReturn(officer);

        // When & Then
        mockMvc.perform(get("/officer/updateOfficer/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("updateOfficer"))
                .andExpect(model().attributeExists("officer"));
    }
    @Test
    @DisplayName("Updated officer successfully")
    void testpostUpdateOfficer_Success() throws Exception {
        // Given
        officerService.updateOfficer(1L,officer);

        // When & Then
        mockMvc.perform(post("/officer/updateOfficer/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/viewOfficerPage"));
    }

    @Test
    @DisplayName("Deleted officer successfully")
    void testDeleteOfficer_Success() throws Exception {
        // Given
        doNothing().when(officerService).deleteOfficerById(1L);

        // When & Then
        mockMvc.perform(delete("/officer/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Officer deleted successfully"));
    }

    @Test
    @DisplayName("In DeleteOfficer - Id not found")
    void testDeleteOfficer_NotFound() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Officer not found")).when(officerService).deleteOfficerById(1L);

        // When & Then
        mockMvc.perform(delete("/officer/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Officer not found"));
    }

    @Test
    @DisplayName("View Pending Loans")
    void testViewPendingLoans_Success() throws Exception {
        // Given
        List<Loan> pendingLoans = List.of(loan);
        when(officerRepository.findByUsername("officerUser")).thenReturn(Optional.of(officer));
        when(loanService.findLoansByAssignedOfficerAndStatus(officer, "PENDING_ADMIN"))
                .thenReturn(pendingLoans);

        // When & Then
        mockMvc.perform(get("/officer/pending").param("username", "officerUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("pendingLoans"))
                .andExpect(model().attributeExists("officer", "pendingLoans"));
    }
    @Test
    @DisplayName("In PendingLoans - Officer not found")
    void testViewPendingLoans_UnSuccessful() throws Exception {
        when(officerRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            officerRepository.findByUsername("nonExistingUser")
                    .orElseThrow(() -> new RuntimeException("Officer not found"));
        });

        assertEquals("Officer not found", exception.getMessage());

    }
    @Test
    @DisplayName("Officer process decision successfully")
    void testProcessDecision_Success() throws Exception {
        // Given
        UUID loanId = loan.getId();
        doNothing().when(loanService).processOfficerDecision(loanId, "APPROVED", "Approved by officer");

        // When & Then
        mockMvc.perform(post("/officer/decision")
                        .param("loanId", loanId.toString())
                        .param("username", "officerUser")
                        .param("decision", "APPROVED")
                        .param("remarks", "Approved by officer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/pending?username=officerUser"));
    }

}