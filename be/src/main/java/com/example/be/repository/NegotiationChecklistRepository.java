package com.contractexposed.backend.repository;

import com.contractexposed.backend.entity.NegotiationChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationChecklistRepository extends JpaRepository<NegotiationChecklistItem, Long> {

    List<NegotiationChecklistItem> findByReportIdOrderBySortOrderAsc(Long reportId);

    long countByReportIdAndIsCompleted(Long reportId, Boolean isCompleted);
}
