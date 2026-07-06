package com.securemail.analyzer.repository;

import com.securemail.analyzer.entity.RiskFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskFindingRepository extends JpaRepository<RiskFinding, Long> {

    List<RiskFinding> findByAnalysisId(Long analysisId);
}