package com.securemail.analyzer.entity;

import com.securemail.analyzer.enums.RiskType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_findings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskType riskType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int scoreImpact;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}