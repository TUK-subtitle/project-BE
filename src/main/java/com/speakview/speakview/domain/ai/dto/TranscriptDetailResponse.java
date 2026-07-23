package com.speakview.speakview.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TranscriptDetailResponse {

    @Schema(description = "강의 ID", example = "1")
    private Long contentId;

    @Schema(description = "전체 자막 텍스트", example = "안녕하세요 오늘은 자료구조를 배웁니다 ...")
    private String fullText;

    @Schema(description = "전체 토큰 수", example = "432")
    private Long tokenCount;

    @Schema(description = "강의 오디오 정보")
    private LectureAudioResponse audio;

    @Schema(description = "단어(토큰) 리스트")
    private List<TranscriptTokenResponse> tokens;
}