package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.dto.LectureAudioResponse;
import com.speakview.speakview.domain.ai.dto.TranscriptDetailResponse;
import com.speakview.speakview.domain.ai.dto.TranscriptTokenResponse;
import com.speakview.speakview.domain.ai.entity.LectureAudio;
import com.speakview.speakview.domain.ai.entity.TranscriptFull;
import com.speakview.speakview.domain.ai.entity.TranscriptToken;
import com.speakview.speakview.domain.ai.service.TranscriptQueryService;
import com.speakview.speakview.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transcript", description = "자막/오디오 조회 API")
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptQueryService transcriptQueryService;

    @GetMapping("/{contentId}/transcript")
    @Operation(
            summary = "강의 자막 상세 조회 API",
            description = "전체 자막 텍스트, 토큰(단어) 리스트, 오디오 정보를 함께 조회"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "자막/오디오 상세 정보가 반환됩니다."
    ))
    public ApiResponse<TranscriptDetailResponse> getTranscript(
            @Parameter(description = "강의(Content) ID", example = "1", required = true)
            @PathVariable Long contentId
    ) {
        TranscriptFull transcriptFull = transcriptQueryService.getTranscriptFull(contentId);
        List<TranscriptToken> tokens = transcriptQueryService.getTokens(contentId);
        LectureAudio lectureAudio = transcriptQueryService.getLectureAudio(contentId);

        List<TranscriptTokenResponse> tokenResponses = tokens.stream()
                .map(TranscriptTokenResponse::from)
                .toList();

        LectureAudioResponse audioResponse = lectureAudio == null ? null : LectureAudioResponse.from(lectureAudio);

        TranscriptDetailResponse response = TranscriptDetailResponse.builder()
                .contentId(contentId)
                .fullText(transcriptFull.getFullText())
                .tokenCount(transcriptFull.getTokenCount())
                .audio(audioResponse)
                .tokens(tokenResponses)
                .build();

        return ApiResponse.success("강의 자막 상세 조회가 완료되었습니다.", response);
    }

    @GetMapping("/{contentId}/audio")
    @Operation(
            summary = "강의 오디오 조회 API",
            description = "강의의 오디오 URL/포맷/길이 정보를 조회"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "강의 오디오 정보가 반환됩니다."
    ))
    public ApiResponse<LectureAudioResponse> getLectureAudio(
            @Parameter(description = "강의(Content) ID", example = "1", required = true)
            @PathVariable Long contentId
    ) {
        LectureAudio lectureAudio = transcriptQueryService.getLectureAudio(contentId);
        LectureAudioResponse response = LectureAudioResponse.from(lectureAudio);

        return ApiResponse.success("강의 오디오 조회가 완료되었습니다.", response);
    }

    @GetMapping("/{contentId}/tokens")
    @Operation(
            summary = "강의 토큰 구간 조회 API",
            description = "전체 토큰 또는 특정 구간(fromMs~toMs)의 토큰을 조회"
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "토큰 리스트가 반환됩니다."
    ))
    public ApiResponse<List<TranscriptTokenResponse>> getTokens(
            @Parameter(description = "강의(Content) ID", example = "1", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "조회 시작 시각(ms)", example = "10000")
            @RequestParam(required = false) Long fromMs,
            @Parameter(description = "조회 종료 시각(ms)", example = "30000")
            @RequestParam(required = false) Long toMs
    ) {
        List<TranscriptToken> tokens = transcriptQueryService.getTokens(contentId, fromMs, toMs);
        List<TranscriptTokenResponse> response = tokens.stream()
                .map(TranscriptTokenResponse::from)
                .toList();

        return ApiResponse.success("강의 토큰 조회가 완료되었습니다.", response);
    }
}