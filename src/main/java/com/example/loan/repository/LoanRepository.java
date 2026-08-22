package com.example.loan.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.loan.dto.LoanSummaryDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.Officer;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByStatus(String status);

    @Query("SELECT l FROM Loan l WHERE l.user.username = :username")
    List<Loan> findByUserUsername(@Param("username") String username);

    @Query("SELECT NEW com.example.loan.dto.LoanSummaryDto(l.user.name, SUM(l.amount)) FROM Loan l GROUP BY l.user.name")
    List<LoanSummaryDto> getLoanAmountSummary();

    List<Loan> findByAssignedOfficerAndStatus(Officer officer, String status);

    List<Loan> findByUserUsernameAndStatus(String username, String approved);

}
