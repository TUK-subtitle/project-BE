package com.speakview.speakview.domain.ai.dto;

import com.speakview.speakview.domain.ai.entity.LectureAudio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureAudioResponse {

    @Schema(description = "오디오 파일 URL", example = "/uploads/audio/1_20260724_020000.wav")
    private String audioUrl;

    @Schema(description = "오디오 포맷", example = "wav")
    private String format;

    @Schema(description = "샘플레이트", example = "16000")
    private Integer sampleRate;

    @Schema(description = "채널 수", example = "1")
    private Integer channels;

    @Schema(description = "총 길이(ms)", example = "185000")
    private Long durationMs;

    public static LectureAudioResponse from(LectureAudio lectureAudio) {
        return LectureAudioResponse.builder()
                .audioUrl(lectureAudio.getAudioUrl())
                .format(lectureAudio.getFormat())
                .sampleRate(lectureAudio.getSampleRate())
                .channels(lectureAudio.getChannels())
                .durationMs(lectureAudio.getDurationMs())
                .build();
    }
}