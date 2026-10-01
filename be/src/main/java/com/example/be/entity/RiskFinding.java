package com.contractexposed.backend.entity;

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
    @JoinColumn(name = "report_id", nullable = false)
    private AnalysisReport report;

    @Column(name = "clause_title", nullable = false, length = 500)
    private String clauseTitle;

    @Column(name = "original_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String originalText;

    @Column(name = "risk_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private RiskType riskType;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "severity_score", nullable = false)
    @Builder.Default
    private Integer severityScore = 0;

    @Column(name = "explanation", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Column(name = "recommendation", columnDefinition = "NVARCHAR(MAX)")
    private String recommendation;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "highlight_color", length = 20)
    @Builder.Default
    private String highlightColor = "#FF6B6B";

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ─── Enums ────────────────────────────────────────────────
    public enum RiskType {
        FINANCIAL, LEGAL, TERMINATION, PRIVACY, PENALTY, RENEWAL, DISPUTE, OTHER
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL;

        public String getColor() {
            return switch (this) {
                case LOW      -> "#4ADE80";
                case MEDIUM   -> "#FACC15";
                case HIGH     -> "#FB923C";
                case CRITICAL -> "#EF4444";
            };
        }

        public int getDefaultScore() {
            return switch (this) {
                case LOW      -> 25;
                case MEDIUM   -> 50;
                case HIGH     -> 75;
                case CRITICAL -> 95;
            };
        }
    }
}
