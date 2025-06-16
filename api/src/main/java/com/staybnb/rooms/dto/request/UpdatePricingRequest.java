package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.request.vo.DateRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdatePricingRequest {

    @NotNull
    private List<DateRange> dateSelected;

    @Min(0)
    private Integer pricePerNight;

}
