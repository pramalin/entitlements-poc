package com.alai.entitlements.model;

import java.io.Serializable;
import java.util.Objects;

public class UserEntitlementId implements Serializable {

    private Integer user;
    private Integer entitlement;

    public UserEntitlementId() {
        // JPA
    }

    public UserEntitlementId(Integer user, Integer entitlement) {
        this.user = user;
        this.entitlement = entitlement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntitlementId that)) return false;
        return Objects.equals(user, that.user) && Objects.equals(entitlement, that.entitlement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, entitlement);
    }
}
