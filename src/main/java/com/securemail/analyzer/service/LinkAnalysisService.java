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
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LinkAnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    public AnalysisResponse analyzeLink(AnalysisRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kullanıcı bulunamadı."
                ));

        String originalInput = request.getInput().trim();
        String input = originalInput.toLowerCase();

        Analysis analysis = new Analysis();
        analysis.setUser(user);
        analysis.setInputType(InputType.LINK);
        analysis.setOriginalInput(originalInput);

        List<RiskFinding> findings = new ArrayList<>();
        int score = 0;

        if (!input.startsWith("https://")) {
            score += addFinding(
                    analysis,
                    findings,
                    RiskType.HTTPS_MISSING,
                    "Link HTTPS ile başlamıyor. Güvenli bağlantı kullanılmıyor olabilir.",
                    25
            );
        }

        if (containsShortUrl(input)) {
            score += addFinding(
                    analysis,
                    findings,
                    RiskType.SHORT_URL,
                    "Link kısa URL servisi kullanıyor. Gerçek hedef adres gizlenmiş olabilir.",
                    25
            );
        }

        if (containsSuspiciousKeyword(input)) {
            score += addFinding(
                    analysis,
                    findings,
                    RiskType.FAKE_LINK,
                    "Link içinde kullanıcıyı giriş yapmaya, doğrulama yapmaya veya işlem yapmaya yönlendiren şüpheli ifadeler bulundu.",
                    20
            );
        }

        if (containsBrandName(input)) {
            score += addFinding(
                    analysis,
                    findings,
                    RiskType.BRAND_IMPERSONATION,
                    "Link içinde bilinen marka veya servis adları geçiyor. Marka taklidi ihtimali olabilir.",
                    20
            );
        }

        if (containsIpAddress(input)) {
            score += addFinding(
                    analysis,
                    findings,
                    RiskType.FAKE_LINK,
                    "Link domain yerine IP adresi içeriyor. Bu durum oltalama bağlantılarında sık görülebilir.",
                    30
            );
        }

        score = Math.min(score, 100);

        RiskLevel riskLevel = determineRiskLevel(score);
        String summary = generateSummary(riskLevel);

        analysis.setScore(score);
        analysis.setRiskLevel(riskLevel);
        analysis.setSummary(summary);
        analysis.setFindings(findings);

        Analysis savedAnalysis = analysisRepository.save(analysis);

        return mapToResponse(savedAnalysis);
    }

    private int addFinding(
            Analysis analysis,
            List<RiskFinding> findings,
            RiskType riskType,
            String description,
            int scoreImpact
    ) {
        RiskFinding finding = new RiskFinding();
        finding.setAnalysis(analysis);
        finding.setRiskType(riskType);
        finding.setDescription(description);
        finding.setScoreImpact(scoreImpact);

        findings.add(finding);

        return scoreImpact;
    }

    private boolean containsShortUrl(String input) {
        return input.contains("://bit.ly/")
                || input.contains("://www.bit.ly/")
                || input.contains("://tinyurl.com/")
                || input.contains("://www.tinyurl.com/")
                || input.contains("://t.co/")
                || input.contains("://www.t.co/")
                || input.contains("://goo.gl/")
                || input.contains("://www.goo.gl/")
                || input.contains("://ow.ly/")
                || input.contains("://www.ow.ly/")
                || input.contains("://is.gd/")
                || input.contains("://www.is.gd/")
                || input.contains("://buff.ly/")
                || input.contains("://www.buff.ly/")
                || input.contains("://rebrand.ly/")
                || input.contains("://www.rebrand.ly/")
                || input.contains("://cutt.ly/")
                || input.contains("://www.cutt.ly/");
    }

    private boolean containsSuspiciousKeyword(String input) {
        return input.contains("login")
                || input.contains("verify")
                || input.contains("update")
                || input.contains("secure")
                || input.contains("account")
                || input.contains("free")
                || input.contains("gift")
                || input.contains("giriş")
                || input.contains("doğrula")
                || input.contains("guncelle")
                || input.contains("güncelle")
                || input.contains("hesap")
                || input.contains("ödül")
                || input.contains("odul");
    }

    private boolean containsBrandName(String input) {
        return input.contains("paypal")
                || input.contains("amazon")
                || input.contains("netflix")
                || input.contains("apple")
                || input.contains("google")
                || input.contains("microsoft")
                || input.contains("banka")
                || input.contains("ziraat")
                || input.contains("garanti")
                || input.contains("akbank")
                || input.contains("yapikredi")
                || input.contains("isbank");
    }

    private boolean containsIpAddress(String input) {
        Pattern ipPattern = Pattern.compile("(https?://)?(\\d{1,3}\\.){3}\\d{1,3}");
        return ipPattern.matcher(input).find();
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score <= 30) {
            return RiskLevel.LOW;
        }

        if (score <= 70) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.HIGH;
    }

    private String generateSummary(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> "Bu link düşük riskli görünüyor. Yine de bağlantıya tıklamadan önce adresi kontrol etmek önemlidir.";
            case MEDIUM -> "Bu link orta riskli görünüyor. Bazı şüpheli ifadeler veya yönlendirme işaretleri içeriyor olabilir.";
            case HIGH -> "Bu link yüksek riskli görünüyor. Oltalama, sahte yönlendirme veya marka taklidi riski taşıyor olabilir.";
        };
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