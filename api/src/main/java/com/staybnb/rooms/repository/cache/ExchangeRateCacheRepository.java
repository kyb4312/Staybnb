package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.staybnb.rooms.domain.ExchangeRate;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class ExchangeRateCacheRepository {

    private final ExchangeRateRepository exchangeRateRepository;
    private final LoadingCache<Currency, Optional<ExchangeRate>> exchangeRateCache;

    public ExchangeRateCacheRepository(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(this.exchangeRateRepository::findById);
    }

    public ExchangeRate getExchangeRate(Currency currency) {
        Optional<ExchangeRate> exchangeRate = exchangeRateCache.get(currency);
        if (exchangeRate.isEmpty()) {
            throw new IllegalArgumentException("Currency가 유효하지 않습니다: " + currency);
        }
        return exchangeRate.get();
    }
}
