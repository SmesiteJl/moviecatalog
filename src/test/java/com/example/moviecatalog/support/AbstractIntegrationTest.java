package com.example.moviecatalog.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests run against a real PostgreSQL 16, because the parts most worth testing here —
 * Liquibase changelogs, the {@code ddl-auto: validate} contract and native-flavoured queries —
 * cannot be exercised on an in-memory database.
 *
 * <p>The container is static, so a single database is started for the whole test run.
 */
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
