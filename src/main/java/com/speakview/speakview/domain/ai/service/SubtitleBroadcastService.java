package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubtitleBroadcastService {

    private final SocketIOServer socketIOServer;
    private final RealtimeSummaryService realtimeSummaryService;

    public void broadcastSubtitle(JsonNode subtitle) {
        socketIOServer.getBroadcastOperations()
                .sendEvent("stt:subtitle_final", subtitle);

        System.out.println("[최종 token] -> " + subtitle);

        Long contentId = subtitle.path("contentId").asLong(-1);
        String text = subtitle.path("text").asText("");

        if (contentId <= 0 || text.isBlank()) {
            System.out.println("[요약 버퍼 스킵] contentId/text 유효하지 않음");
            return;
        }

        // contentId 기준으로 요약 버퍼 저장
        realtimeSummaryService.addSentenceToBuffer(contentId, text);
    }
}