package com.apiGem.AiAssistantApplication.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.apiGem.AiAssistantApplication.dto.GeminiRespose;
import com.apiGem.AiAssistantApplication.dto.PromptLogResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
	
@Component
public class GeminiClient {

	private final WebClient webClient;
		
	private final String API_KEY;

	private final String BASE_URL;
	
	private final String AI_MODEL;
	
	private final ObjectMapper objectMapper;

	public GeminiClient(WebClient webClient,
			ObjectMapper objectMapper,
			@Value("${gemini.api.key}") String API_KEY, 
			@Value("${gemini.api.baseUrl}") String BASE_URL, 
			@Value("${gemini.api.model}") String AI_MODEL ) {
		this.webClient = webClient;
		this.objectMapper=objectMapper;
		this.API_KEY = API_KEY;
		this.BASE_URL = BASE_URL;
		this.AI_MODEL = AI_MODEL;
	}
	
	public Mono<String> generateContent(String prompt) {
		//baseUrl-> https://generativelanguage.googleapis.com
		//model -> gemini-2.5-flash
		//gemini structure -> { "contents": [{ "parts": [{ "text": "your prompt" }] }] }
		
		String url = BASE_URL+"/v1beta/models/"+AI_MODEL+":generateContent?key="+API_KEY;
		
		Map<String, Object> requestBody = Map.of(
				"contents", List.of(
								Map.of("parts", List.of(
										Map.of("text", prompt)
														)
									  )
								   )
		);
		
		return webClient.post()
				.uri(url)
				.bodyValue(requestBody)
				.retrieve()
				//.bodyToMono(String.class);
				.bodyToMono(GeminiRespose.class)
				.map(g -> g.candidates().get(0).content().parts().get(0).text())
				.onErrorReturn("Error parsing the response payload of gemini api!!");
	}
	
	public Mono<PromptLogResponse> generateLogContent(String promptedLog) {
		String url = BASE_URL+"/v1beta/models/"+AI_MODEL+":generateContent?key="+API_KEY;
		
		Map<String, Object> requestBody = Map.of("contents",List.of(
				Map.of("parts", List.of(
							Map.of("text", promptedLog)
									   )
					  )
				)
		);
		
		return webClient.post()
		.uri(url)
		.bodyValue(requestBody)
		.retrieve()
		.bodyToMono(GeminiRespose.class)
		.map(g -> g.candidates().get(0).content().parts().get(0).text())
		.map(json ->{
				try {
					json = json.replace("```json", "").replace("```", "").trim();
					return objectMapper.readValue(json, PromptLogResponse.class);
				} catch (Exception e) {
					throw new RuntimeException("Failed to parse Gemini JSON response",e);
				}
		});
		
		
	}	
}
