package com.example.loan.controller;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.configuration.JWTAuthenticationFilter;
import com.example.loan.entity.AuthRequest;
import com.example.loan.entity.PasswordUpdateRequest;
import com.example.loan.entity.User;
import com.example.loan.repository.UserRepository;
import com.example.loan.service.AuthService;
import com.example.loan.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JWTAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private JWTTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Display home page")
    void testHomePage() throws Exception {
        mockMvc.perform(get("/auth/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("homePage"));
    }

    @Test
    @DisplayName("Display Admin page")
    void testShowAdminPage() throws Exception {
        mockMvc.perform(get("/auth/login/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminLogin"));
    }

    @Test
    @DisplayName("Display customer dashboard")
    void testShowCustomerDashboard() throws Exception {
        mockMvc.perform(get("/auth/customer/dash"))
                .andExpect(status().isOk())
                .andExpect(view().name("customerDashboard"));
    }

    @Test
    @DisplayName("Display admin dashboard")
    void testShowAdminDashboard() throws Exception {
        mockMvc.perform(get("/auth/admin/dash"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminDashboard"));
    }

    @Test
    @DisplayName("Logout Customer successfully")
    void testShowLoginPage_WithLogout() throws Exception {
        mockMvc.perform(get("/auth/login/customer").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("logoutMessage"))
                .andExpect(model().attribute("logoutMessage", "Logged out successfully"))
                .andExpect(view().name("customerLogin"));
    }

    @Test
    @DisplayName("Display customer registration page")
    void testShowRegisterPage() throws Exception {
        mockMvc.perform(get("/auth/registerUser"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("Customer registered successfully")
    void testRegisterUser_Success() throws Exception {
        // Given
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");

        // When & Then
        mockMvc.perform(post("/auth/registerUser")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", user.getUsername())
                        .param("email", user.getEmail())
                        .param("password", user.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login/customer"));
    }
    @Test
    @DisplayName("Admin logined successfully")
    void testAdminLogin_Success() throws Exception {
        // Given
        String username = "admin";
        String password = "adminPass";

        when(userService.authenticateLogin(username, password)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/login/admin")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(model().attribute("successMessage", "Admin login successfully"))
                .andExpect(view().name("adminDashboard"));
    }

    @Test
    @DisplayName("Invalid credentials for admin login")
    void testAdminLogin_Failure() throws Exception {
        // Given
        String username = "admin";
        String password = "wrongPass";

        when(userService.authenticateLogin(username, password)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/login/admin")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "Invalid username or password"))
                .andExpect(view().name("adminLogin"));
    }

    @Test
    @DisplayName("Update customer information successfully")
    void testUpdateUser_Success() throws Exception {
        // Given
        User user = new User();
        user.setUsername("testUser");

        when(userService.updateUser(any(User.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/update-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User information updated successfully!"));
    }

    @Test
    @DisplayName("Error while updating customer information")
    void testUpdateUser_Failure() throws Exception {
        // Given
        when(userService.updateUser(any(User.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/update-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error updating user information."));
    }
    @Test
    @DisplayName("Successful Authentication")
    void testAuthenticate_Success() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testUser");
        authRequest.setPassword("password123");
        String token = "mockedToken";
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authService.authenticateUser(anyString(),anyString())).thenReturn(token);

        // When & Act
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Failed Authentication")
    void testAuthenticate_Failure() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("wrongUser");
        authRequest.setPassword("wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new AuthenticationException("Invalid username or password") {
        });

        // When & Act
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrongUser\",\"password\":\"wrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Updated customer password successfully")
    void testUpdatePassword_Success() throws Exception {
        // Given
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setUsername("testUser");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");
        userService.updatePassword(request.getUsername(), request.getNewPassword(), request.getConfirmPassword());

        // When & Act
        mockMvc.perform(post("/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\",\"newPassword\":\"newPass\",\"confirmPassword\":\"newPass\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated password successfully"));
    }

    @Test
    @DisplayName("Failed to update customer password")
    void testUpdatePassword_Failure() throws Exception {
        // Given
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setUsername("testUser");
        request.setNewPassword("newPass");
        request.setConfirmPassword("diffPass");
        doThrow(new RuntimeException("Passwords do not match"))
                .when(userService).updatePassword(request.getUsername(), request.getNewPassword(), request.getConfirmPassword());

        // When & Act
        mockMvc.perform(post("/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\",\"newPassword\":\"newPass\",\"confirmPassword\":\"diffPass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Passwords do not match"));
    }

    @Test
    @DisplayName("Get Logout message")
    void testLogout() throws Exception {
        mockMvc.perform(get("/auth/logout")
                        .sessionAttr("user","testUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login/customer?logout=true"));
    }

    @Test
    @DisplayName("View Customer details without search details")
    void testViewCustomers_NoSearch() throws Exception {
        // Given
        Page<User> mockPage = new PageImpl<>(Collections.emptyList());
        when(userService.getAllCustomer(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/auth/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("viewCustomer"))
                .andExpect(model().attributeExists("customerPage", "currentPage", "totalPages"));
    }

    @Test
    @DisplayName("Search customer details")
    void testViewCustomers_WithSearch() throws Exception {
        // Given
        String searchType = "email";
        String searchValue = "test@example.com";
        Page<User> mockPage = new PageImpl<>(Collections.emptyList());
        when(userService.searchCustomers(eq(searchType), eq(searchValue), any(Pageable.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/auth/customers")
                        .param("searchType", searchType)
                        .param("searchValue", searchValue))
                .andExpect(status().isOk())

                .andExpect(view().name("viewCustomer"))
                .andExpect(model().attributeExists("customerPage", "currentPage", "totalPages", "searchType", "searchValue"));
    }
}