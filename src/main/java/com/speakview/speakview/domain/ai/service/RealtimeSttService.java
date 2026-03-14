package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class RealtimeSttService {

    private final ObjectMapper objectMapper;
    private final SocketIOServer socketIOServer;
    private final SubtitleBroadcastService subtitleService;

    @Value("${soniox.api-key}")
    private String apiKey;

    private static final String SONIOX_WS_URL = "wss://stt-rt.soniox.com/transcribe-websocket";
    private WebSocketSession session;
    private final Queue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();

    /**
     * 최초 클라이언트 연결 시 Soniox WebSocket 연결
     */
    public void initConnection() {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(URI.create(SONIOX_WS_URL), this::handleSession).subscribe();
        System.out.println("[Soniox] 연결 시도");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initSocketListeners() {
        // 클라이언트 연결
        socketIOServer.addConnectListener(client -> {
            System.out.println("[클라이언트] 연결 성공: " + client.getSessionId());

            // 최초 연결 시 Soniox 연결
            if (session == null || !session.isOpen()) {
                initConnection();
            }
        });

        // 클라이언트 연결 해제
        socketIOServer.addDisconnectListener(client -> {
            System.out.println("[클라이언트] 연결 해제: " + client.getSessionId());

            // 모든 클라이언트가 끊기면 Soniox 연결 종료
            if (socketIOServer.getAllClients().isEmpty() && session != null && session.isOpen()) {
                session.close().subscribe();
                session = null;
                System.out.println("[Soniox] 연결 종료");
            }
        });

        // 오디오 데이터 수신 (Base64로 오는 경우)
        socketIOServer.addEventListener("stt:audio", String.class, (client, base64Data, ackSender) -> {
            try {
                byte[] audioData = java.util.Base64.getDecoder().decode(base64Data);
                System.out.println("[오디오] 수신, 길이: " + audioData.length);
                sendAudioFrame(audioData);
            } catch (IllegalArgumentException e) {
                System.out.println("[경고] Base64 디코딩 실패: " + e.getMessage());
            }
        });
    }

    /**
     * Soniox WebSocket 연결 처리
     */
    private Mono<Void> handleSession(WebSocketSession ws) {
        this.session = ws;
        System.out.println("[Soniox] 연결 성공");

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("api_key", apiKey);
            config.put("model", "stt-rt-v4");
            config.put("audio_format", "pcm_s16le");
            config.put("sample_rate", 16000);
            config.put("num_channels", 1);

            config.put("enable_speaker_diarization", true);
            config.put("enable_endpoint_detection", true);

            String configJson = objectMapper.writeValueAsString(config);
            ws.send(Mono.just(ws.textMessage(configJson))).subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }

        flushAudioQueue();

        return ws.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(this::handleSonioxMessage)
                .then();
    }

    /**
     * Soniox 메시지 처리
     */
    private void handleSonioxMessage(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            if (node.has("tokens")) {
                int lastSpeaker = -1;
                StringBuilder sb = new StringBuilder();
                boolean hasFinal = false;

                for (JsonNode token : node.path("tokens")) {
                    String text = token.path("text").asText("");
                    int speaker = token.path("speaker").asInt(0);

                    if (speaker != lastSpeaker) {
                        sb.append("\nSpeaker ").append(speaker).append(": ");
                        lastSpeaker = speaker;
                    }

                    sb.append(text);
                    if (token.path("is_final").asBoolean(false)) {
                        hasFinal = true;
                    }
                }

                String subtitle = sb.toString();
                if (!subtitle.isEmpty()) {
                    // 클라이언트가 하나 이상 연결되어 있을 때만 로그 출력
                    if (!socketIOServer.getAllClients().isEmpty()) {
                        System.out.println("[받아쓰는 중 ...] -> " + subtitle);
                    }
                    subtitleService.broadcastSubtitle(subtitle, hasFinal);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 오디오 프레임 전송
     */
    public void sendAudioFrame(byte[] rawPcmChunk) {
        if (session != null && session.isOpen()) {
            sendNow(rawPcmChunk);
        } else {
            System.out.println("[오디오] WebSocket 연결 안됨, 데이터 버림");
        }
    }

    /**
     * Soniox로 바로 전송
     */
    private void sendNow(byte[] rawPcm) {
        if (session != null && session.isOpen()) {
            session.send(Mono.just(session.binaryMessage(b -> b.wrap(ByteBuffer.wrap(rawPcm))))).subscribe();
        }
    }

    /**
     * 큐에 쌓인 오디오 flush
     */
    private void flushAudioQueue() {
        if (session == null || !session.isOpen()) return;

        while (!audioQueue.isEmpty()) {
            sendNow(audioQueue.poll());
        }
    }
}