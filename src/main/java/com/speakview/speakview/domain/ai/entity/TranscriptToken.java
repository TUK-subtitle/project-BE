package com.speakview.speakview.domain.ai.entity;

import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "TranscriptToken",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transcript_token_content_seq", columnNames = {"content_id", "seq"})
        },
        indexes = {
                @Index(name = "idx_transcript_token_user", columnList = "user_id"),
                @Index(name = "idx_transcript_token_content", columnList = "content_id"),
                @Index(name = "idx_transcript_token_content_seq", columnList = "content_id, seq"),
                @Index(name = "idx_transcript_token_content_start", columnList = "content_id, start_ms")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TranscriptToken extends BaseEntity {

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

    @Column(name = "seq", nullable = false)
    private Long seq;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "speaker")
    private Integer speaker;

    @Column(name = "start_ms")
    private Long startMs;

    @Column(name = "end_ms")
    private Long endMs;

    @Column(name = "confidence")
    private Double confidence;
}