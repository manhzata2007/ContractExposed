package com.contractexposed.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "overall_risk_score", nullable = false)
    @Builder.Default
    private Integer overallRiskScore = 0;

    @Column(name = "risk_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.UNKNOWN;

    @Column(name = "summary", columnDefinition = "NVARCHAR(MAX)")
    private String summary;

    @Column(name = "ai_raw_response", columnDefinition = "NVARCHAR(MAX)")
    private String aiRawResponse;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "analysis_duration_ms")
    private Long analysisDurationMs;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sort_order ASC, severity_score DESC")
    @Builder.Default
    private List<RiskFinding> findings = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sort_order ASC")
    @Builder.Default
    private List<NegotiationChecklistItem> checklistItems = new ArrayList<>();

    // ─── Enum ─────────────────────────────────────────────────
    public enum RiskLevel {
        UNKNOWN, LOW, MEDIUM, HIGH, CRITICAL;

        public static RiskLevel fromScore(int score) {
            if (score >= 80) return CRITICAL;
            if (score >= 60) return HIGH;
            if (score >= 40) return MEDIUM;
            if (score >= 1)  return LOW;
            return UNKNOWN;
        }
    }
}
