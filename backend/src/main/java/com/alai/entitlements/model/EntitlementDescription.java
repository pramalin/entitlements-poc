package com.alai.entitlements.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entitlement_descriptions")
public class EntitlementDescription {

    @Id
    @Column(name = "entitlement_id")
    private Integer entitlementId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "risk_note")
    private String riskNote;

    @Column(name = "generated_by_model")
    private String generatedByModel;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    protected EntitlementDescription() {
        // JPA
    }

    public Integer getEntitlementId() {
        return entitlementId;
    }

    public String getDescription() {
        return description;
    }

    public String getRiskNote() {
        return riskNote;
    }

    public String getGeneratedByModel() {
        return generatedByModel;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
