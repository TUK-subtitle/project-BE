package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubtitleBroadcastService {

    private final SocketIOServer socketIOServer;

    // 마지막 최종 문장
    private String lastFinalSubtitle = "";

    /**
     * 프론트로 자막 전송
     * @param subtitle 브로드캐스트할 문자열
     * @param isFinal 최종 텍스트 여부
     */
    public synchronized void broadcastSubtitle(String subtitle, boolean isFinal) {
        if (isFinal) {
            String toSend = mergeFinalSubtitle(lastFinalSubtitle, subtitle);
            if (!toSend.isEmpty()) {
                lastFinalSubtitle = subtitle;
                System.out.println("[최종] -> " + toSend);
                socketIOServer.getBroadcastOperations()
                        .sendEvent("stt:subtitle_final", toSend);
            }
        } else {
            System.out.println("[받아쓰는 중 ...] -> " + subtitle);
            socketIOServer.getBroadcastOperations()
                    .sendEvent("stt:subtitle_live", subtitle);
        }
    }

    /**
     * 이전 최종 문장과 현재 최종 문장을 비교해 겹치는 부분 제외
     */
    private String mergeFinalSubtitle(String previous, String current) {
        if (previous.isEmpty()) return current;

        int overlapIndex = 0;
        int maxCheck = Math.min(previous.length(), current.length());

        for (int i = 1; i <= maxCheck; i++) {
            if (previous.endsWith(current.substring(0, i))) {
                overlapIndex = i;
            }
        }

        return current.substring(overlapIndex);
    }
}