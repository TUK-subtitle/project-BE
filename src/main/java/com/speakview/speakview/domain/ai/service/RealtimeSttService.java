package com.speakview.speakview.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${SONIOX_API_KEY}")
    private String apiKey;

    private static final String SONIOX_WS_URL = "wss://stt-rt.soniox.com/transcribe-websocket";

    private WebSocketSession session;

    private final Queue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();

    public void initConnection() {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(
                URI.create(SONIOX_WS_URL),
                this::handleSession
        ).subscribe();
    }

    private Mono<Void> handleSession(WebSocketSession ws) {
        this.session = ws;
        System.out.println("Connected to Soniox WebSocket!");

        // ⬇️ 1. Start config JSON
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("api_key", apiKey);
            config.put("model", "stt-rt-v4");
            config.put("audio_format", "pcm_s16le"); // Raw PCM
            config.put("sample_rate", 16000);
            config.put("num_channels", 1);

            String configJson = objectMapper.writeValueAsString(config);

            // Must send config as TEXT
            ws.send(Mono.just(ws.textMessage(configJson))).subscribe();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // ⬇️ 2. Listen for server responses (JSON with tokens)
        Flux<String> recv = ws.receive()
                .map(WebSocketMessage::getPayloadAsText);

        recv.subscribe(System.out::println);

        // ⬇️ 3. Send any queued audio data
        flushAudioQueue();

        return Mono.never();
    }

    /** Called from your mic capture class */
    public void sendAudioFrame(byte[] rawPcmChunk) {
        if (session != null && session.isOpen()) {
            sendNow(rawPcmChunk);
        } else {
            audioQueue.add(rawPcmChunk);
            System.out.println("큐에 저장, WebSocket 연결 대기중...");
        }
    }

    /** Finish audio (optional) */
    public void commitAudio() {
        if (session != null && session.isOpen()) {
            // No special commit required for Soniox
            // To end streaming, send an “empty frame”
            session.send(Mono.just(session.binaryMessage(b -> b.wrap(new byte[0])))).subscribe();
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