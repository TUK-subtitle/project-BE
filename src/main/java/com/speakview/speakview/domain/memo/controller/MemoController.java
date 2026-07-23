package com.speakview.speakview.domain.memo.controller;

import com.speakview.speakview.domain.memo.dto.MemoCreateRequest;
import com.speakview.speakview.domain.memo.dto.MemoResponse;
import com.speakview.speakview.domain.memo.service.MemoService;
import com.speakview.speakview.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Memo", description = "강의 중 메모 작성 및 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/memo/{contentId}")
public class MemoController {

    private final MemoService memoService;

    @PostMapping
    @Operation(
            summary = "메모 작성 API",
            description = "강의 도중 특정 시점(timestamp)에 대한 메모를 작성"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "메모가 저장되고, 저장된 메모 정보가 반환됩니다."
    ))
    public ApiResponse<MemoResponse> createMemo(
            @Parameter(description = "강의(Content) ID", example = "1", required = true)
            @PathVariable Long contentId,
            @Valid @RequestBody MemoCreateRequest request
    ) {
        MemoResponse response = memoService.createMemo(contentId, request);
        return ApiResponse.success("메모가 저장되었습니다.", response);
    }

    @GetMapping
    @Operation(
            summary = "메모 목록 조회 API",
            description = "특정 강의에 작성된 메모 전체를 조회"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "해당 강의에 작성된 메모 목록이 반환됩니다."
    ))
    public ApiResponse<List<MemoResponse>> getMemos(
            @Parameter(description = "강의(Content) ID", example = "1", required = true)
            @PathVariable Long contentId
    ) {
        List<MemoResponse> response = memoService.getMemos(contentId);
        return ApiResponse.success("메모 조회 성공", response);
    }
}
