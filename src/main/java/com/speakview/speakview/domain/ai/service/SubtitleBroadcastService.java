package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SubtitleBroadcastService {

    private final SocketIOServer socketIOServer;
    private final RealtimeSummaryService realtimeSummaryService;

    public SubtitleBroadcastService(
            SocketIOServer socketIOServer,
            @Lazy RealtimeSummaryService realtimeSummaryService
    ) {
        this.socketIOServer = socketIOServer;
        this.realtimeSummaryService = realtimeSummaryService;
    }

    public void broadcastSubtitle(JsonNode subtitle) {
        Long contentId = subtitle.path("contentId").asLong(-1);
        String text = subtitle.path("text").asText("");

        if (contentId > 0) {
            socketIOServer.getRoomOperations("content:" + contentId)
                    .sendEvent("stt:subtitle", subtitle);
        } else {
            socketIOServer.getBroadcastOperations()
                    .sendEvent("stt:subtitle", subtitle);
        }

        System.out.println("[최종 token] -> " + subtitle);

        if (contentId <= 0 || text.isBlank()) {
            System.out.println("[요약 버퍼 스킵] contentId/text 유효하지 않음");
            return;
        }

        realtimeSummaryService.addSentenceToBuffer(contentId, text);
    }
}