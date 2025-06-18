package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.Pricing;
import com.staybnb.rooms.repository.custom.PricingRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRepository extends JpaRepository<Pricing, Long>, PricingRepositoryCustom {
}
