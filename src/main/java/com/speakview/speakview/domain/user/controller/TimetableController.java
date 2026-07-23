package com.speakview.speakview.domain.user.controller;

import com.speakview.speakview.domain.user.service.TimetableService;
import com.speakview.speakview.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Timetable", description = "시간표 이미지 관리 API")
@RestController
@RequestMapping("/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "시간표 이미지 등록/수정", description = "시간표가 없으면 새로 생성, 있으면 이미지 교체")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "시간표 이미지가 저장되고, 저장된 이미지 URL이 반환됩니다."
    ))
    public ApiResponse<String> uploadImage(
            @Parameter(description = "사용자(User) ID", example = "1", required = true)
            @RequestParam Long userId,
            @Parameter(description = "업로드할 시간표 이미지 파일", required = true)
            @RequestPart MultipartFile image
    ) throws Exception {
        String imageUrl = timetableService.uploadImage(userId, image);
        return ApiResponse.success("시간표 이미지가 저장되었습니다.", imageUrl);
    }

    @DeleteMapping("/")
    @Operation(summary = "시간표 이미지 삭제")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "시간표 이미지가 삭제됩니다."
    ))
    public ApiResponse<Void> deleteImage(
            @Parameter(description = "사용자(User) ID", example = "1", required = true)
            @RequestParam Long userId
    ) {
        timetableService.deleteImage(userId);
        return ApiResponse.success("시간표 이미지가 삭제되었습니다.", null);
    }

    @GetMapping("/")
    @Operation(summary = "시간표 이미지 조회", description = "유저 ID로 시간표 이미지 URL 조회")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "등록된 시간표 이미지 URL이 반환됩니다. 등록된 이미지가 없으면 null이 반환됩니다."
    ))
    public ApiResponse<String> getImage(
            @Parameter(description = "사용자(User) ID", example = "1", required = true)
            @RequestParam Long userId
    ) {
        String imageUrl = timetableService.getImage(userId);
        return ApiResponse.success("시간표 이미지를 조회했습니다.", imageUrl);
    }
}
