package com.example.loan.service;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.entity.User;
import com.example.loan.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger= LoggerFactory.getLogger(UserService.class);
    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;

    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public boolean authenticateLogin(String username,String password){
        return username.equals(adminUsername) && password.equals(adminPassword);
    }
    public String updatePassword(String username,String newPassword,String confirmPassword) {
        try {
            logger.info("Username: ",username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if(!newPassword.equals(confirmPassword)){
                return "New password and confirm password not matched";
            }

            // Update the new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return "Password updated successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating password";
        }
    }
    public boolean updateUser(User updateuser) {
        Optional<User> existing=userRepository.findByUsername(updateuser.getUsername());
        if(existing.isPresent()){
            User existinguser=existing.get();
            existinguser.setName(updateuser.getName());
            existinguser.setEmail(updateuser.getEmail());
            existinguser.setPhoneNo(updateuser.getPhoneNo());
            userRepository.save(existinguser);
            return true;
        }
        return false;
    }
    public Page<User> getAllCustomer(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> searchCustomers(String searchType, String searchValue, Pageable pageable) {
        switch (searchType) {
            case "email":
                return userRepository.findByEmailContaining(searchValue, pageable);
            case "pancard":
                return userRepository.findByPancardContaining(searchValue, pageable);
            case "aadharcard":
                return userRepository.findByAadharcardContaining(searchValue, pageable);
            default:
                return getAllCustomer(pageable);
        }
    }
    public List<User> getAllUser() {
        return userRepository.findAll();
    }
}
