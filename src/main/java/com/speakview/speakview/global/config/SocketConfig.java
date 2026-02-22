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

        SocketIOServer server = new SocketIOServer(config);
        server.start();
        return server;
    }
}