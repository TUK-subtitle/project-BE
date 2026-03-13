package com.speakview.speakview.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

public class GeminiDTO {

    @Getter
    @AllArgsConstructor
    public static class Request {
        private List<Content> contents;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Part {
        private String text;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Candidate> candidates;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Candidate {
            private Content content;
        }
    }
}