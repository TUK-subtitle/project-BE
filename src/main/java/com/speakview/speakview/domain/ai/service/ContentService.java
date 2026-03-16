package com.speakview.speakview.domain.ai.service;

import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.domain.ai.repository.ContentRepository;

import com.speakview.speakview.domain.user.entity.Subject;
import com.speakview.speakview.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public Long createContent(Long userId, Long subjectId) {
        // 유저와 과목 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 과목을 찾을 수 없습니다. ID: " + subjectId));

        // 새로운 Content(강의 방) 생성
        Content newContent = Content.builder()
                .user(user)
                .subject(subject)
                .createAt(LocalDate.now()) // DB 컬럼 타입(Date/Timestamp)에 맞춰 조정
                .build();

        // DB에 저장 후 자동 생성된 ID 반환
        Content savedContent = contentRepository.save(newContent);
        return savedContent.getId();
    }
}