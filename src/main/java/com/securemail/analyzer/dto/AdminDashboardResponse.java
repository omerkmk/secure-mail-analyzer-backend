package com.securemail.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalAnalyses;
    private Map<String, Long> riskLevelDistribution;
    private List<RiskTypeStatResponse> topRiskTypes;
}
