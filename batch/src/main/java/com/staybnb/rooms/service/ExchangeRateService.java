package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.ExchangeRateResponse;
import com.staybnb.rooms.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestClient restClient = RestClient.builder().build();

    @Transactional
    @Scheduled(initialDelay = 0, fixedDelay = 15 * 60 * 1000)
    public void updateExchangeRates() {
        log.info("Updating exchange rates");

        ExchangeRateResponse response = getCurrencyRate();
        exchangeRateRepository.findAll().forEach(exchangeRate -> {
            exchangeRate.setRate(response.getRates().get(exchangeRate.getCurrency().toString()));
            exchangeRate.setUpdatedAt(LocalDateTime.now());
        });

        log.info("Exchange rates updated");
    }

    private ExchangeRateResponse getCurrencyRate() {
        String baseCurrency = Currency.USD.toString();

        ExchangeRateResponse response = restClient.get()
                .uri("https://api.frankfurter.app/latest?base={base}", baseCurrency)
                .retrieve()
                .body(ExchangeRateResponse.class);

        if (response == null || response.getRates() == null) {
            throw new RuntimeException("Error getting currency rates");
        }

        response.getRates().put(baseCurrency, 1.0);
        return response;
    }
}
