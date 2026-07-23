package com.speakview.speakview.domain.ai.service;

import com.speakview.speakview.domain.ai.dto.ContentResponse;
import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.domain.ai.repository.ContentRepository;

import com.speakview.speakview.domain.user.entity.Subject;
import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.domain.user.repository.SubjectRepository;
import com.speakview.speakview.domain.user.repository.UserRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "userId=" + userId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBJECT_NOT_FOUND, "subjectId=" + subjectId));

        // 새로운 Content(강의 방) 생성
        Content newContent = Content.builder()
                .user(user)
                .subject(subject)
                .build();

        // DB에 저장 후 자동 생성된 ID 반환
        Content savedContent = contentRepository.save(newContent);
        return savedContent.getId();
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> getContents(Long userId, String subjectName) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "userId=" + userId);
        }

        List<Content> contents = (subjectName != null)
                ? contentRepository.findByUser_IdAndSubject_NameOrderByCreatedAtDesc(userId, subjectName)
                : contentRepository.findByUser_IdOrderByCreatedAtDesc(userId);

        return contents.stream()
                .map(ContentResponse::from)
                .toList();
    }

    @Transactional
    public void updateTitle(Long contentId, String title) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "contentId=" + contentId));

        content.updateTitle(title);
    }

    @Transactional(readOnly = true)
    public String getTitle(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "contentId=" + contentId));

        return content.getTitle();
    }
}
