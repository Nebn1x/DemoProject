package org.example.service;

import org.example.config.RateLimitProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Інтеграційний тест rate limiting з реальним Redis (Testcontainers).
 * Якщо Docker недоступний - тест ПРОПУСКАЄТЬСЯ (assumeTrue), а не падає.
 */
@DisplayName("RateLimitService (інтеграційний, потребує Docker)")
class RateLimitServiceIntegrationTest {

    static GenericContainer<?> redis;
    static RateLimitService service;
    static boolean dockerAvailable;

    @BeforeAll
    static void setUp() {
        try {
            dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            dockerAvailable = false;
        }
        assumeTrue(dockerAvailable, "Docker недоступний - тест пропущено");

        redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        redis.start();

        RateLimitProperties props = new RateLimitProperties();
        props.setEnabled(true);
        props.setPerEndpointPerMinute(3);
        props.setPerUserPerMinute(1000);

        service = new RateLimitService(props);
        ReflectionTestUtils.setField(service, "redisHost", redis.getHost());
        ReflectionTestUtils.setField(service, "redisPort", redis.getMappedPort(6379));
        // init() більше не потрібен - підключення ліниве, при першому checkLimits()
    }

    @AfterAll
    static void tearDown() {
        if (redis != null && redis.isRunning()) {
            redis.stop();
        }
    }

    @Test
    @DisplayName("перші 3 запити проходять, 4-й блокується")
    void blocksAfterLimit() {
        assumeTrue(dockerAvailable);

        String userHash = "testHash";
        String endpointKey = "testHash:GET:/mock/testHash/api/x";

        assertThat(service.checkLimits(userHash, endpointKey).allowed()).isTrue();
        assertThat(service.checkLimits(userHash, endpointKey).allowed()).isTrue();
        assertThat(service.checkLimits(userHash, endpointKey).allowed()).isTrue();

        RateLimitService.RateLimitResult fourth = service.checkLimits(userHash, endpointKey);
        assertThat(fourth.allowed()).isFalse();
        assertThat(fourth.retryAfterSeconds()).isGreaterThan(0);
    }
}