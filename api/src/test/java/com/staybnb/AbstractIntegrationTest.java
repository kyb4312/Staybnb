package com.staybnb;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    static final ComposeContainer composeContainer =
            new ComposeContainer(new File("src/test/resources/docker-compose-test.yml"))
                    .withExposedService("db", 5432,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                    .withExposedService("redis", 6379,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                    .withLocalCompose(true);

    static {
        composeContainer.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Postgres
        String pgHost = composeContainer.getServiceHost("db", 5432);
        Integer pgPort = composeContainer.getServicePort("db", 5432);
        registry.add("spring.datasource.url", () -> String.format("jdbc:postgresql://%s:%d/staybnb", pgHost, pgPort));
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");

        // Redis
        String redisHost = composeContainer.getServiceHost("redis", 6379);
        Integer redisPort = composeContainer.getServicePort("redis", 6379);
        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> redisPort);
    }
}
