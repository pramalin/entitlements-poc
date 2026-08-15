package com.alai.entitlements.web;

import java.time.LocalDate;

public record AccessItemDto(
        Integer entitlementId,
        String crypticTitle,
        String entitlementType,
        String applicationName,
        LocalDate grantedDate,
        String description,
        String riskNote
) {
}
