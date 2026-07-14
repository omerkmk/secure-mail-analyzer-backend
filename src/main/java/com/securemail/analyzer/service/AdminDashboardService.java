package com.securemail.analyzer.service;

import com.securemail.analyzer.dto.AdminDashboardResponse;
import com.securemail.analyzer.dto.RiskTypeStatResponse;
import com.securemail.analyzer.enums.RiskLevel;
import com.securemail.analyzer.enums.RiskType;
import com.securemail.analyzer.repository.AnalysisRepository;
import com.securemail.analyzer.repository.RiskFindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final int TOP_RISK_TYPE_LIMIT = 5;

    private final AnalysisRepository analysisRepository;
    private final RiskFindingRepository riskFindingRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalAnalyses = analysisRepository.count();
        Map<String, Long> riskLevelDistribution = buildRiskLevelDistribution();
        List<RiskTypeStatResponse> topRiskTypes = buildTopRiskTypes();

        return AdminDashboardResponse.builder()
                .totalAnalyses(totalAnalyses)
                .riskLevelDistribution(riskLevelDistribution)
                .topRiskTypes(topRiskTypes)
                .build();
    }

    private Map<String, Long> buildRiskLevelDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        Arrays.stream(RiskLevel.values())
                .forEach(level -> distribution.put(level.name(), 0L));

        for (Object[] row : analysisRepository.countGroupedByRiskLevel()) {
            RiskLevel riskLevel = (RiskLevel) row[0];
            Long count = (Long) row[1];
            distribution.put(riskLevel.name(), count);
        }

        return distribution;
    }

    private List<RiskTypeStatResponse> buildTopRiskTypes() {
        return riskFindingRepository
                .findTopRiskTypes(PageRequest.of(0, TOP_RISK_TYPE_LIMIT))
                .stream()
                .map(row -> RiskTypeStatResponse.builder()
                        .riskType((RiskType) row[0])
                        .count((Long) row[1])
                        .build())
                .toList();
    }
}
