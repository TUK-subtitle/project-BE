package com.speakview.speakview.domain.user.service;

import com.speakview.speakview.domain.user.entity.TimeTable;
import com.speakview.speakview.domain.user.entity.User;
import com.speakview.speakview.domain.user.repository.TimetableRepository;
import com.speakview.speakview.domain.user.repository.UserRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:uploads/timetable}")
    private String uploadDir;

    @Transactional
    public String uploadImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "userId=" + userId));

        String imageUrl = saveFile(file);

        TimeTable timetable = timetableRepository.findByUserId(userId)
                .orElse(TimeTable.builder().user(user).build());

        // 기존 이미지 삭제
        if (timetable.getImageUrl() != null) {
            deleteFile(timetable.getImageUrl());
        }

        TimeTable updated = TimeTable.builder()
                .id(timetable.getId())
                .user(user)
                .name(timetable.getName())
                .imageUrl(imageUrl)
                .build();

        timetableRepository.save(updated);
        return imageUrl;
    }

    @Transactional
    public void deleteImage(Long userId) {
        TimeTable timetable = timetableRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIMETABLE_NOT_FOUND, "userId=" + userId));

        if (timetable.getImageUrl() != null) {
            deleteFile(timetable.getImageUrl());
        }

        TimeTable updated = TimeTable.builder()
                .id(timetable.getId())
                .user(timetable.getUser())
                .name(timetable.getName())
                .imageUrl(null)
                .build();

        timetableRepository.save(updated);
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path dirPath = Paths.get(System.getProperty("user.dir"), uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        return "/" + uploadDir + "/" + fileName;
    }

    private void deleteFile(String imageUrl) {
        try {
            Path filePath = Paths.get(imageUrl.substring(1)); // 앞 "/" 제거
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("[파일 삭제 실패] " + imageUrl);
        }
    }

    @Transactional(readOnly = true)
    public String getImage(Long userId) {
        return timetableRepository.findByUserId(userId)
                .map(TimeTable::getImageUrl)
                .orElse(null);
    }
}