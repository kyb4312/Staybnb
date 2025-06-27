package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdatePricingRequest {

    @NotNull
    private List<DateRangeRequest> dateSelected;

    @Min(0)
    private Integer pricePerNight;

}
