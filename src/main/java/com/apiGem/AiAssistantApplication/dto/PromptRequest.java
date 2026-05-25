package com.apiGem.AiAssistantApplication.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptRequest(@NotBlank String prompt) {

}
