package com.example.loan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loan.entity.Officer;

@Repository
public interface OfficerRepository extends JpaRepository<Officer,Long> {
    Optional<Officer> findByUsername(String username);

}