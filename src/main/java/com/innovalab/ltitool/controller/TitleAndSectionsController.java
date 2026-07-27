package com.innovalab.ltitool.controller;

import com.innovalab.ltitool.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/v1/titleAndSections")
@CrossOrigin(origins = "${app.react-url}")
public class TitleAndSectionsController {

    private final AiService aiService;

    public TitleAndSectionsController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/generate-sections-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateSectionsStream(@RequestBody String text) {
        return aiService.generateTitleAndSections(text);
    }
}
