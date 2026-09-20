package com.example.moviecatalog.support;

import com.example.moviecatalog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The single most valuable test in this repository: the context only starts if Liquibase built the
 * schema AND Hibernate's {@code validate} agreed that every entity matches it. A drift between a
 * changelog and an entity fails the build here rather than at someone else's startup.
 */
@SpringBootTest
class SchemaMigrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void liquibaseSchema_whenApplied_thenMatchesEveryJpaEntity() {
        assertThat(userRepository.count()).isNotNegative();
    }

    @Test
    void dataInitializer_whenApplicationStarts_thenAdministratorAccountExists() {
        assertThat(userRepository.existsByUsername("admin")).isTrue();
    }
}
