package com.speakview.speakview.console;

import com.speakview.speakview.domain.ai.service.RealtimeSttService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.Base64;

@Component
public class RealtimeSttMicTest {

    @Autowired
    private RealtimeSttService sttService;

    @PostConstruct
    public void startRealtimeMic() throws Exception {
        System.out.println("마이크 실시간 STT 테스트 시작...");

        // 1. WebSocket 연결
        sttService.initConnection();

        // 2. 마이크 설정
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("마이크 라인 지원 안됨!");
            return;
        }

        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        byte[] buffer = new byte[3200]; // 100ms chunk (16kHz*2byte*0.1s)
        System.out.println("마이크 캡처 시작, Speakix STT 연결 대기중...");

        while (true) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                sttService.sendAudioFrame(Arrays.copyOf(buffer, bytesRead));
            }
        }
    }
}