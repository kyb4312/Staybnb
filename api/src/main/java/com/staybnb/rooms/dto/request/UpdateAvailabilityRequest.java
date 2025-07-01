package com.staybnb.rooms.dto.request;

import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdateAvailabilityRequest {

    @NotNull
    private List<DateRangeRequest> dateSelected;

    @NotNull
    private Boolean isAvailable;
}
