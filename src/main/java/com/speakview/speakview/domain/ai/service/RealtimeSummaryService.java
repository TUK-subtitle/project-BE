package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.speakview.speakview.domain.ai.dto.ChatGptDTO;
import com.speakview.speakview.domain.ai.dto.GeminiDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RealtimeSummaryService {

    private final SocketIOServer socketIOServer;

    @Value("${GPT_API_KEY}")
    private String gptApiKey;

    @Value("${openapi.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    private final List<String> textBuffer = Collections.synchronizedList(new ArrayList<>());

    public void addSentenceToBuffer(String sentence) {
        textBuffer.add(sentence);
    }

    @Scheduled(fixedRate = 60000)
    public void processSummaryEveryMinute() {
        // 1분 동안 아무말도 없어서 버퍼가 비어있으면 API 호출 생략
        if (textBuffer.isEmpty()) {
            return;
        }

        List<String> sentencesToSummarize;

        // 버퍼의 내용을 복사하고, 기존 버퍼는 비우기
        synchronized (textBuffer) {
            sentencesToSummarize = new ArrayList<>(textBuffer);
            textBuffer.clear();
        }

        // 복사한 문장들을 하나의 긴 텍스트로 합치기
        String textToSummarize = String.join(" ", sentencesToSummarize);
        System.out.println("[1분 경과] 요약 요청 텍스트: " + textToSummarize);

        // 상황에 따라 chatgpt, Gemini 변경
        //requestSummaryToChatGpt(textToSummarize);
        requestSummaryToGemini(textToSummarize);
    }

    /**
     * WebClient를 이용해 비동기로 ChatGPT API 호출
     */
    private void requestSummaryToChatGpt(String text) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + gptApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        String systemPrompt = "너는 실시간 회의/강의 내용을 요약하는 어시스턴트야. " +
                "입력되는 텍스트는 음성 인식(STT) 결과라 오타나 문맥이 끊기는 부분이 있을 수 있어. " +
                "최근 1분 동안 진행된 내용이니, 핵심만 파악해서 자연스럽게 요약해줘.";

        List<ChatGptDTO.Message> messages = List.of(
                new ChatGptDTO.Message("system", systemPrompt),
                new ChatGptDTO.Message("user", text)
        );

        // gpt-4o-mini 모델 사용
        ChatGptDTO.Request requestBody = new ChatGptDTO.Request(openAiModel, messages, 0.7);

        webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChatGptDTO.Response.class)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                    System.err.println("[ChatGPT API 상세 오류] " + e.getResponseBodyAsString());
                    return Mono.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("[ChatGPT API 일반 오류] " + e.getMessage());
                    return Mono.empty();
                })
                .subscribe(response -> {
                    if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String summaryResult = response.getChoices().get(0).getMessage().getContent();
                        broadcastSummary(summaryResult);
                    }
                });
    }

    private void requestSummaryToGemini(String text) {
        WebClient webClient = WebClient.builder().build();

        String prompt = "너는 실시간 회의/강의 내용을 요약하는 어시스턴트야. " +
                "입력되는 텍스트는 음성 인식(STT) 결과라 오타나 문맥이 끊기는 부분이 있을 수 있어. " +
                "최근 1분 동안 진행된 내용이니, 핵심만 파악해서 자연스럽게 요약해줘.\n\n[요약할 텍스트]\n" + text;

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
                    System.err.println("[Gemini API 일반 오류] " + e.getMessage());
                    return Mono.empty();
                })
                .subscribe(response -> {
                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        String summaryResult = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        broadcastSummary(summaryResult);
                    }
                });
    }

    /**
     * 생성된 요약을 프론트엔드로 브로드캐스트
     */
    private void broadcastSummary(String summary) {
        System.out.println("[ChatGPT 1분 요약 완료] -> " + summary);
        socketIOServer.getBroadcastOperations()
                .sendEvent("stt:summary", summary);
    }
}
