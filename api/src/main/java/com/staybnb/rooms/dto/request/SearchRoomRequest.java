package com.staybnb.rooms.dto.request;

import com.staybnb.common.validation.annotation.NullOrNotBlank;
import com.staybnb.common.validation.annotation.ValidDateRange;
import com.staybnb.common.validation.annotation.ValidPriceRange;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Builder
@ToString
@ValidDateRange
@ValidPriceRange
public class SearchRoomRequest {

    @Min(1)
    private Integer guests;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @NullOrNotBlank
    private String location;

    @Min(0)
    private Integer priceFrom;

    @Min(0)
    private Integer priceTo;

    private String currency;

}
