package com.securemail.analyzer.dto;

import com.securemail.analyzer.enums.RiskType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskFindingResponse {

    private RiskType riskType;

    private String description;

    private int scoreImpact;
}