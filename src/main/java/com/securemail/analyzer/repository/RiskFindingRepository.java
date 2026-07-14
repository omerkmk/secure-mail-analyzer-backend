package com.securemail.analyzer.repository;

import com.securemail.analyzer.entity.RiskFinding;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RiskFindingRepository extends JpaRepository<RiskFinding, Long> {

    List<RiskFinding> findByAnalysisId(Long analysisId);

    @Query("SELECT r.riskType, COUNT(r) FROM RiskFinding r GROUP BY r.riskType ORDER BY COUNT(r) DESC")
    List<Object[]> findTopRiskTypes(Pageable pageable);
}