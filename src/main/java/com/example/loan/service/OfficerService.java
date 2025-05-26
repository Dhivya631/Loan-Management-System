package com.example.loan.service;

import com.example.loan.entity.Officer;
import com.example.loan.repository.OfficerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfficerService {
    private static final Logger logger= LoggerFactory.getLogger(OfficerService.class);
    @Autowired
    private OfficerRepository officerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Cacheable("officers")
    public List<Officer> getAllOfficer() {
        logger.info("Fetching all officers from the database");
        long startTime=System.currentTimeMillis();
        List<Officer> listOfficer=officerRepository.findAll();
        long endTime=System.currentTimeMillis();
        logger.info("Time taken to fetch officers: {}ms",endTime-startTime);
        return listOfficer;
    }
    public Officer getOfficerById(Long id) {
        return officerRepository.findById(id).orElseThrow(() -> new RuntimeException("Officer not found"));
    }
    public Officer updateOfficer(Long id, Officer officerDetails) {
        Optional<Officer> optionalOfficer = officerRepository.findById(id);
        if (optionalOfficer.isPresent()) {
            Officer officer = optionalOfficer.get();
            officer.setName(officerDetails.getName());
            officer.setEmail(officerDetails.getEmail());
            officer.setPhoneNo(officerDetails.getPhoneNo());
            officer.setUsername(officerDetails.getUsername());
            return officerRepository.save(officer);
        } else {
            return null;
        }
    }

    public void deleteOfficerById(Long id) {
        if(officerRepository.findById(id).isPresent()){
            officerRepository.deleteById(id);
        }
        else{
            throw new EntityNotFoundException("Officer with id: "+id+" not found");
        }
    }

    public Officer validateLogin(String username, String password) {
        Optional<Officer> officer=officerRepository.findByUsername(username);
        if(officer.isPresent() && passwordEncoder.matches(password,officer.get().getPassword())){
            return officer.get();
        }
        return null;
    }

    public Optional<Officer> findByUsername(String username) {
        return officerRepository.findByUsername(username);
    }
}

