package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.service.RealtimeSummaryService;
import com.speakview.speakview.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Summary", description = "AI 요약 관련 API")
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final RealtimeSummaryService realtimeSummaryService;

    @PostMapping("/end/{contentId}")
    @Operation(
            summary = "강의 종료 및 최종 요약 생성 요청 API",
            description = "실시간 강의를 종료하고, 그동안 쌓인 1분 단위 요약본들을 합쳐서 서론-본론-결론 형태의 최종 요약본을 생성"
    )
    public ApiResponse<String> endLecture(@PathVariable Long contentId) {
        // 비동기 작업 시작
        realtimeSummaryService.generateFinalSummary(contentId);

        return ApiResponse.success("강의가 종료되었습니다. 최종 요약본 생성을 백그라운드에서 시작합니다.", null);
    }

    @GetMapping("/{contentId}")
    @Operation(
            summary = "특정 강의의 최종 요약본 조회 API",
            description = "생성 완료된 최종 요약본 데이터를 조회, 아직 생성 중이거나 데이터가 없을 경우 안내 메시지가 반환"
    )
    public ApiResponse<String> getSummary(@PathVariable Long contentId) {
        String finalSummary = realtimeSummaryService.getFinalSummary(contentId);

        return ApiResponse.success("요약본 조회가 완료되었습니다.", finalSummary);
    }
}