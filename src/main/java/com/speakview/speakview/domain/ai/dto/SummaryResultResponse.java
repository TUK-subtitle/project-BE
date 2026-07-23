package com.speakview.speakview.domain.ai.dto;

import com.speakview.speakview.domain.memo.dto.MemoResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SummaryResultResponse {

    @Schema(description = "강의명", example = "3주차 강의")
    private String title;

    @Schema(description = "최종 요약본 생성 일시 (한국 표준시, KST), 아직 생성되지 않았다면 null", example = "2026-07-23T22:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "최종 요약본 텍스트", example = "이번 강의에서는 ...")
    private String summaryText;

    @Schema(description = "1분 단위 실시간 요약 리스트 (생성 순)")
    private List<MinuteSummaryResponse> minuteSummaries;

    @Schema(description = "해당 강의에 작성된 메모 리스트")
    private List<MemoResponse> memos;
}
