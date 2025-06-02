package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
}
