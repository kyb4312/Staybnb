package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.repository.cache.ExchangeRateCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateCacheRepository exchangeRateRepository;

    public double convertToUSD(Currency from, int amount) {
        return amount * (exchangeRateRepository.getExchangeRate(Currency.USD).getRate() / exchangeRateRepository.getExchangeRate(from).getRate());
    }

    public double convert(Currency from, Currency to, int amount) {
        return amount * (exchangeRateRepository.getExchangeRate(to).getRate() / exchangeRateRepository.getExchangeRate(from).getRate());
    }

}
