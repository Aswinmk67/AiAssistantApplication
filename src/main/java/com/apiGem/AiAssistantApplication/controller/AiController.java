package com.apiGem.AiAssistantApplication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.apiGem.AiAssistantApplication.dto.PromptLogResponse;
import com.apiGem.AiAssistantApplication.dto.PromptRequest;
import com.apiGem.AiAssistantApplication.dto.PromptResponse;
import com.apiGem.AiAssistantApplication.service.AiService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

	@Autowired
	AiService aiService;
	
	@PostMapping("/ask")
	public Mono<PromptResponse> askAi(@Valid @RequestBody PromptRequest request) {
		return aiService.askAi(request.prompt());
	}
	
	@PostMapping("/analyze")
	public Mono<PromptLogResponse> analyzeLog(@Valid @ RequestBody PromptRequest request) {
		return aiService.analyzeLog(request.prompt());
	}
	
	@PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<PromptLogResponse> uploadLog(@RequestPart(name = "file") FilePart filePart) {
		return aiService.processLog(filePart);
	}
}
