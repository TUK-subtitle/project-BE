package com.speakview.speakview.domain.ai.dto;

import com.speakview.speakview.domain.ai.entity.TranscriptToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TranscriptTokenResponse {

    @Schema(description = "토큰 ID", example = "1201")
    private Long tokenId;

    @Schema(description = "토큰 순서", example = "57")
    private Long seq;

    @Schema(description = "단어/토큰 텍스트", example = "자료구조")
    private String text;

    @Schema(description = "화자 번호", example = "1")
    private Integer speaker;

    @Schema(description = "시작 시각(ms)", example = "62340")
    private Long startMs;

    @Schema(description = "종료 시각(ms)", example = "62720")
    private Long endMs;

    @Schema(description = "인식 신뢰도", example = "0.98")
    private Double confidence;

    public static TranscriptTokenResponse from(TranscriptToken token) {
        return TranscriptTokenResponse.builder()
                .tokenId(token.getId())
                .seq(token.getSeq())
                .text(token.getText())
                .speaker(token.getSpeaker())
                .startMs(token.getStartMs())
                .endMs(token.getEndMs())
                .confidence(token.getConfidence())
                .build();
    }
}