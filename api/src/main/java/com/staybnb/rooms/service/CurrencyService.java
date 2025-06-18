package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Currency;
import com.staybnb.rooms.repository.cache.CurrencyCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyCacheRepository currencyRepository;

    public Currency getByCode(String code) {
        return currencyRepository.getByCode(code);
    }

    public double convert(Currency from, Currency to, int amount) {
        return amount * (to.getExchangeRate() / from.getExchangeRate());
    }

}
