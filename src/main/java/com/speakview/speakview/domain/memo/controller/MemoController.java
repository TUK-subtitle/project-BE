package com.speakview.speakview.domain.memo.controller;

import com.speakview.speakview.domain.memo.dto.MemoCreateRequest;
import com.speakview.speakview.domain.memo.dto.MemoResponse;
import com.speakview.speakview.domain.memo.service.MemoService;
import com.speakview.speakview.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memo/{contentId}")
public class MemoController {

    private final MemoService memoService;

    @PostMapping
    public ApiResponse<MemoResponse> createMemo(@PathVariable Long contentId, @Valid @RequestBody MemoCreateRequest request) {
        MemoResponse response = memoService.createMemo(contentId, request);
        return ApiResponse.success("메모가 저장되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<List<MemoResponse>> getMemos(@PathVariable Long contentId) {
        List<MemoResponse> response = memoService.getMemos(contentId);
        return ApiResponse.success("메모 조회 성공", response);
    }
}