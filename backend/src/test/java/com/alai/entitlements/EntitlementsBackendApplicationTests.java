package com.alai.entitlements;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;

/**
 * The cheapest possible test with the highest signal: does the whole
 * application context start up against a real Postgres running the
 * actual db/init.sql schema from step 1? If a bean is misconfigured,
 * this fails fast without needing to hit any endpoint.
 */
@SpringBootTest
@Testcontainers
class EntitlementsBackendApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of("..", "db", "init.sql").toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/01-init.sql");

    @Test
    void contextLoads() {
        // If the context fails to start, this test fails - that's the whole point.
    }
}
