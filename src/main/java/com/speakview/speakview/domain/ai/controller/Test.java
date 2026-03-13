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
        // 프론트에서 소켓으로 날아오는 데이터인 것처럼 가짜 문장을 버퍼에 강제로 집어넣습니다.
        summaryService.addSentenceToBuffer("안녕하세요, 오늘 프로젝트 회의 시작하겠습니다.");
        summaryService.addSentenceToBuffer("이번 스프링 부트 아키텍처는 어떻게 구성할까요?");
        summaryService.addSentenceToBuffer("데이터베이스는 RDS를 쓰고, EC2에 도커로 올립시다.");

        return "가짜 데이터 주입 완료! 이제 백엔드 콘솔 창을 열고 1분 뒤에 요약 로그가 뜨는지 확인하세요.";
    }
}