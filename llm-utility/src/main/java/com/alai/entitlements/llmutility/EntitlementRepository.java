package com.alai.entitlements.llmutility;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EntitlementRepository {

    private final JdbcTemplate jdbcTemplate;

    public EntitlementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Every entitlement that doesn't already have a row in entitlement_descriptions.
     * Re-running the utility after adding new entitlements only processes the new ones -
     * this is what makes it safe to run again rather than something that must be run
     * exactly once ever.
     */
    public List<EntitlementRow> findPending() {
        return jdbcTemplate.query("""
                select e.entitlement_id, a.name as application_name, a.source_system,
                       e.cryptic_title, e.entitlement_type, e.raw_attributes::text as raw_attributes
                from entitlements e
                join applications a on a.application_id = e.application_id
                left join entitlement_descriptions d on d.entitlement_id = e.entitlement_id
                where d.entitlement_id is null
                order by a.name, e.cryptic_title
                """,
                (rs, rowNum) -> new EntitlementRow(
                        rs.getLong("entitlement_id"),
                        rs.getString("application_name"),
                        rs.getString("source_system"),
                        rs.getString("cryptic_title"),
                        rs.getString("entitlement_type"),
                        rs.getString("raw_attributes")
                ));
    }

    public void save(long entitlementId, DescriptionResult result, String modelName) {
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement("""
                    insert into entitlement_descriptions
                        (entitlement_id, description, risk_note, generated_by_model, generated_at)
                    values (?, ?, ?, ?, ?)
                    """);
            ps.setLong(1, entitlementId);
            ps.setString(2, result.description());
            if (result.riskNote() == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, result.riskNote());
            }
            ps.setString(4, modelName);
            ps.setObject(5, LocalDateTime.now());
            return ps;
        });
    }
}
