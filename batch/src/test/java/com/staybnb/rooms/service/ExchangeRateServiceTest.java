package com.staybnb.rooms.service;

import com.staybnb.rooms.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Testcontainers
@SpringBootTest
class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("staybnb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // jpa 에서 ddl 실행 방지
        registry.add("spring.sql.init.mode", () -> "always"); // schema.sql, data.sql 스크립트 실행
    }

    @Test
    void testCurrencyService() {
        exchangeRateService.updateExchangeRates();
        exchangeRateRepository.findAll().forEach(exchangeRate -> {
            log.info("currency: {}, rate: {}", exchangeRate.getCurrency(), exchangeRate.getRate());
            assertNotNull(exchangeRate.getRate());
            assertNotNull(exchangeRate.getUpdatedAt());
        });
    }
}