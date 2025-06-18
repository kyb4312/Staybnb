package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.staybnb.rooms.domain.Currency;
import com.staybnb.rooms.repository.CurrencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class CurrencyCacheRepository {

    private final CurrencyRepository currencyRepository;
    private final LoadingCache<String, Optional<Currency>> currencyCache;

    public CurrencyCacheRepository(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
        this.currencyCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(this.currencyRepository::findByCode);
    }

    public Currency getByCode(String code) {
        Optional<Currency> currency = currencyCache.get(code);
        if (currency.isEmpty()) {
            throw new IllegalArgumentException("Currency가 유효하지 않습니다. Invalid code: " + code);
        }
        return currency.get();
    }
}
