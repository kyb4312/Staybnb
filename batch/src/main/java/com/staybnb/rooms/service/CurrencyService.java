package com.staybnb.rooms.service;

import com.staybnb.rooms.dto.CurrencyRateResponse;
import com.staybnb.rooms.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final RestClient restClient = RestClient.builder().build();

    @Transactional
    @Scheduled(initialDelay = 0, fixedDelay = 15 * 60 * 1000)
    public void updateCurrencyRates() {
        CurrencyRateResponse response = getCurrencyRate();

        currencyRepository.findAll().forEach(currency -> {
            currency.setExchangeRate(response.getRates().get(currency.getCode()));
            currency.setUpdatedAt(LocalDateTime.now());
        });

    }

    private CurrencyRateResponse getCurrencyRate() {
        String baseCurrency = "USD";

        CurrencyRateResponse response = restClient.get()
                .uri("https://api.frankfurter.app/latest?base={base}", baseCurrency)
                .retrieve()
                .body(CurrencyRateResponse.class);

        if (response == null || response.getRates() == null) {
            throw new RuntimeException("Error getting currency rates");
        }

        response.getRates().put(baseCurrency, 1.0);
        return response;
    }
}
