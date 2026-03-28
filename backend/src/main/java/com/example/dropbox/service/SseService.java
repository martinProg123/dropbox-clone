package com.example.dropbox.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long fileId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        emitters.put(fileId, emitter);
        // cleanup on complete/error/timeout
        emitter.onCompletion(() -> emitters.remove(fileId));
        emitter.onError((e) -> {
            emitters.remove(fileId);
            System.out.println("SseEmitter subscribe: " + e.getMessage());
        });
        emitter.onTimeout(() -> emitters.remove(fileId));
        return emitter;
    }

    public void emit(Long fileId, String event, Object data) throws IOException {
        SseEmitter emitter = emitters.get(fileId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (IOException e) {
                emitters.remove(fileId);
            }
        }
    }
}
