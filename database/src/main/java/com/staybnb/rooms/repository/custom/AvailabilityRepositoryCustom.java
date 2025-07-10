package com.staybnb.rooms.repository.custom;

import java.util.List;

public interface AvailabilityRepositoryCustom {
    void updateRoomAvailability(long roomId, List<String> dateRanges, boolean isAvailable);
}
