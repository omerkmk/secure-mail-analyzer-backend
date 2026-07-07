package com.securemail.analyzer.controller;

import com.securemail.analyzer.dto.AnalysisRequest;
import com.securemail.analyzer.dto.AnalysisResponse;
import com.securemail.analyzer.service.AnalysisService;
import com.securemail.analyzer.service.LinkAnalysisService;
import com.securemail.analyzer.service.MailAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final MailAnalysisService mailAnalysisService;
    private final LinkAnalysisService linkAnalysisService;
    private final AnalysisService analysisService;

    @PostMapping("/mail")
    public ResponseEntity<AnalysisResponse> analyzeMail(
            @Valid @RequestBody AnalysisRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        AnalysisResponse response = mailAnalysisService.analyzeMail(request, userEmail);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/link")
    public ResponseEntity<AnalysisResponse> analyzeLink(
            @Valid @RequestBody AnalysisRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        AnalysisResponse response = linkAnalysisService.analyzeLink(request, userEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResponse>> getHistory(Authentication authentication) {
        String userEmail = authentication.getName();

        List<AnalysisResponse> history = analysisService.getHistory(userEmail);

        return ResponseEntity.ok(history);
    }
}