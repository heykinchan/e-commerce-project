package com.comp5348.Store.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.comp5348.Store.service.SseService;

@RestController
@RequestMapping("/sse")
public class SseController {

    private final SseService sseService;

    @Autowired
    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return sseService.createEmitter();
    }
}
