package com.example.loan.repository;

import com.example.loan.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNo(Long phoneNo);
    Optional<User> findByAadharcard(String aadharcard);
    Optional<User> findByPancard(String pancard);

    Page<User> findByEmailContaining(String searchValue, Pageable pageable);

    Page<User> findByPancardContaining(String searchValue, Pageable pageable);

    Page<User> findByAadharcardContaining(String searchValue, Pageable pageable);
}