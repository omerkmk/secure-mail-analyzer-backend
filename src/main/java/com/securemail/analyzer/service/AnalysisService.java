package com.securemail.analyzer.service;

import com.securemail.analyzer.dto.AnalysisResponse;
import com.securemail.analyzer.dto.RiskFindingResponse;
import com.securemail.analyzer.entity.Analysis;
import com.securemail.analyzer.entity.RiskFinding;
import com.securemail.analyzer.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    @Transactional(readOnly = true)
    public List<AnalysisResponse> getHistory(String userEmail) {
        return analysisRepository.findByUser_EmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysisDetail(Long id, String userEmail) {
        Analysis analysis = analysisRepository
                .findByIdAndUser_Email(id, userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Analiz bulunamadı."
                ));

        return mapToResponse(analysis);
    }

    private AnalysisResponse mapToResponse(Analysis analysis) {
        List<RiskFindingResponse> findings = analysis.getFindings()
                .stream()
                .map(this::mapToRiskFindingResponse)
                .toList();

        return new AnalysisResponse(
                analysis.getId(),
                analysis.getInputType(),
                analysis.getOriginalInput(),
                analysis.getRiskLevel(),
                analysis.getScore(),
                analysis.getSummary(),
                findings,
                analysis.getCreatedAt()
        );
    }

    private RiskFindingResponse mapToRiskFindingResponse(RiskFinding finding) {
        return new RiskFindingResponse(
                finding.getRiskType(),
                finding.getDescription(),
                finding.getScoreImpact()
        );
    }
}