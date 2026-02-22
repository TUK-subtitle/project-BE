package com.speakview.speakview.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealtimeResponseDTO {

    private String type;
    private String delta;   // partial 텍스트
    private String text;    // final 텍스트
}