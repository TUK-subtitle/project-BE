package com.speakview.speakview.domain.ai.entity;

import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "TranscriptFull",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transcript_full_content", columnNames = "content_id")
        },
        indexes = {
                @Index(name = "idx_transcript_full_user", columnList = "user_id"),
                @Index(name = "idx_transcript_full_content", columnList = "content_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TranscriptFull extends BaseEntity {

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

    @Column(name = "full_text", columnDefinition = "TEXT", nullable = false)
    private String fullText;

    @Column(name = "token_count", nullable = false)
    private Long tokenCount;
}