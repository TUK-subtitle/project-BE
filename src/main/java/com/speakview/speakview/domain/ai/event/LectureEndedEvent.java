package com.speakview.speakview.domain.ai.event;

public class LectureEndedEvent {
    private final Long contentId;
    private final Object source;

    public LectureEndedEvent(Object source, Long contentId) {
        this.source = source;
        this.contentId = contentId;
    }

    public Long getContentId() {
        return contentId;
    }

    public Object getSource() {
        return source;
    }
}