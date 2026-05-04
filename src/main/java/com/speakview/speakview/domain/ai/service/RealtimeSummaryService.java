package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.speakview.speakview.domain.ai.dto.GeminiDTO;
import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.domain.ai.entity.Summary;
import com.speakview.speakview.domain.ai.enums.SummaryType;
import com.speakview.speakview.domain.ai.repository.ContentRepository;
import com.speakview.speakview.domain.ai.repository.SummaryRepository;
import com.speakview.speakview.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeSummaryService {

    private final SocketIOServer socketIOServer;
    private final SummaryRepository summaryRepository;
    private final ContentRepository contentRepository;

    @Value("${GPT_API_KEY}")
    private String gptApiKey;

    @Value("${openapi.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    private final Map<Long, List<String>> textBufferMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> timerMap = new ConcurrentHashMap<>();

    public void addSentenceToBuffer(String sentence) {
        Long defaultContentId = 1L;
        addSentenceToBuffer(defaultContentId, sentence);
    }

    public void addSentenceToBuffer(Long contentId, String sentence) {
        // 버퍼에 텍스트 추가
        textBufferMap.computeIfAbsent(contentId, k -> Collections.synchronizedList(new ArrayList<>())).add(sentence);

        // 이 강의의 첫 음성이 들어온 시간(시작 시간)을 기록
        timerMap.putIfAbsent(contentId, System.currentTimeMillis());

        // 현재 시간과 시작 시간을 비교하여 경과 시간 계산
        long elapsedTime = System.currentTimeMillis() - timerMap.get(contentId);

        // 만약 60초(60000ms) 이상이 경과했다면 요약 실행!
        if (elapsedTime >= 60000) {
            processSummaryForContent(contentId);
        }
    }

    private void processSummaryForContent(Long contentId) {
        List<String> buffer = textBufferMap.get(contentId);

        if (buffer == null || buffer.isEmpty()) return;

        List<String> sentencesToSummarize;
        synchronized (buffer) {
            sentencesToSummarize = new ArrayList<>(buffer);
            buffer.clear(); // 버퍼 비우기
        }

        // 다음 1분을 위해 타이머 초기화
        timerMap.put(contentId, System.currentTimeMillis());

        String textToSummarize = String.join(" ", sentencesToSummarize);
        log.info("[1분 경과] 강의 ID [{}] 요약 요청 (텍스트 길이: {})", contentId, textToSummarize.length());

        // Gemini API 호출
        requestSummaryToGemini(contentId, textToSummarize);
    }

    private void requestSummaryToGemini(Long contentId, String text) {
        WebClient webClient = WebClient.builder().build();

        String prompt = "너는 청각장애 학생을 위해 실시간 강의를 요약하는 전문 AI 조수야. " +
                "입력되는 STT 텍스트에서 불필요한 서론이나 강사의 추임새는 모두 제거하고, 화자가 전달하려는 '핵심 개념'과 '결론'만 추출해서 1~3문장의 명확한 문어체(-합니다, -입니다)로 요약해.\n\n" +
                "⚠️[절대 지켜야 할 규칙]⚠️\n" +
                "1. '네, 요약해 드리겠습니다', '**요약:**' 같은 인사말이나 마크다운 기호, 부연 설명은 절대 출력하지 마.\n" +
                "2. 오직 요약된 순수 텍스트 결과만 바로 출력해.\n\n" +
                "[요약할 텍스트]\n" + text;

        GeminiDTO.Request requestBody = new GeminiDTO.Request(
                List.of(new GeminiDTO.Content(List.of(new GeminiDTO.Part(prompt))))
        );

        webClient.post()
                .uri(geminiApiUrl)
                .header("x-goog-api-key", geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiDTO.Response.class)
                .onErrorResume(Exception.class, e -> {
                    log.error("[Gemini 1분 요약 API 오류] 강의 ID [{}]: {}", contentId, e.getMessage());
                    return Mono.empty();
                })
                .subscribe(response -> {
                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        String summaryResult = response.getCandidates().get(0).getContent().getParts().get(0).getText();

                        saveSummaryToDb(contentId, summaryResult, SummaryType.MINUTE);

                        broadcastSummary(summaryResult);
                    }
                });
    }

    @Transactional
    public void saveSummaryToDb(Long contentId, String summaryText, SummaryType type) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다. ID: " + contentId));

        User user = content.getUser();

        Summary summary = Summary.builder()
                .user(user)
                .content(content)
                .text(summaryText)
                .type(type)
                .build();

        summaryRepository.save(summary);
        log.info("[DB 저장 완료] 강의 ID [{}], 타입 [{}]", contentId, type);
    }

    private void broadcastSummary(String summary) {
        log.info("[Gemini 1분 요약 브로드캐스트] -> {}", summary);
        socketIOServer.getBroadcastOperations().sendEvent("stt:summary", summary);
    }

    @Async
    @Transactional
    public void generateFinalSummary(Long contentId) {
        if(summaryRepository.existsByContentIdAndType(contentId, SummaryType.FINAL)) {
            log.info("이미 FINAL 요약이 존재합니다. contentId={}", contentId);
            return;
        }

        log.info("강의 ID [{}] 의 최종 요약본 생성을 시작합니다.", contentId);

        List<Summary> minuteSummaries = summaryRepository
                .findAllByContentIdAndTypeOrderByCreatedAtAsc(contentId, SummaryType.MINUTE);

        if (minuteSummaries.isEmpty()) {
            log.warn("강의 ID [{}] 에 대한 1분 단위 요약 데이터가 없습니다.", contentId);
            return; // 요약할 데이터가 없으면 종료
        }

        String aggregatedText = minuteSummaries.stream()
                .map(Summary::getText)
                .collect(Collectors.joining("\n"));

        String prompt = "다음은 수업 내용을 1분 단위로 요약한 텍스트입니다. 전체 흐름을 파악하여 서론, 본론, 결론이 있는 완성된 형태의 전체 강의 요약본을 작성해주세요:\n\n" + aggregatedText;

        WebClient webClient = WebClient.builder().build();
        GeminiDTO.Request requestBody = new GeminiDTO.Request(
                List.of(new GeminiDTO.Content(List.of(new GeminiDTO.Part(prompt))))
        );

        try {
            GeminiDTO.Response response = webClient.post()
                    .uri(geminiApiUrl)
                    .header("x-goog-api-key", geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiDTO.Response.class)
                    .block();

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                String finalSummaryText = response.getCandidates().get(0).getContent().getParts().get(0).getText();

                saveSummaryToDb(contentId, finalSummaryText, SummaryType.FINAL);
                log.info("강의 ID [{}] 의 최종 요약본 생성이 완료되었습니다.", contentId);

                socketIOServer.getBroadcastOperations().sendEvent("stt:finalSummaryDone", contentId);
            }
        } catch (Exception e) {
            log.error("[Gemini 최종 요약 API 오류] 강의 ID [{}]: {}", contentId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public String getFinalSummary(Long contentId) {
        return summaryRepository.findFirstByContentIdAndTypeOrderByCreatedAtDesc(contentId, SummaryType.FINAL)
                .map(Summary::getText)
                .orElse("아직 최종 요약본이 생성되지 않았거나 해당 강의를 찾을 수 없습니다.");
    }
}