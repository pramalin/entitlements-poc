package com.alai.entitlements.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_entitlements")
@IdClass(UserEntitlementId.class)
public class UserEntitlement {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entitlement_id")
    private Entitlement entitlement;

    @Column(name = "granted_date")
    private LocalDate grantedDate;

    protected UserEntitlement() {
        // JPA
    }

    public AppUser getUser() {
        return user;
    }

    public Entitlement getEntitlement() {
        return entitlement;
    }

    public LocalDate getGrantedDate() {
        return grantedDate;
    }
}
