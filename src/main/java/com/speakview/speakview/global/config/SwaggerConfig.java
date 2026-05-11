package com.speakview.speakview.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Speakview API 명세서")
                .description("실시간 자막 브로드캐스트 서비스 API")
                .version("1.0.0");

        return new OpenAPI()
                .info(info)
                // 배포 환경에서 https/http 경로 문제를 방지하기 위해 설정
                .addServersItem(new Server().url("/"));
    }
}