package com.speakview.speakview.domain.ai.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    public void initConnection() {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(URI.create(SONIOX_WS_URL), this::handleSession).subscribe();
    }

    @PostConstruct
    public void initSocketListeners() {
        socketIOServer.addConnectListener(client -> System.out.println("클라이언트 연결 성공: " + client.getSessionId()));
        socketIOServer.addDisconnectListener(client -> System.out.println("클라이언트 연결 해제: " + client.getSessionId()));

        socketIOServer.addEventListener("stt:audio", byte[].class, (client, data, ackSender) -> {
            System.out.println("오디오 데이터 수신, 길이: " + data.length);
            sendAudioFrame(data);
        });
    }

    private Mono<Void> handleSession(WebSocketSession ws) {
        this.session = ws;
        System.out.println("Soniox 소켓 연결 성공");

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("api_key", apiKey);
            config.put("model", "stt-rt-v4");
            config.put("audio_format", "pcm_s16le");
            config.put("sample_rate", 16000);
            config.put("num_channels", 1);

            String configJson = objectMapper.writeValueAsString(config);
            ws.send(Mono.just(ws.textMessage(configJson))).subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 서버에서 오는 STT 응답 처리
        Flux<String> recv = ws.receive()
                .map(WebSocketMessage::getPayloadAsText);

        recv.subscribe(this::handleSonioxMessage);

        flushAudioQueue();
        return Mono.never();
    }

    private void handleSonioxMessage(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            if (node.has("tokens")) {
                StringBuilder sb = new StringBuilder();
                boolean hasFinal = false;

                for (JsonNode token : node.path("tokens")) {
                    String text = token.path("text").asText("");
                    sb.append(text);
                    if (token.path("is_final").asBoolean(false)) {
                        hasFinal = true;
                    }
                }

                String subtitle = sb.toString();
                if (!subtitle.isEmpty()) {
                    subtitleService.broadcastSubtitle(subtitle, hasFinal);
                    // System.out.println("[STT] " + (hasFinal ? "(최종) " : "") + subtitle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 오디오 프레임 전송
    public void sendAudioFrame(byte[] rawPcmChunk) {
        if (session != null && session.isOpen()) {
            sendNow(rawPcmChunk);
        } else {
            audioQueue.add(rawPcmChunk);
            System.out.println("큐에 저장, WebSocket 연결 대기중...");
        }
    }

    private void sendNow(byte[] rawPcm) {
        if (session != null && session.isOpen()) {
            session.send(Mono.just(session.binaryMessage(b -> b.wrap(ByteBuffer.wrap(rawPcm))))).subscribe();
        }
    }

    private void flushAudioQueue() {
        while (!audioQueue.isEmpty()) {
            sendNow(audioQueue.poll());
        }
    }
}