package com.speakview.speakview.domain.ai.entity;

import com.speakview.speakview.domain.ai.enums.SummaryType;
import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "Summary",
        indexes = {
                @Index(name="idx_summary_user", columnList="user_id"),
                @Index(name="idx_summary_content", columnList="content_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Summary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SummaryType type;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;
}