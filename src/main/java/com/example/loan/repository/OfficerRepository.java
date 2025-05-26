package com.example.loan.repository;

import com.example.loan.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer,Long> {
    Optional<Officer> findByUsername(String username);

}