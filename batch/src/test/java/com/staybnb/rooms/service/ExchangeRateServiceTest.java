package com.staybnb.rooms.service;

import com.staybnb.rooms.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

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