package com.apiGem.AiAssistantApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apiGem.AiAssistantApplication.client.GeminiClient;
import com.apiGem.AiAssistantApplication.dto.PromptLogResponse;
import com.apiGem.AiAssistantApplication.dto.PromptResponse;

import reactor.core.publisher.Mono;

@Service
public class AiService {
	
	@Autowired
	GeminiClient geminiClient;
	
	public Mono<PromptResponse> askAi(String prompt) {
		return geminiClient.generateContent(prompt).map(PromptResponse::new);
	}
	
	public Mono<PromptLogResponse> analyzeLog(String log) {
		String promptedLog = """
				Analyze this production log.

				Provide:
				1. Error summary
				2. Root cause
				3. Severity
				4. Suggested fix

				Return ONLY valid JSON.

				JSON format:
				{
				  "summary": "",
				  "severity": "",
				  "rootCause": "",
				  "fixSuggestion": ""
				}

				Log:
				""" + log;
		
		return geminiClient.generateLogContent(promptedLog);
	}
}
