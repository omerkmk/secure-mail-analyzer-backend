package com.securemail.analyzer.service;

import com.securemail.analyzer.dto.AnalysisRequest;
import com.securemail.analyzer.dto.AnalysisResponse;
import com.securemail.analyzer.dto.RiskFindingResponse;
import com.securemail.analyzer.entity.Analysis;
import com.securemail.analyzer.entity.RiskFinding;
import com.securemail.analyzer.entity.User;
import com.securemail.analyzer.enums.InputType;
import com.securemail.analyzer.enums.RiskLevel;
import com.securemail.analyzer.enums.RiskType;
import com.securemail.analyzer.repository.AnalysisRepository;
import com.securemail.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MailAnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    public AnalysisResponse analyzeMail(AnalysisRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kullanıcı bulunamadı."
                ));

        String input = request.getInput();
        String normalizedInput = input.toLowerCase(Locale.forLanguageTag("tr"));

        Analysis analysis = Analysis.builder()
                .user(user)
                .inputType(InputType.EMAIL)
                .originalInput(input)
                .riskLevel(RiskLevel.LOW)
                .score(0)
                .summary("Bu e-posta düşük riskli görünüyor.")
                .findings(new ArrayList<>())
                .build();

        List<RiskFinding> findings = new ArrayList<>();
        int score = 0;

        if (containsAny(normalizedInput, "acil", "hemen", "son uyarı", "24 saat", "bugün")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.URGENCY_LANGUAGE,
                    "Mail içinde aciliyet baskısı oluşturan ifadeler bulundu.",
                    20
            ));
            score += 20;
        }

        if (containsAny(normalizedInput, "şifre", "parola", "password", "giriş bilgileri")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.PASSWORD_REQUEST,
                    "Mail içinde kullanıcıdan şifre veya giriş bilgisi istendiği tespit edildi.",
                    35
            ));
            score += 35;
        }

        if (containsAny(normalizedInput, "tc kimlik", "kart bilgisi", "kredi kartı", "iban", "kişisel bilgi")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.PERSONAL_INFO_REQUEST,
                    "Mail içinde kullanıcıdan kişisel veya finansal bilgi istendiği tespit edildi.",
                    35
            ));
            score += 35;
        }

        if (containsAny(normalizedInput, "http://", "https://", "tıklayın", "linke tıkla", "bağlantıya tıklayın")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.FAKE_LINK,
                    "Mail içinde kullanıcıyı bir bağlantıya yönlendiren ifade bulundu.",
                    20
            ));
            score += 20;
        }

        if (containsAny(normalizedInput, "banka", "paypal", "amazon", "netflix", "apple", "google", "microsoft")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.BRAND_IMPERSONATION,
                    "Mail içinde bilinen bir marka veya kurum adı kullanılarak güven algısı oluşturulmuş olabilir.",
                    15
            ));
            score += 15;
        }

        if (containsAny(normalizedInput, "ek dosya", "dosyayı aç", "fatura ektedir", "attachment")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.ATTACHMENT,
                    "Mail içinde ek dosya açmaya yönlendiren ifade bulundu.",
                    20
            ));
            score += 20;
        }

        if (containsAny(normalizedInput, "yazım hatası", "hatalı yazım")) {
            findings.add(createFinding(
                    analysis,
                    RiskType.SPELLING_ERRORS,
                    "Mail içinde yazım hatası şüphesi oluşturabilecek ifadeler bulundu.",
                    10
            ));
            score += 10;
        }

        if (score > 100) {
            score = 100;
        }

        RiskLevel riskLevel = calculateRiskLevel(score);
        String summary = createSummary(riskLevel);

        analysis.setScore(score);
        analysis.setRiskLevel(riskLevel);
        analysis.setSummary(summary);
        analysis.setFindings(findings);

        Analysis savedAnalysis = analysisRepository.save(analysis);

        return toResponse(savedAnalysis);
    }

    private RiskFinding createFinding(Analysis analysis, RiskType riskType, String description, int scoreImpact) {
        return RiskFinding.builder()
                .analysis(analysis)
                .riskType(riskType)
                .description(description)
                .scoreImpact(scoreImpact)
                .build();
    }

    private boolean containsAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private RiskLevel calculateRiskLevel(int score) {
        if (score >= 71) {
            return RiskLevel.HIGH;
        }

        if (score >= 31) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    private String createSummary(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> "Bu e-posta yüksek riskli görünüyor. Kullanıcıyı yanıltmaya veya hassas bilgi almaya yönelik ifadeler içeriyor olabilir.";
            case MEDIUM -> "Bu e-posta orta seviyede riskli görünüyor. Dikkatli incelenmesi gereken bazı risk işaretleri bulundu.";
            case LOW -> "Bu e-posta düşük riskli görünüyor. Belirgin bir risk işareti bulunmadı.";
        };
    }

    private AnalysisResponse toResponse(Analysis analysis) {
        List<RiskFindingResponse> findingResponses = analysis.getFindings()
                .stream()
                .map(finding -> RiskFindingResponse.builder()
                        .riskType(finding.getRiskType())
                        .description(finding.getDescription())
                        .scoreImpact(finding.getScoreImpact())
                        .build())
                .toList();

        return AnalysisResponse.builder()
                .id(analysis.getId())
                .inputType(analysis.getInputType())
                .originalInput(analysis.getOriginalInput())
                .riskLevel(analysis.getRiskLevel())
                .score(analysis.getScore())
                .summary(analysis.getSummary())
                .findings(findingResponses)
                .createdAt(analysis.getCreatedAt())
                .build();
    }
}