package com.apiGem.AiAssistantApplication.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromptLogResponse(
		String summary
		, String severity
		, String rootCause
		, String fixSuggestion) {

}
