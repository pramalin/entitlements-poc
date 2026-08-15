package com.alai.entitlements.model;

import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @Column(name = "application_id")
    private Integer applicationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "source_system")
    private String sourceSystem;

    @Column(name = "description")
    private String description;

    protected Application() {
        // JPA
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public String getName() {
        return name;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getDescription() {
        return description;
    }
}
