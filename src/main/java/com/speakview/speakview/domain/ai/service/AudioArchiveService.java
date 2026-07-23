package com.speakview.speakview.domain.ai.service;

import com.speakview.speakview.domain.ai.entity.Content;
import com.speakview.speakview.domain.ai.entity.LectureAudio;
import com.speakview.speakview.domain.ai.repository.ContentRepository;
import com.speakview.speakview.domain.ai.repository.LectureAudioRepository;
import com.speakview.speakview.global.exception.CustomException;
import com.speakview.speakview.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AudioArchiveService {

    private final ContentRepository contentRepository;
    private final LectureAudioRepository lectureAudioRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.key-prefix:audio}")
    private String keyPrefix;

    @Value("${aws.s3.cloudfront-domain:}")
    private String cloudfrontDomain;

    private static final int SAMPLE_RATE = 16000;
    private static final short CHANNELS = 1;
    private static final short BITS_PER_SAMPLE = 16;
    private static final String FORMAT = "wav";

    private final Map<Long, ByteArrayOutputStream> pcmBufferMap = new ConcurrentHashMap<>();

    public void appendChunk(Long contentId, byte[] pcmChunk) {
        if (contentId == null || pcmChunk == null || pcmChunk.length == 0) return;
        pcmBufferMap.computeIfAbsent(contentId, k -> new ByteArrayOutputStream()).writeBytes(pcmChunk);
    }

    @Transactional
    public LectureAudio finalizeAudio(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND, "contentId=" + contentId));

        ByteArrayOutputStream pcmBuffer = pcmBufferMap.remove(contentId);
        if (pcmBuffer == null || pcmBuffer.size() == 0) {
            throw new IllegalStateException("오디오 버퍼가 비어있습니다. contentId=" + contentId);
        }

        byte[] pcmBytes = pcmBuffer.toByteArray();
        byte[] wavBytes = toWavBytes(pcmBytes);

        String key = buildObjectKey(contentId);
        uploadToS3(key, wavBytes);

        long durationMs = calculateDurationMs(pcmBytes.length);
        String audioUrl = buildPublicUrl(key);

        LectureAudio lectureAudio = LectureAudio.builder()
                .user(content.getUser())
                .content(content)
                .audioUrl(audioUrl)
                .format(FORMAT)
                .sampleRate(SAMPLE_RATE)
                .channels((int) CHANNELS)
                .durationMs(durationMs)
                .build();

        return lectureAudioRepository.save(lectureAudio);
    }

    private String buildObjectKey(Long contentId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return keyPrefix + "/" + contentId + "_" + timestamp + ".wav";
    }

    private void uploadToS3(String key, byte[] wavBytes) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("audio/wav")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(wavBytes));
    }

    private String buildPublicUrl(String key) {
        if (cloudfrontDomain != null && !cloudfrontDomain.isBlank()) {
            return "https://" + cloudfrontDomain + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private long calculateDurationMs(int pcmByteLength) {
        int bytesPerSample = BITS_PER_SAMPLE / 8;
        long totalSamples = pcmByteLength / (bytesPerSample * CHANNELS);
        return (totalSamples * 1000L) / SAMPLE_RATE;
    }

    private byte[] toWavBytes(byte[] pcmData) {
        int dataLength = pcmData.length;
        int fileLength = 36 + dataLength;
        int byteRate = SAMPLE_RATE * CHANNELS * (BITS_PER_SAMPLE / 8);
        short blockAlign = (short) (CHANNELS * (BITS_PER_SAMPLE / 8));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeAscii(out, "RIFF");
        writeIntLE(out, fileLength);
        writeAscii(out, "WAVE");

        writeAscii(out, "fmt ");
        writeIntLE(out, 16);
        writeShortLE(out, (short) 1);
        writeShortLE(out, CHANNELS);
        writeIntLE(out, SAMPLE_RATE);
        writeIntLE(out, byteRate);
        writeShortLE(out, blockAlign);
        writeShortLE(out, BITS_PER_SAMPLE);

        writeAscii(out, "data");
        writeIntLE(out, dataLength);
        out.writeBytes(pcmData);

        return out.toByteArray();
    }

    private void writeAscii(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private void writeIntLE(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }

    private void writeShortLE(ByteArrayOutputStream out, short value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }
}