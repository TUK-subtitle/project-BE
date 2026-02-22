package com.speakview.speakview.domain.ai.controller;

import com.speakview.speakview.domain.ai.service.RealtimeSttService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stt")
@RequiredArgsConstructor
public class RealtimeSttController {

    private final RealtimeSttService sttService;

    @PostMapping("/audio")
    public String sendAudio(@RequestBody byte[] rawAudio){
        sttService.sendAudioFrame(rawAudio);
        return "queued";
    }

    /** 전송 종료 */
    @PostMapping("/commit")
    public String commitAudio() {
        sttService.commitAudio();
        return "committed";
    }
}