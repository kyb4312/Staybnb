package com.staybnb.rooms.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PricingResponse {

    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String currency;
}
