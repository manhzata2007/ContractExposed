package com.contractexposed.backend.repository;

import com.contractexposed.backend.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Page<Contract> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Contract> findByStatusOrderByCreatedAtDesc(Contract.ContractStatus status);

    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.analysisReport WHERE c.id = :id")
    Optional<Contract> findByIdWithReport(Long id);

    long countByStatus(Contract.ContractStatus status);
}
