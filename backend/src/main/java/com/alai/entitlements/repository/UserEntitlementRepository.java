package com.alai.entitlements.repository;

import com.alai.entitlements.model.UserEntitlement;
import com.alai.entitlements.model.UserEntitlementId;
import com.alai.entitlements.web.AccessItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserEntitlementRepository
        extends JpaRepository<UserEntitlement, UserEntitlementId> {

    /**
     * The read that matters for the whole prototype: a plain join against
     * entitlement_descriptions. No LLM call happens here - descriptions were
     * generated offline by the llm-utility and just live in the table.
     */
    @Query("""
            select new com.alai.entitlements.web.AccessItemDto(
                e.entitlementId,
                e.crypticTitle,
                e.entitlementType,
                a.name,
                ue.grantedDate,
                d.description,
                d.riskNote
            )
            from UserEntitlement ue
                join ue.entitlement e
                join e.application a
                left join EntitlementDescription d on d.entitlementId = e.entitlementId
            where ue.user.userId = :userId
            order by a.name, e.crypticTitle
            """)
    List<AccessItemDto> findAccessForUser(@Param("userId") Integer userId);
}
