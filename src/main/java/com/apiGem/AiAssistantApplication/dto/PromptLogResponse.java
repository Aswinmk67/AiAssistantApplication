package com.apiGem.AiAssistantApplication.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromptLogResponse(
		@JsonAlias({"summary", "error_summary"})
		String summary
		, String severity
		, String rootCause
		, String fixSuggestion) {

}
