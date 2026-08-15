package com.alai.entitlements.llmutility;

public record EntitlementRow(
        long entitlementId,
        String applicationName,
        String sourceSystem,
        String crypticTitle,
        String entitlementType,
        String rawAttributesJson
) {
}
