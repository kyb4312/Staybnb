package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.request.vo.DateRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdatePricingRequest {

    @NotNull
    List<DateRange> dateSelected;

    @Min(0)
    private Integer pricePerNight;

}
