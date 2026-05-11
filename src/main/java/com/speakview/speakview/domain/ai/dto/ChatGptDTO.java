package com.speakview.speakview.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.message.Message;

import java.awt.*;
import java.util.List;

public class ChatGptDTO {

    @Getter
    @AllArgsConstructor
    public static class Request {
        private String model;
        private List<Message> messages;
        private double temperature;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Choice> choices;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            private Message message;
        }
    }
}
