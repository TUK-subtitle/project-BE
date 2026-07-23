package com.speakview.speakview.domain.ai.entity;

import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "LectureAudio",
        indexes = {
                @Index(name = "idx_lecture_audio_user", columnList = "user_id"),
                @Index(name = "idx_lecture_audio_content", columnList = "content_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureAudio extends BaseEntity {

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

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "format", nullable = false, length = 50)
    private String format;

    @Column(name = "sample_rate", nullable = false)
    private Integer sampleRate;

    @Column(name = "channels", nullable = false)
    private Integer channels;

    @Column(name = "duration_ms")
    private Long durationMs;
}