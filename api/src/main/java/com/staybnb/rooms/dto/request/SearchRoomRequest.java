package com.staybnb.rooms.dto.request;

import com.staybnb.validation.annotation.NullOrNotBlank;
import com.staybnb.validation.annotation.ValidDateRange;
import com.staybnb.validation.annotation.ValidPriceRange;
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
    Integer guests;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate;

    @NullOrNotBlank
    String location;

    @Min(0)
    Integer priceFrom;

    @Min(0)
    Integer priceTo;

    String currency;

}
