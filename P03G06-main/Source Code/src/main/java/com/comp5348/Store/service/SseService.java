package com.comp5348.Store.service;
import com.comp5348.Store.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

@Service
public class SseService {

    private final List<SseEmitter> emitters;
    private final OrderRepository orderRepository;

    @Autowired
    public SseService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.emitters = new ArrayList<>();
    }
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        // Clean up when the connection is completed or times out
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    public void sendSseEvent(String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (Exception e) {
                deadEmitters.add(emitter);  // Collect dead emitters to remove them
            }
        });
        emitters.removeAll(deadEmitters);
    }
}
