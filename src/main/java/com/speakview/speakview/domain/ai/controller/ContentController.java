package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.dto.ContentRequestDTO;
import com.speakview.speakview.domain.ai.dto.ContentResponse;
import com.speakview.speakview.domain.ai.service.ContentService;
import com.speakview.speakview.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Content", description = "강의(방) 생성 및 관리 API")
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    @Operation(
            summary = "새로운 강의 방 생성 API",
            description = "수업을 시작할 때 호출하는 API, 유저 ID와 과목 ID를 받아 새로운 강의(Content)를 생성하고, 생성된 contentId를 반환"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "강의 방이 생성되고, 생성된 강의(Content)의 ID가 반환됩니다.",
            content = @Content(examples = @ExampleObject(
                    value = "{\"success\":true,\"code\":200,\"message\":\"새로운 강의가 성공적으로 생성되었습니다.\",\"data\":1}"
            ))
    ))
    public ApiResponse<Long> createContent(@RequestBody ContentRequestDTO request) {
        // 서비스 호출하여 새로운 방 만들고 ID 가져오기
        Long newContentId = contentService.createContent(request.getUserId(), request.getSubjectId());

        // 프론트엔드에게 생성된 ID 반환
        return ApiResponse.success("새로운 강의가 성공적으로 생성되었습니다.", newContentId);
    }

    @GetMapping
    @Operation(
            summary = "마이페이지 강의 목록 조회 API",
            description = "유저 ID로 강의(Content) 목록을 조회, subjectName을 함께 전달하면 해당 수업으로 필터링"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "강의 목록이 생성 일시 최신순으로 반환됩니다. 각 항목은 제목과 생성 일시(KST)만 포함합니다."
    ))
    public ApiResponse<List<ContentResponse>> getContents(
            @Parameter(description = "사용자(User) ID", example = "1", required = true)
            @RequestParam Long userId,
            @Parameter(description = "과목(Subject) 이름, 전달하지 않으면 전체 강의 목록 조회", example = "자료구조")
            @RequestParam(required = false) String subjectName
    ) {
        List<ContentResponse> contents = contentService.getContents(userId, subjectName);
        return ApiResponse.success("강의 목록을 조회했습니다.", contents);
    }
}
