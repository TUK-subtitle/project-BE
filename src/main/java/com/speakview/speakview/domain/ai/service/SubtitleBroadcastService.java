package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubtitleBroadcastService {

    private final SocketIOServer socketIOServer;
    private final RealtimeSummaryService realtimeSummaryService;

    public void broadcastSubtitle(String subtitle) {
        socketIOServer.getBroadcastOperations()
                .sendEvent("stt:subtitle_final", subtitle);

        System.out.println("[최종 token] -> " + subtitle);

        // 요약 버퍼
        realtimeSummaryService.addSentenceToBuffer(subtitle);
    }
}