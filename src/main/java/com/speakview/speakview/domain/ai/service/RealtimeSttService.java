package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.speakview.speakview.domain.ai.entity.TranscriptToken;
import com.speakview.speakview.domain.ai.event.LectureEndedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class RealtimeSttService {

    private final ObjectMapper objectMapper;
    private final SocketIOServer socketIOServer;
    private final SubtitleBroadcastService subtitleService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AudioArchiveService audioArchiveService;
    private final TranscriptService transcriptService;
    private final ConcurrentMap<String, Long> sessionContentMap = new ConcurrentHashMap<>();
    private volatile Long currentAudioContentId;

    @Value("${soniox.api-key}")
    private String apiKey;

    private static final String SONIOX_WS_URL = "wss://stt-rt.soniox.com/transcribe-websocket";
    private WebSocketSession session;
    private final Queue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();

    private String roomName(Long contentId) {
        return "content:" + contentId;
    }

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
            if (session == null || !session.isOpen()) {
                initConnection();
            }
        });

        // 방 입장
        socketIOServer.addEventListener("stt:join", Map.class, (client, data, ackSender) -> {
            String sid = client.getSessionId().toString();
            Long contentId = Long.valueOf(data.get("contentId").toString());

            sessionContentMap.put(sid, contentId);
            client.joinRoom(roomName(contentId));

            System.out.println("[join] sid=" + sid + " contentId=" + contentId);
        });

        // 오디오 수신
        socketIOServer.addEventListener("stt:audio", String.class, (client, base64Data, ackSender) -> {
            try {
                String sid = client.getSessionId().toString();
                Long contentId = sessionContentMap.get(sid);

                if (contentId == null) {
                    System.out.println("[경고] 오디오 수신했지만 contentId 매핑 없음 sid=" + sid);
                    return;
                }

                currentAudioContentId = contentId;

                byte[] audioData = java.util.Base64.getDecoder().decode(base64Data);
                System.out.println("[오디오] 수신, 길이: " + audioData.length + ", contentId=" + contentId);
                audioArchiveService.appendChunk(contentId, audioData);
                sendAudioFrame(audioData);
            } catch (IllegalArgumentException e) {
                System.out.println("[경고] Base64 디코딩 실패: " + e.getMessage());
            }
        });

        // 클라이언트 연결 해제
        socketIOServer.addDisconnectListener(client -> {
            String sid = client.getSessionId().toString();
            Long contentId = sessionContentMap.remove(sid);

            if (contentId != null) {
                client.leaveRoom(roomName(contentId));
            }

            if (socketIOServer.getAllClients().isEmpty() && session != null && session.isOpen()) {
                session.close().subscribe();
                session = null;
                System.out.println("[Soniox] 연결 종료");
            }

            if (contentId != null) {
                boolean stillPresent = sessionContentMap.values().stream().anyMatch(id -> id.equals(contentId));
                if (!stillPresent) {
                    applicationEventPublisher.publishEvent(new LectureEndedEvent(this, contentId));
                }
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
    private String lastSentToken = "";

    private void handleSonioxMessage(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            if (node.has("tokens")) {
                for (JsonNode token : node.path("tokens")) {
                    String text = token.path("text").asText("");
                    int speaker = token.path("speaker").asInt(0);
                    boolean isFinal = token.path("is_final").asBoolean(false);

                    if (!isFinal) continue;
                    if (text.equals("<end>")) continue;

                    // 중복 방지
                    if (text.equals(lastSentToken)) continue;
                    lastSentToken = text;

                    Long contentId = currentAudioContentId;
                    if (contentId == null) {
                        System.out.println("[경고] contentId 매핑이 없어 자막/요약 저장 스킵");
                        continue;
                    }

                    Long startMs = readTimeMs(token, "start_ms", "startMs", "start_time");
                    Long endMs = readTimeMs(token, "end_ms", "endMs", "end_time");
                    Double confidence = token.has("confidence") ? token.path("confidence").asDouble() : null;

                    TranscriptToken savedToken = transcriptService.saveToken(
                            contentId, text, speaker, startMs, endMs, confidence
                    );

                    ObjectNode response = objectMapper.createObjectNode();
                    response.put("text", text);
                    response.put("tokenId", savedToken.getId());
                    response.put("seq", savedToken.getSeq());
                    if (startMs != null) response.put("startMs", startMs);
                    if (endMs != null) response.put("endMs", endMs);
                    if (confidence != null) response.put("confidence", confidence);
                    response.put("speaker", speaker);
                    response.put("contentId", contentId); // 핵심: contentId 포함

                    if (!socketIOServer.getAllClients().isEmpty()) {
                        System.out.println("[token] -> " + response);
                    }

                    subtitleService.broadcastSubtitle(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long readTimeMs(JsonNode token, String... keys) {
        for (String key : keys) {
            if (!token.has(key)) continue;
            JsonNode valueNode = token.get(key);
            if (!valueNode.isNumber()) continue;

            double value = valueNode.asDouble();

            if (key.endsWith("_ms") || key.endsWith("Ms")) {
                return (long) value;
            }
            return Math.round(value * 1000.0);
        }
        return null;
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