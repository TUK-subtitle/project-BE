package com.speakview.speakview.global.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketConfig {

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();

        config.setHostname("0.0.0.0");
        config.setPort(8081);
        config.setOrigin("*");
        // 추후 서비스 배포 완료시 : config.setOrigin("https://your-frontend.com");

        config.setMaxFramePayloadLength(1024 * 1024); // 1MB
        config.setMaxHttpContentLength(1024 * 1024);  // 1MB

        config.setExceptionListener(new com.corundumstudio.socketio.listener.DefaultExceptionListener() {
            @Override
            public void onEventException(Exception e, java.util.List<Object> args, com.corundumstudio.socketio.SocketIOClient client) {
                // NumberFormatException 로그가 너무 시끄럽다면 여기서 필터링 가능합니다.
                if (e instanceof NumberFormatException) {
                    System.err.println("[Socket Error] 데이터 포맷 불일치 (바이너리 데이터 해석 오류)");
                } else {
                    super.onEventException(e, args, client);
                }
            }
        });

        SocketIOServer server = new SocketIOServer(config);

        server.start();
        return server;
    }
}