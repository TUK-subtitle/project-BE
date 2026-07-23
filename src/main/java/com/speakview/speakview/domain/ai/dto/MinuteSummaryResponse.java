package com.speakview.speakview.domain.ai.dto;

import com.speakview.speakview.domain.ai.entity.Summary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MinuteSummaryResponse {

    @Schema(description = "1분 단위 실시간 요약 텍스트", example = "이번 시간에는 자료구조 3단원을 배웠습니다.")
    private String text;

    @Schema(description = "생성 일시 (한국 표준시, KST)", example = "2026-07-23T22:25:46")
    private LocalDateTime createdAt;

    public static MinuteSummaryResponse from(Summary summary) {
        return MinuteSummaryResponse.builder()
                .text(summary.getText())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
