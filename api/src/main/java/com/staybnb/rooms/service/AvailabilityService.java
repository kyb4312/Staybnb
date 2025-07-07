package com.staybnb.rooms.service;

import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.rooms.domain.Availability;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.vo.DateRange;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    private final RoomService roomService;

    @Transactional
    public void updateSelectedDatesAvailability(long userId, long roomId, UpdateAvailabilityRequest request) {
        Room room = roomService.findById(roomId);
        validateUser(userId, room);
        DateRangeRequest.sortAndValidateDateSelected(request.getDateSelected());

        // DateRangeRequest는 endDate가 exclusive인 DateRange로 변경 후 전달
        updateAvailabilities(room,
                request.getDateSelected().stream().map(DateRangeRequest::toDateRange).toList(),
                request.getIsAvailable());
    }

    @Transactional
    public void updateAvailabilityToFalse(Room room, LocalDate startDateInclusive, LocalDate endDateExclusive) {
        updateAvailabilities(room, List.of(new DateRange(startDateInclusive, endDateExclusive)), false);
    }

    /**
     * dateRanges 날짜 범위에 대한 availability 데이터 추가.
     * dateRanges 전체 날짜 범위와 겹치는 기존 availability 데이터가 있을 경우, 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updateAvailabilities(Room room, List<DateRange> sortedSelectedDateRanges, boolean isAvailable) {
        LocalDate minStartDate = sortedSelectedDateRanges.getFirst().getStartDate();
        LocalDate maxEndDate = sortedSelectedDateRanges.getLast().getEndDate();

        // 업데이트 할 dateRanges 구간과 겹치는 데이터 조회
        List<Availability> sortedConflictedAvailabilities = availabilityRepository
                .findOrderedAvailabilitiesByDate(room.getId(), minStartDate, maxEndDate);

        // 충돌 하는 범위 내 데이터 전체 삭제
        if (!sortedConflictedAvailabilities.isEmpty()) {
            availabilityRepository.deleteAll(sortedConflictedAvailabilities);
            availabilityRepository.flush();
        }

        // 새로 요청 온 날짜 구간(dateRanges)과 기존 데이터 중 겹치지 않는 구간의 데이터 저장
        List<Availability> newAvailabilities = new ArrayList<>();

        for (DateRange range : sortedSelectedDateRanges) {
            newAvailabilities.add(new Availability(room, range.getStartDate(), range.getEndDate(), isAvailable));
        }

        addNonOverlappingRanges(newAvailabilities, room, sortedConflictedAvailabilities, sortedSelectedDateRanges);

        availabilityRepository.saveAll(newAvailabilities);
    }

    /**
     * sortedConflicted 범위 중 sortedSelected 와 겹치지 않는 구간을 newAvailabilities 리스트에 추가
     */
    private void addNonOverlappingRanges(List<Availability> newAvailabilities, Room room,
                                         List<Availability> sortedConflicted, List<DateRange> sortedSelected) {
        int selectedIndex = 0;
        LocalDate currentStart = null;
        LocalDate currentEnd = null;

        for (Availability conflicted : sortedConflicted) {
            currentStart = conflicted.getStartDate();
            currentEnd = conflicted.getEndDate();

            // 현재 conflicted와 겹치는 selected 들을 모두 처리
            while (selectedIndex < sortedSelected.size()) {
                DateRange selected = sortedSelected.get(selectedIndex);

                // selected가 conflicted 보다 완전히 앞에 있으면 다음 selected 로
                if (selected.getEndDate().isBefore(currentStart) || selected.getEndDate().isEqual(currentStart)) {
                    selectedIndex++;
                    continue;
                }

                // 안 겹치는 conflicted 앞 부분 복원
                if (currentStart.isBefore(selected.getStartDate())) {
                    newAvailabilities.add(new Availability(room, currentStart, selected.getStartDate(), conflicted.isAvailable()));
                }
                currentStart = selected.getEndDate();

                // currentStart가 currentEnd와 같거나 더 이후라면, 현재 conflicted에 대한 처리를 완료한 것
                if (!currentStart.isBefore(currentEnd)) {
                    break;
                }
            }
        }

        // 마지막 conflicted 뒷 부분 복원
        if (currentStart != null && currentStart.isBefore(currentEnd)) {
            newAvailabilities.add(new Availability(room, currentStart, currentEnd, sortedConflicted.getLast().isAvailable()));
        }
    }

    public List<Availability> findAvailabilitiesByMonth(Long roomId, YearMonth yearMonth) {
        return availabilityRepository.findAvailabilitiesByDate(roomId, yearMonth.atDay(1), yearMonth.plusMonths(1).atDay(1));
    }

    public boolean isAvailable(long roomId, LocalDate checkInDateInclusive, LocalDate checkOutDateExclusive) {
        LocalDate date = checkInDateInclusive;
        List<Availability> availabilities = availabilityRepository.findTrueAvailabilitiesByDate(roomId, checkInDateInclusive, checkOutDateExclusive);
        for (Availability availability : availabilities) {
            if (availability.getStartDate().isAfter(date)) {
                return false;
            }
            date = availability.getEndDate();
        }
        return !date.isBefore(checkOutDateExclusive);
    }

    public boolean isAvailableForUpdate(long roomId, LocalDate checkInDateInclusive, LocalDate checkOutDateExclusive) {
        LocalDate date = checkInDateInclusive;
        List<Availability> availabilities = availabilityRepository.findTrueAvailabilitiesByDateForUpdate(roomId, checkInDateInclusive, checkOutDateExclusive);
        for (Availability availability : availabilities) {
            if (availability.getStartDate().isAfter(date)) {
                return false;
            }
            date = availability.getEndDate();
        }
        return !date.isBefore(checkOutDateExclusive);
    }

    private void validateUser(long userId, Room room) {
        if (!room.getHost().getId().equals(userId)) {
            throw new UnauthorizedException(userId);
        }
    }

}
