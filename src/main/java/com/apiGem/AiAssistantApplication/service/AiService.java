package com.apiGem.AiAssistantApplication.service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.apiGem.AiAssistantApplication.client.GeminiClient;
import com.apiGem.AiAssistantApplication.dto.PromptLogResponse;
import com.apiGem.AiAssistantApplication.dto.PromptResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
	
	public Mono<PromptLogResponse> processLog(FilePart filePart) {
		String archieveDirectory = "C:/ArchieveDirectory/uploads";
		
		return Mono.fromCallable(()->{
			Path uploadPath = Paths.get(archieveDirectory);
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			return uploadPath;
		}).flatMap(uploadPath->{
			String fileName = UUID.randomUUID()+"_"+filePart.name();
			Path filePath = uploadPath.resolve(fileName);
			return filePart.transferTo(filePath).thenReturn(filePath);
		}).flatMap(this:: chunkProcessing);
	}
	
	public Mono<PromptLogResponse> chunkProcessing(Path filePath) {

	    return Mono.fromCallable(() -> {

	        List<List<String>> chunks = new ArrayList<>();
	        List<String> currentChunk = new ArrayList<>();

	        try (BufferedReader reader =
	                     Files.newBufferedReader(filePath)) {

	            String line;

	            while ((line = reader.readLine()) != null) {
	                currentChunk.add(line);
	                if (currentChunk.size() == 100) {
	                    chunks.add(new ArrayList<>(currentChunk));
	                    currentChunk.clear();
	                }
	            }

	            if (!currentChunk.isEmpty()) {
	                chunks.add(currentChunk);
	            }
	        }
	        return chunks;

	    }).subscribeOn(Schedulers.boundedElastic())
	      .flatMapMany(Flux::fromIterable)
	      .flatMap(this::processChunk)
	      .collectList()
	      .flatMap(this::generateFinalLogAnalysisResult)
	      ;
	}

	private Mono<PromptLogResponse> generateFinalLogAnalysisResult(List<PromptLogResponse> responses) {

	    String combinedSummary = responses.stream()
	            .map(PromptLogResponse::toString)
	            .collect(Collectors.joining("\n"));

	    String finalPrompt = """
	            Combine these chunk analyses into a final
	            production incident report.
	            
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
				
				Combined Summaries:
	            """
	            + combinedSummary;

	    return geminiClient.generateLogContent(finalPrompt);
	}

	private Mono<PromptLogResponse> processChunk(List<String> chunkBuffer) {
		String log = String.join("\n", chunkBuffer);

	    String promptedLog = """
	            Analyze this production log.

	            Provide:
	            1. Error summary
	            2. Root cause
	            3. Severity
	            4. Suggested fix

	            Return ONLY valid JSON.
	            """
	            + log;

	    return geminiClient.generateLogContent(promptedLog);		
	}
}
