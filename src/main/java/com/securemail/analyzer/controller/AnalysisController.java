package com.securemail.analyzer.controller;

import com.securemail.analyzer.dto.AnalysisRequest;
import com.securemail.analyzer.dto.AnalysisResponse;
import com.securemail.analyzer.service.MailAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final MailAnalysisService mailAnalysisService;

    @PostMapping("/mail")
    public ResponseEntity<AnalysisResponse> analyzeMail(
            @Valid @RequestBody AnalysisRequest request,
            Authentication authentication
    ) {
        AnalysisResponse response = mailAnalysisService.analyzeMail(
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
}