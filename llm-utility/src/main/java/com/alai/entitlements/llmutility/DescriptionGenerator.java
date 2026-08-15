package com.alai.entitlements.llmutility;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class DescriptionGenerator {

    private final ChatClient chatClient;

    public DescriptionGenerator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public DescriptionResult generate(EntitlementRow row) {
        String prompt = """
                You are helping an identity-and-access-management reviewer at a bank understand \
                a cryptic entitlement name from a legacy access catalog. Someone will see your \
                description as a hover tooltip when reviewing a user's access - be concise and \
                write for a non-technical reviewer, not an engineer. Do not just restate the code.

                Application: %s (source system: %s)
                Entitlement type: %s
                Raw access code: %s
                Additional metadata (may be empty or unhelpful, use only if it adds real signal): %s

                Provide:
                - description: one or two plain-English sentences on what this access grants \
                  and who would typically need it in a banking context.
                - riskNote: ONLY if this access is meaningfully privileged or sensitive - \
                  production write/delete access, financial approval authority, domain/system \
                  administration, bulk data export, or anything that could enable fraud or a \
                  segregation-of-duties conflict - a short phrase (under 12 words) flagging why. \
                  Otherwise, leave this null. Do not invent risk that isn't there; most \
                  entitlements are routine and should get no risk note at all.
                """.formatted(
                row.applicationName(),
                row.sourceSystem(),
                row.entitlementType(),
                row.crypticTitle(),
                row.rawAttributesJson() == null ? "(none)" : row.rawAttributesJson()
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(DescriptionResult.class);
    }
}
