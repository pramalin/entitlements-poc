package com.alai.entitlements.repository;

import com.alai.entitlements.web.AccessItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the actual JPQL join (user_entitlements -> entitlements -> applications,
 * left join entitlement_descriptions) against a real Postgres seeded from the same
 * db/init.sql that docker-compose uses. This is the single most important test in
 * the project: it's the read path the whole prototype's pitch depends on - access
 * descriptions served from a table, no LLM call at view time.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcTemplateAutoConfiguration.class) // @DataJpaTest doesn't bring this in by default
@Testcontainers
class UserEntitlementRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of("..", "db", "init.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/01-init.sql");

    @Autowired
    UserEntitlementRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void leftJoinsDescriptionWhenOnePresent_andReturnsNullWhenAbsent() {
        // Seed a description for exactly one of Priya's three entitlements (entitlement_id 1 = SAP_FI_GL_APRV_L3).
        // The other two intentionally get no description row, to prove the LEFT JOIN doesn't drop them.
        jdbcTemplate.update(
                "insert into entitlement_descriptions (entitlement_id, description, generated_by_model) values (?, ?, ?)",
                1, "Allows approving general ledger journal entries up to authorization level 3.", "test-fixture");

        List<AccessItemDto> access = repository.findAccessForUser(1); // Priya Natarajan

        assertThat(access).hasSize(3);

        // Query orders by application name, then cryptic_title - all three are the same
        // application here, so alphabetical by title: CO_COST_CTR < FI_AP_CREATE < FI_GL_APRV
        assertThat(access).extracting(AccessItemDto::crypticTitle)
                .containsExactly("SAP_CO_COST_CTR_MAINT", "SAP_FI_AP_CREATE_INV", "SAP_FI_GL_APRV_L3");

        AccessItemDto withDescription = access.stream()
                .filter(a -> a.crypticTitle().equals("SAP_FI_GL_APRV_L3"))
                .findFirst().orElseThrow();
        assertThat(withDescription.description()).contains("general ledger");
        assertThat(withDescription.applicationName()).isEqualTo("SAP ERP Financials");

        AccessItemDto withoutDescription = access.stream()
                .filter(a -> a.crypticTitle().equals("SAP_FI_AP_CREATE_INV"))
                .findFirst().orElseThrow();
        assertThat(withoutDescription.description()).isNull();
    }

    @Test
    void returnsEmptyListForUserWithNoGrants() {
        assertThat(repository.findAccessForUser(9999)).isEmpty();
    }

    @Test
    void flaggedSegregationOfDutiesComboIsVisibleInResults() {
        // Diego (user_id 6) has both wire-initiate and wire-approve entitlements -
        // the seed data's intentional SoD flag. The repository doesn't do any
        // "flagging" itself (that's risk_note, populated later by the LLM utility),
        // but both grants must come back so the UI/LLM layer has something to flag.
        List<AccessItemDto> access = repository.findAccessForUser(6);

        assertThat(access).extracting(AccessItemDto::crypticTitle)
                .containsExactlyInAnyOrder("SAP_TREASURY_WIRE_APRV", "SAP_TREASURY_WIRE_INIT");
    }
}
