package com.alai.entitlements.web;

public record UserSummaryDto(
        Integer userId,
        String employeeId,
        String fullName,
        String department,
        String title,
        String managerName
) {
}
