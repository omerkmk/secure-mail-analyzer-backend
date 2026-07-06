package com.securemail.analyzer.dto;

import com.securemail.analyzer.enums.InputType;
import com.securemail.analyzer.enums.RiskLevel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponse {

    private Long id;

    private InputType inputType;

    private String originalInput;

    private RiskLevel riskLevel;

    private int score;

    private String summary;

    private List<RiskFindingResponse> findings;

    private LocalDateTime createdAt;
}