package com.example.loan.controller;

import com.example.loan.entity.Loan;
import com.example.loan.entity.Officer;
import com.example.loan.repository.OfficerRepository;
import com.example.loan.service.AuthService;
import com.example.loan.service.LoanService;
import com.example.loan.service.OfficerService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/officer")
public class OfficerController {
    private static final Logger logger= LoggerFactory.getLogger(OfficerController.class);
    @Autowired
    private OfficerRepository officerRepository;
    @Autowired
    private OfficerService officerService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LoanService loanService;
    @Autowired
    private AuthService authService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String showOfficerPage(){
        return "officerLogin";
    }
    @GetMapping("/dash")
    public String dashboardPage(){
        return "officerDashboard";
    }
    @GetMapping("/addOfficerPage")
    public String addOfficerPage(){
        return "addOfficer";
    }

    @PostMapping("/addOfficerUser")
    public String addOfficer(@ModelAttribute Officer officer, Model model) {
        if (officerRepository.findByUsername(officer.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "Username already exists.");
            return "addOfficer";
        }
        officer.setPassword(passwordEncoder.encode(officer.getPassword()));
        officerRepository.save(officer);
        model.addAttribute("successMessage","Added officer successfully");
        return "redirect:/officer/login";
    }
    @PostMapping("/login")
    public String officerLogin(@RequestParam("username") String username,@RequestParam("password") String password,Model model){
        Officer officer=officerService.validateLogin(username,password);
        if(officer!=null){
            model.addAttribute("officer", officer);
            model.addAttribute("successMessage","Officer login successfully");
            return "officerDashboard";
        }
        model.addAttribute("errorMessage","Invalid username or password");
        return "officerLogin";
    }
    @GetMapping("/viewOfficerPage")
    public String viewOfficer(Model model){
        List<Officer> officers=officerService.getAllOfficer();
        logger.debug("Get all officer are "+officers);
        model.addAttribute("officer",officers);
        return "viewOfficer";
    }
    @GetMapping("/updateOfficer/{id}")
    public String showUpdateForm(@PathVariable("id") Long id,Model model){
        logger.info("Given id for update officer: "+id);
        Officer officer=officerService.getOfficerById(id);
        logger.debug("Get officer details using id: "+id+" are "+officer);
        model.addAttribute("officer",officer);
        return "updateOfficer";
    }
    @PostMapping("/updateOfficer/{id}")
    public String showUpdateForm(@PathVariable("id") Long id,Model model,@ModelAttribute("officer") Officer officer){
        officerService.updateOfficer(id,officer);
        return "redirect:/officer/viewOfficerPage";
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOfficer(@PathVariable Long id){
        logger.info("Given id for delete officer: "+id);
        try{
            officerService.deleteOfficerById(id);
            return ResponseEntity.ok("Officer deleted successfully");
        }
        catch (EntityNotFoundException e){
            logger.error("Error: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/pending")
    public String viewPendingLoans(@RequestParam String username, Model model) {
        logger.info("Fetched username: "+username);
        Officer officer = officerRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Officer not found"));
        List<Loan> pendingLoans = loanService.findLoansByAssignedOfficerAndStatus(officer, "PENDING_ADMIN");
        logger.debug("Get assigned officer and status for loan are: "+pendingLoans);
        model.addAttribute("officer",officer);
        model.addAttribute("pendingLoans", pendingLoans);
        return "pendingLoans";
    }
    @PostMapping("/decision")
    public String processDecision(@RequestParam UUID loanId,@RequestParam String username,@RequestParam String decision,@RequestParam(required = false) String remarks){
        loanService.processOfficerDecision(loanId,decision,remarks);
        return "redirect:/officer/pending?username="+username;
    }
}
