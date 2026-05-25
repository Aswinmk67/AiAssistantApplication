package com.apiGem.AiAssistantApplication.dto;

public record PromptLogResponse(
		String summary
		, String severity
		, String rootCause
		, String fixSuggestion) {

}
