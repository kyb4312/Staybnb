package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Availability;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.vo.DateRange;
import com.staybnb.rooms.exception.InvalidDateRangeException;
import com.staybnb.rooms.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    private final RoomService roomService;

    /**
     * 선택 구간들 숙박 가능 여부 변경
     */
    @Transactional
    public void updateSelectedDatesAvailability(long roomId, UpdateAvailabilityRequest request) {
        Room room = roomService.findById(roomId);
        validateDateSelected(request.getDateSelected());

        request.getDateSelected().forEach(range ->
                updateAvailability(room, range.getStartDate(), range.getEndDate(), request.getIsAvailable()));
    }

    /**
     * startDate ~ endDate 구간 숙박 가능 여부 변경
     */
    public void updateAvailability(Room room, LocalDate startDateInclusive, LocalDate endDateInclusive, boolean isAvailable) {
        updateConflictedAvailability(room, startDateInclusive, endDateInclusive);
        availabilityRepository.save(new Availability(room, startDateInclusive, endDateInclusive, isAvailable));
    }

    /**
     * 날짜가 겹치는 기존 availability 데이터가 있을 경우,
     * 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updateConflictedAvailability(Room room, LocalDate startDateInclusive, LocalDate endDateInclusive) {
        List<Availability> conflictedAvailabilities = availabilityRepository.findAvailabilitiesByDate(room.getId(), startDateInclusive, endDateInclusive);
        availabilityRepository.deleteAll(conflictedAvailabilities);

        for (Availability conflicted : conflictedAvailabilities) {
            if (conflicted.getStartDate().isBefore(startDateInclusive)) {
                availabilityRepository.save(new Availability(room, conflicted.getStartDate(), startDateInclusive.minusDays(1), conflicted.isAvailable()));
            }
            if (conflicted.getEndDate().isAfter(endDateInclusive)) {
                availabilityRepository.save(new Availability(room, endDateInclusive.plusDays(1), conflicted.getEndDate(), conflicted.isAvailable()));
            }
        }
    }

    public List<Availability> findAvailabilitiesByMonth(Long roomId, YearMonth yearMonth) {
        return availabilityRepository.findAvailabilitiesByMonth(roomId, yearMonth);
    }

    public boolean isAvailable(long roomId, LocalDate checkInDateInclusive, LocalDate checkOutDateExclusive) {
        LocalDate date = checkInDateInclusive;
        List<Availability> availabilities = availabilityRepository.findTrueAvailabilitiesByDate(roomId, checkInDateInclusive, checkOutDateExclusive.minusDays(1));
        for (Availability availability : availabilities) {
            if (availability.getStartDate().isAfter(date)) {
                return false;
            }
            date = availability.getEndDate().plusDays(1);
        }
        return !date.isBefore(checkOutDateExclusive);
    }

    private void validateDateSelected(List<DateRange> dateSelected) {
        dateSelected.forEach(dateRange -> {
            if (dateRange.getStartDate().isBefore(LocalDate.now())) {
                throw new InvalidDateRangeException("startDate가 과거 일자입니다.", dateRange.getStartDate(), LocalDate.now());
            }
            if (dateRange.getStartDate().isAfter(dateRange.getEndDate())) {
                throw new InvalidDateRangeException("startDate는 endDate 보다 같거나 이전 일자여야 합니다.", dateRange.getStartDate(), dateRange.getEndDate());
            }
            if (!dateRange.getEndDate().isBefore(LocalDate.now().plusYears(1))) {
                throw new InvalidDateRangeException("1년 이내의 가격만 설정 가능합니다.", dateRange.getStartDate(), dateRange.getEndDate());
            }
        });
    }

}
