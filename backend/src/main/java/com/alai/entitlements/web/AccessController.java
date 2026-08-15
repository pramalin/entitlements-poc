package com.alai.entitlements.web;

import com.alai.entitlements.model.AppUser;
import com.alai.entitlements.repository.UserEntitlementRepository;
import com.alai.entitlements.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AccessController {

    private final UserRepository userRepository;
    private final UserEntitlementRepository userEntitlementRepository;

    public AccessController(UserRepository userRepository,
                             UserEntitlementRepository userEntitlementRepository) {
        this.userRepository = userRepository;
        this.userEntitlementRepository = userEntitlementRepository;
    }

    @GetMapping("/users")
    public List<UserSummaryDto> listUsers() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> a.getFullName().compareTo(b.getFullName()))
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/users/{userId}/access")
    public List<AccessItemDto> userAccess(@PathVariable Integer userId) {
        return userEntitlementRepository.findAccessForUser(userId);
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    private UserSummaryDto toSummary(AppUser u) {
        return new UserSummaryDto(
                u.getUserId(), u.getEmployeeId(), u.getFullName(),
                u.getDepartment(), u.getTitle(), u.getManagerName());
    }
}
