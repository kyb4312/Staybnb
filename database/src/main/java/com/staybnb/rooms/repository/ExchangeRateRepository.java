package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.ExchangeRate;
import com.staybnb.rooms.domain.vo.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Currency> {
}
