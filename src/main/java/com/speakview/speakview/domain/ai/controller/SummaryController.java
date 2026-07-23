package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.dto.MinuteSummaryResponse;
import com.speakview.speakview.domain.ai.dto.SummaryResultResponse;
import com.speakview.speakview.domain.ai.entity.Summary;
import com.speakview.speakview.domain.ai.service.ContentService;
import com.speakview.speakview.domain.ai.service.RealtimeSummaryService;
import com.speakview.speakview.domain.memo.dto.MemoResponse;
import com.speakview.speakview.domain.memo.service.MemoService;
import com.speakview.speakview.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Summary", description = "AI 요약 관련 API")
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final RealtimeSummaryService realtimeSummaryService;
    private final ContentService contentService;
    private final MemoService memoService;

    /**
     * 강의 종료 및 최종 요약 생성 요청
     */
    @PostMapping("/end/{contentId}")
    @Operation(
            summary = "강의 종료 및 최종 요약 생성 요청 API",
            description = "실시간 강의를 종료하고, 그동안 쌓인 1분 단위 요약본들을 합쳐서 서론-본론-결론 형태의 최종 요약본을 생성. title을 함께 전달하면 강의명을 설정"
    )
    public ApiResponse<String> endLecture(
            @PathVariable Long contentId,
            @RequestParam(required = false) String title
    ) {
        // 강의명이 전달된 경우 저장
        if (title != null) {
            contentService.updateTitle(contentId, title);
        }

        // 비동기 작업 시작
        realtimeSummaryService.generateFinalSummary(contentId);

        return ApiResponse.success("강의가 종료되었습니다. 최종 요약본 생성을 백그라운드에서 시작합니다.", null);
    }

    /**
     * 특정 강의의 요약본 결과 조회
     */
    @GetMapping("/{contentId}")
    @Operation(
            summary = "특정 강의의 최종 요약본 조회 API",
            description = "강의명, 최종 요약본 생성 시간, 전체 요약, 1분 단위 실시간 요약 리스트, 메모 리스트를 함께 조회. 아직 생성 중이거나 데이터가 없을 경우 안내 메시지가 반환"
    )
    public ApiResponse<SummaryResultResponse> getSummary(@PathVariable Long contentId) {
        String title = contentService.getTitle(contentId);

        Summary finalSummary = realtimeSummaryService.getFinalSummary(contentId).orElse(null);

        List<MinuteSummaryResponse> minuteSummaries = realtimeSummaryService.getMinuteSummaries(contentId).stream()
                .map(MinuteSummaryResponse::from)
                .toList();

        List<MemoResponse> memos = memoService.getMemos(contentId);

        SummaryResultResponse response = SummaryResultResponse.builder()
                .title(title)
                .createdAt(finalSummary != null ? finalSummary.getCreatedAt() : null)
                .summaryText(finalSummary != null ? finalSummary.getText() : "아직 최종 요약본이 생성되지 않았습니다.")
                .minuteSummaries(minuteSummaries)
                .memos(memos)
                .build();

        return ApiResponse.success("요약본 조회가 완료되었습니다.", response);
    }
}
