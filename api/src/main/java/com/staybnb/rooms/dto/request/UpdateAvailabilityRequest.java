package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.request.vo.DateRange;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdateAvailabilityRequest {

    @NotNull
    private List<DateRange> dateSelected;

    @NotNull
    private Boolean isAvailable;
}
