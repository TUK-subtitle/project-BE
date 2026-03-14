package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.service.RealtimeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Test {

    @GetMapping("/")
    public String ok() {
        return "ok";
    }

    private final RealtimeSummaryService summaryService;

    @GetMapping("/test-summary")
    public String testSummary() {
        summaryService.addSentenceToBuffer("안녕하세요, 테스트 자막입니다.");
        summaryService.addSentenceToBuffer("이번 수업의 진도는 3단원까지 나가겠습니다.");

        return "데이터 주입 완료, 1분 뒤에 요약 로그 뜨는지 확인";
    }
}