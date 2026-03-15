package com.speakview.speakview.domain.ai.entity;

import com.speakview.speakview.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "Summary",
        indexes = {
                @Index(name="idx_summary_user", columnList="userId"),
                @Index(name="idx_summary_content", columnList="contentId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Content content;

    @Lob
    @Column(nullable = false)
    private String summaryText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SummaryType summaryType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum SummaryType {
        MINUTE, FINAL
    }
}