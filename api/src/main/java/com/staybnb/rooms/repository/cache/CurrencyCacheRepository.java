package com.staybnb.rooms.repository.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.staybnb.rooms.domain.Currency;
import com.staybnb.rooms.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CurrencyCacheRepository {

    private final CurrencyRepository currencyRepository;
    private final Cache<String, Currency> currencyCache = Caffeine.newBuilder().build();

    @Scheduled(initialDelay = 0, fixedDelay = 24 * 60 * 60 * 1000)
    public void loadCurrencyCache() {
        currencyRepository.findAll().forEach(currency -> currencyCache.put(currency.getCode(), currency));
    }

    public Currency getByCode(String code) {
        Currency currency = currencyCache.getIfPresent(code);
        if (currency == null) {
            throw new IllegalArgumentException("Currency가 유효하지 않습니다.");
        }
        return currency;
    }
}
