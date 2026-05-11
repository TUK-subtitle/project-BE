package com.speakview.speakview.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class GeminiDTO {

    // 🔥 @Data와 @NoArgsConstructor를 추가하여 완벽한 JSON 직렬화/역직렬화를 보장합니다.
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private List<Content> contents;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part {
        private String text;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Candidate> candidates;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Candidate {
            private Content content;
        }
    }
}