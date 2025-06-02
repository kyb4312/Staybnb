package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Currency;
import com.staybnb.rooms.repository.CurrencyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final Map<String, Currency> CurrencyMap = new HashMap<>();

    @PostConstruct
    public void loadCurrencyMap() {
        currencyRepository.findAll().forEach(currency -> CurrencyMap.put(currency.getCode(), currency));
    }

    public Currency getByCode(String code) {
        return CurrencyMap.get(code);
    }
}
