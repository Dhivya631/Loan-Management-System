package com.example.loan.controller;

import com.example.loan.configuration.JWTTokenProvider;
import com.example.loan.entity.AuthRequest;
import com.example.loan.entity.AuthResponse;
import com.example.loan.entity.PasswordUpdateRequest;
import com.example.loan.entity.User;
import com.example.loan.repository.UserRepository;
import com.example.loan.service.AuthService;
import com.example.loan.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger= LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/home")
    public String homePage(){
        return "homePage";
    }

    @GetMapping("/login/admin")
    public String showAdminPage(){
        return "adminLogin";
    }

    @GetMapping("/customer/dash")
    public String showDashPage(){
        return "customerDashboard";
    }

    @GetMapping("/admin/dash")
    public String showadminDashPage(){
        return "adminDashboard";
    }


    @GetMapping("/login/customer")
    public String showLoginPage(@RequestParam(value="logout",required = false) String logout, Model model) {
        if("true".equals(logout)){
            model.addAttribute("logoutMessage","Logged out successfully");
        }
        return "customerLogin";
    }

    @GetMapping("/registerUser")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute User user, Model model) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "Username already exists.");
            return "register";
        }
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            model.addAttribute("errorMessage", "Email already exists");
            return "register";
        }
        if(userRepository.findByPhoneNo(user.getPhoneNo()).isPresent()){
            model.addAttribute("errorMessage","PhoneNo already exists");
            return "register";
        }
        if(userRepository.findByAadharcard(user.getAadharcard()).isPresent()){
            model.addAttribute("errorMessage","Aadharcard already exists");
            return "register";
        }
        if (userRepository.findByPancard(user.getPancard()).isPresent()){
            model.addAttribute("errorMessage","Pancard already exists");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("CUSTOMER");
        userRepository.save(user);
        model.addAttribute("successMessage","User registered successfully");
        return "redirect:/auth/login/customer";
    }
    @PostMapping("/login/admin")
    public String adminLogin(@RequestParam("username") String username,@RequestParam("password") String password,Model model){
        logger.info("Given username: "+username+", password: "+password);
        if(userService.authenticateLogin(username,password)){
            model.addAttribute("successMessage","Admin login successfully");
            return "adminDashboard";
        }
        model.addAttribute("errorMessage","Invalid username or password");
        return "adminLogin";
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = authService.authenticateUser(authRequest.getUsername(), authRequest.getPassword());
            session.setAttribute("username",authRequest.getUsername());
            session.setAttribute("password",authRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(token,session.getId()));
        }
        catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null,"Invalid username or password"));
        }
    }
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        try{
            userService.updatePassword(request.getUsername(),request.getNewPassword(),request.getConfirmPassword());
            return ResponseEntity.ok("Updated password successfully");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/update-user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        boolean updated = userService.updateUser(user);
        if (updated) {
            logger.info("User information updated successfully!");
            return ResponseEntity.ok("User information updated successfully!");
        } else {
            logger.error("Error updating user information.");
            return ResponseEntity.badRequest().body("Error updating user information.");
        }
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        HttpSession session=request.getSession(false);
        if(session!=null) {
            session.invalidate();
        }
        return "redirect:/auth/login/customer?logout=true";
    }
    @GetMapping("/customers")
    public String viewCustomers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String searchType,
                                @RequestParam(required = false) String searchValue,Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> customerPage;
        if (searchValue != null && !searchValue.isEmpty()) {
            customerPage = userService.searchCustomers(searchType, searchValue, pageable);
        } else {
            customerPage = userService.getAllCustomer(pageable);
        }
        model.addAttribute("customerPage", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchValue", searchValue);
        return "viewCustomer";
    }
}
