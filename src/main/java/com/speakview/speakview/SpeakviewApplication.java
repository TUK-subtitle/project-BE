package com.speakview.speakview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 타이머 기능 활성화
@EnableJpaAuditing	// JPA Auditing 활성화
@EnableAsync // @Async 비동기 실행 활성화
@SpringBootApplication
public class SpeakviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeakviewApplication.class, args);
	}

}