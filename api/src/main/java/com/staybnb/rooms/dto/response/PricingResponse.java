package com.staybnb.rooms.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PricingResponse {

    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String currency;
}
