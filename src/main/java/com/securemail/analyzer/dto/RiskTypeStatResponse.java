package com.securemail.analyzer.dto;

import com.securemail.analyzer.enums.RiskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskTypeStatResponse {

    private RiskType riskType;
    private long count;
}
