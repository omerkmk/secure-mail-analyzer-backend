package com.securemail.analyzer.repository;

import com.securemail.analyzer.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Analysis> findByIdAndUser_Email(Long id, String email);

    @Query("SELECT a.riskLevel, COUNT(a) FROM Analysis a GROUP BY a.riskLevel")
    List<Object[]> countGroupedByRiskLevel();
}