package com.alai.entitlements.model;

import jakarta.persistence.*;

@Entity
@Table(name = "entitlements")
public class Entitlement {

    @Id
    @Column(name = "entitlement_id")
    private Integer entitlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "cryptic_title", nullable = false)
    private String crypticTitle;

    @Column(name = "entitlement_type")
    private String entitlementType;

    // raw_attributes (jsonb) is intentionally not mapped here - the LLM utility
    // reads it directly via JDBC; the API layer doesn't need it.

    protected Entitlement() {
        // JPA
    }

    public Integer getEntitlementId() {
        return entitlementId;
    }

    public Application getApplication() {
        return application;
    }

    public String getCrypticTitle() {
        return crypticTitle;
    }

    public String getEntitlementType() {
        return entitlementType;
    }
}
