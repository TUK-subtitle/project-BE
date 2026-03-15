package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.service.RealtimeSummaryService;
import com.speakview.speakview.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final RealtimeSummaryService realtimeSummaryService;

    /**
     * 강의 종료 및 최종 요약 생성 요청
     */
    @PostMapping("/end/{contentId}")
    public ApiResponse<String> endLecture(@PathVariable Long contentId) {
        // 비동기 작업 시작
        realtimeSummaryService.generateFinalSummary(contentId);

        return ApiResponse.success("강의가 종료되었습니다. 최종 요약본 생성을 백그라운드에서 시작합니다.", null);
    }

    /**
     * 특정 강의의 요약본 결과 조회
     */
    @GetMapping("/{contentId}")
    public ApiResponse<Object> getSummary(@PathVariable Long contentId) {
        String finalSummary = realtimeSummaryService.getFinalSummary(contentId);

        return ApiResponse.success("요약본 조회가 완료되었습니다.", finalSummary);
    }
}