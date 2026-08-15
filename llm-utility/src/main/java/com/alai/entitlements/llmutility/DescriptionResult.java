package com.alai.entitlements.llmutility;

/**
 * The shape Claude's response gets parsed into via Spring AI's structured
 * output support (ChatClient's .entity() call). riskNote is null for most
 * entitlements - only set when the model judges the access meaningfully
 * privileged or sensitive.
 */
public record DescriptionResult(String description, String riskNote) {
}
