package com.example.loan.service;

import com.example.loan.entity.Officer;
import com.example.loan.repository.OfficerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficerServiceTest {

    @Mock
    private OfficerRepository officerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OfficerService officerService;

    private Officer officer;

    @BeforeEach
    void setUp() {
        officer = new Officer();
        officer.setId(1L);
        officer.setName("John");
        officer.setEmail("john.doe@example.com");
        officer.setPhoneNo(1234567890L);
        officer.setUsername("john");
        officer.setPassword("encodedPassword");
    }

    @Test
    @DisplayName("Get all officer")
    void testGetAllOfficer() {
        when(officerRepository.findAll()).thenReturn(Arrays.asList(officer));

        List<Officer> result = officerService.getAllOfficer();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    @DisplayName("Get officer using id")
    void testGetOfficerById_Found() {
        when(officerRepository.findById(1L)).thenReturn(Optional.of(officer));

        Officer result = officerService.getOfficerById(1L);

        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    @DisplayName("Get Officer using id - ID not found")
    void testGetOfficerById_NotFound() {
        when(officerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> officerService.getOfficerById(2L));
    }

    @Test
    @DisplayName("Updated officer successfully")
    void testUpdateOfficer_Found() {
        Officer updatedOfficer = new Officer();
        updatedOfficer.setName("Janu");
        updatedOfficer.setEmail("janu@example.com");
        updatedOfficer.setPhoneNo(987654321L);
        updatedOfficer.setUsername("janu");

        when(officerRepository.findById(1L)).thenReturn(Optional.of(officer));
        when(officerRepository.save(any(Officer.class))).thenReturn(updatedOfficer);

        Officer result = officerService.updateOfficer(1L, updatedOfficer);

        assertNotNull(result);
        assertEquals("Janu", result.getName());
    }

    @Test
    @DisplayName("Update officer - ID not found")
    void testUpdateOfficer_NotFound() {
        when(officerRepository.findById(2L)).thenReturn(Optional.empty());

        Officer result = officerService.updateOfficer(2L, officer);

        assertNull(result);
    }

    @Test
    @DisplayName("Delete officer using id")
    void testDeleteOfficerById_Found() {
        when(officerRepository.findById(1L)).thenReturn(Optional.of(officer));
        doNothing().when(officerRepository).deleteById(1L);

        assertDoesNotThrow(() -> officerService.deleteOfficerById(1L));
        verify(officerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete officer - ID not found")
    void testDeleteOfficerById_NotFound() {
        when(officerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> officerService.deleteOfficerById(2L));
    }

    @Test
    @DisplayName("Validate login credentials")
    void testValidateLogin_ValidCredentials() {
        when(officerRepository.findByUsername("john")).thenReturn(Optional.of(officer));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        Officer result = officerService.validateLogin("john", "password");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
    }

    @Test
    @DisplayName("Invalid login credentials")
    void testValidateLogin_InvalidCredentials() {
        when(officerRepository.findByUsername("john")).thenReturn(Optional.of(officer));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Officer result = officerService.validateLogin("john", "wrongPassword");

        assertNull(result);
    }

    @Test
    @DisplayName("Validate Login - Officer not found")
    void testValidateLogin_OfficerNotFound() {
        when(officerRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        Officer result = officerService.validateLogin("unknownUser", "password");

        assertNull(result);
    }

    @Test
    @DisplayName("Find username for officer")
    void testFindByUsername_Found() {
        when(officerRepository.findByUsername("john")).thenReturn(Optional.of(officer));

        Optional<Officer> result = officerService.findByUsername("john");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    @DisplayName("Username not found")
    void testFindByUsername_NotFound() {
        when(officerRepository.findByUsername("unknownUser")).thenReturn(Optional.
                empty());

        Optional<Officer> result = officerService.findByUsername("unknownUser");

        assertFalse(result.isPresent());
    }
}