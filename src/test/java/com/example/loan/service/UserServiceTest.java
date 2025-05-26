package com.example.loan.service;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.entity.User;
import com.example.loan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JWTTokenProvider jwtTokenProvider;
    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "adminUsername", "admin");
        ReflectionTestUtils.setField(userService, "adminPassword", "admin123");
        user = new User();
        user.setUsername("testuser");
        user.setPassword("oldpassword");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPhoneNo(1234567890L);
        user.setPancard("189208191F");
        user.setAadharcard("9898177289212");
    }

    @Test
    @DisplayName("Updated customer successfully")
    void testUpdatePassword_Success() {
        // Arrange
        String newPassword = "newpassword";
        String confirmPassword = "newpassword";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedpassword");

        // Act
        String result = userService.updatePassword("testuser", newPassword, confirmPassword);

        // Assert
        assertEquals("Password updated successfully!", result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Update customer password - User not found")
    void testUpdatePassword_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        // Act
        String result = userService.updatePassword("invaliduser", "newpass", "newpass");

        // Assert
        assertEquals("Error updating password", result);
    }

    @Test
    @DisplayName("Update Password - Confirm password mismatched")
    void testUpdatePassword_ConfirmPasswordMismatch() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        String result = userService.updatePassword("testuser", "newpass", "wrongpass");

        // Assert
        assertEquals("New password and confirm password not matched", result);
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("Updated user information successfully")
    void testUpdateUser_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUsername("testuser");
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPhoneNo(9876543210L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = userService.updateUser(updatedUser);

        // Assert
        assertTrue(result);
        assertEquals("Updated Name", user.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Update user information - user not found")
    void testUpdateUser_UserNotFound() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUsername("nonexistentuser");

        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act
        boolean result = userService.updateUser(updatedUser);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get all customer details")
    void testGetAllCustomer() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<User> result = userService.getAllCustomer(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Search customer using email")
    void testSearchCustomers_ByEmail() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findByEmailContaining("test@example.com", pageable)).thenReturn(mockPage);

        // Act
        Page<User> result = userService.searchCustomers("email", "test@example.com", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByEmailContaining("test@example.com", pageable);
    }
    @Test
    @DisplayName("Search customer using pancard")
    void testSearchCustomers_ByPancard() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findByPancardContaining("189208191F", pageable)).thenReturn(mockPage);

        // Act
        Page<User> result = userService.searchCustomers("pancard", "189208191F", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByPancardContaining("189208191F", pageable);
    }
    @Test
    @DisplayName("Search customer using aadharcard")
    void testSearchCustomers_ByAadharcard() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findByAadharcardContaining("9898177289212", pageable)).thenReturn(mockPage);

        // Act
        Page<User> result = userService.searchCustomers("aadharcard", "9898177289212", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByAadharcardContaining("9898177289212", pageable);
    }
    @Test
    @DisplayName("Search customer - By default")
    void testSearchCustomers_ByDefault() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<User> mockPage = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        userRepository.findAll(pageable);

        // Assert
        verify(userRepository, times(1)).findAll(pageable);
    }
    @Test
    @DisplayName("Get all user information")
    void testGetAllUser() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Act

        List<User> users = userService.getAllUser();

        // Assert
        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAll();
    }
    @Test
    @DisplayName("Successful authentication for login")
    void testAuthenticateLogin_Success() {
        // Act
        boolean result = userService.authenticateLogin("admin", "admin123");
        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Invalid username authentication login")
    void testAuthenticateLogin_InvalidUsername() {
        // Act
        boolean result = userService.authenticateLogin("wrongUser", "admin123");
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Invalid password authentication login")
    void testAuthenticateLogin_InvalidPassword() {
        // Act
        boolean result = userService.authenticateLogin("admin", "wrongPassword");
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Invalid both username and password")
    void testAuthenticateLogin_InvalidCredentials() {
        // Act
        boolean result = userService.authenticateLogin("invalidUser", "invalidPassword");
        // Assert
        assertFalse(result);
    }
}