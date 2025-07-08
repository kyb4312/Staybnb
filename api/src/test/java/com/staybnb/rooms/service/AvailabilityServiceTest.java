package com.staybnb.rooms.service;

import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.rooms.domain.Availability;
import com.staybnb.rooms.domain.Room;
import com.staybnb.users.domain.User;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.repository.AvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @InjectMocks
    private AvailabilityService availabilityService;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private RoomService roomService;

    @Captor
    private ArgumentCaptor<List<Availability>> availabilityListCaptor;

    private Room room;
    private final long HOST_ID = 1L;
    private final long OTHER_USER_ID = 2L;
    private final long ROOM_ID = 100L;

    @BeforeEach
    void setUp() {
        User host = new User("host@example.com", "host", "password");
        host.setId(HOST_ID);

        room = Room.builder().id(ROOM_ID).host(host).build();
    }

    @Nested
    @DisplayName("updateSelectedDatesAvailability Tests")
    class UpdateSelectedDatesAvailability {

        @Test
        @DisplayName("성공: 기존 데이터가 없을 때 새로운 available 기간 추가")
        void givenNoExistingAvailabilities_whenUpdate_thenSavesNewAvailability() {
            // Given
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(10);
            var request = new UpdateAvailabilityRequest(
                    List.of(new DateRangeRequest(start, end)), true
            );

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(availabilityRepository.findOrderedAvailabilitiesByDate(ROOM_ID, start, end.plusDays(1)))
                    .thenReturn(Collections.emptyList());

            // When
            availabilityService.updateSelectedDatesAvailability(HOST_ID, ROOM_ID, request);

            // Then
            verify(availabilityRepository).saveAll(availabilityListCaptor.capture());
            List<Availability> savedAvailabilities = availabilityListCaptor.getValue();

            assertThat(savedAvailabilities).hasSize(1);
            assertThat(savedAvailabilities.getFirst().getRoom()).isEqualTo(room);
            assertThat(savedAvailabilities.getFirst().getStartDate()).isEqualTo(start);
            assertThat(savedAvailabilities.getFirst().getEndDate()).isEqualTo(end.plusDays(1));
            assertThat(savedAvailabilities.getFirst().isAvailable()).isTrue();
        }

        @Test
        @DisplayName("성공: 새로운 unavailable 기간이 기존 available 기간을 둘로 나눌 때")
        void givenUnavailableRange_whenSplitsExistingAvailableRange_thenSavesThreeRanges() {
            // Given
            LocalDate originalStart = LocalDate.now();
            LocalDate originalEnd = LocalDate.now().plusDays(20);
            Availability existingAvailability = new Availability(room, originalStart, originalEnd, true);

            LocalDate newStart = LocalDate.now().plusDays(5);
            LocalDate newEnd = LocalDate.now().plusDays(10);
            var request = new UpdateAvailabilityRequest(
                    List.of(new DateRangeRequest(newStart, newEnd)), false
            );

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(availabilityRepository.findOrderedAvailabilitiesByDate(ROOM_ID, newStart, newEnd.plusDays(1)))
                    .thenReturn(List.of(existingAvailability));

            // When
            availabilityService.updateSelectedDatesAvailability(HOST_ID, ROOM_ID, request);

            // Then
            verify(availabilityRepository).deleteAll(List.of(existingAvailability));
            verify(availabilityRepository).saveAll(availabilityListCaptor.capture());
            List<Availability> savedAvailabilities = availabilityListCaptor.getValue();
            savedAvailabilities.sort(Comparator.comparing(Availability::getStartDate));

            assertThat(savedAvailabilities).hasSize(3);
            // 1. Original part 1 (available)
            assertThat(savedAvailabilities.get(0).getStartDate()).isEqualTo(originalStart);
            assertThat(savedAvailabilities.get(0).getEndDate()).isEqualTo(newStart);
            assertThat(savedAvailabilities.get(0).isAvailable()).isTrue();
            // 2. New part (unavailable)
            assertThat(savedAvailabilities.get(1).getStartDate()).isEqualTo(newStart);
            assertThat(savedAvailabilities.get(1).getEndDate()).isEqualTo(newEnd.plusDays(1));
            assertThat(savedAvailabilities.get(1).isAvailable()).isFalse();
            // 3. Original part 2 (available)
            assertThat(savedAvailabilities.get(2).getStartDate()).isEqualTo(newEnd.plusDays(1));
            assertThat(savedAvailabilities.get(2).getEndDate()).isEqualTo(originalEnd);
            assertThat(savedAvailabilities.get(2).isAvailable()).isTrue();
        }

        @Test
        @DisplayName("성공: 새로운 unavailable 기간이 기존 available 기간을 여러 구간으로 나눌 때")
        void givenUnavailableRange_whenSplitsExistingAvailableRange_thenSavesMultiRanges() {
            // Given
            LocalDate originalStart1 = LocalDate.now().plusDays(1);
            LocalDate originalEnd1 = LocalDate.now().plusDays(7);
            LocalDate originalStart2 = LocalDate.now().plusDays(8);
            LocalDate originalEnd2 = LocalDate.now().plusDays(9);
            LocalDate originalStart3 = LocalDate.now().plusDays(12);
            LocalDate originalEnd3 = LocalDate.now().plusDays(13);
            LocalDate originalStart4 = LocalDate.now().plusDays(14);
            LocalDate originalEnd4 = LocalDate.now().plusDays(22);
            Availability existingAvailability1 = new Availability(room, originalStart1, originalEnd1, true);
            Availability existingAvailability2 = new Availability(room, originalStart2, originalEnd2, true);
            Availability existingAvailability3 = new Availability(room, originalStart3, originalEnd3, true);
            Availability existingAvailability4 = new Availability(room, originalStart4, originalEnd4, true);

            LocalDate newStart1 = LocalDate.now().plusDays(3);
            LocalDate newEnd1 = LocalDate.now().plusDays(4);
            LocalDate newStart2 = LocalDate.now().plusDays(10);
            LocalDate newEnd2 = LocalDate.now().plusDays(15);
            LocalDate newStart3 = LocalDate.now().plusDays(18);
            LocalDate newEnd3 = LocalDate.now().plusDays(19);
            LocalDate newStart4 = LocalDate.now().plusDays(23);
            LocalDate newEnd4 = LocalDate.now().plusDays(24);
            var request = new UpdateAvailabilityRequest(
                    List.of(
                            new DateRangeRequest(newStart1, newEnd1),
                            new DateRangeRequest(newStart2, newEnd2),
                            new DateRangeRequest(newStart3, newEnd3),
                            new DateRangeRequest(newStart4, newEnd4)
                    ), false
            );

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(availabilityRepository.findOrderedAvailabilitiesByDate(ROOM_ID, newStart1, newEnd4.plusDays(1)))
                    .thenReturn(List.of(
                            existingAvailability1,
                            existingAvailability2,
                            existingAvailability3,
                            existingAvailability4
                    ));

            // When
            availabilityService.updateSelectedDatesAvailability(HOST_ID, ROOM_ID, request);

            // Then
            verify(availabilityRepository).deleteAll(List.of(
                    existingAvailability1,
                    existingAvailability2,
                    existingAvailability3,
                    existingAvailability4)
            );
            verify(availabilityRepository).saveAll(availabilityListCaptor.capture());
            List<Availability> savedAvailabilities = availabilityListCaptor.getValue();
            savedAvailabilities.sort(Comparator.comparing(Availability::getStartDate));

            assertThat(savedAvailabilities).hasSize(9);
            // 1. Original part 1 (available)
            assertThat(savedAvailabilities.get(0).getStartDate()).isEqualTo(originalStart1);
            assertThat(savedAvailabilities.get(0).getEndDate()).isEqualTo(newStart1);
            assertThat(savedAvailabilities.get(0).isAvailable()).isTrue();
            // 2. New part 1 (unavailable)
            assertThat(savedAvailabilities.get(1).getStartDate()).isEqualTo(newStart1);
            assertThat(savedAvailabilities.get(1).getEndDate()).isEqualTo(newEnd1.plusDays(1));
            assertThat(savedAvailabilities.get(1).isAvailable()).isFalse();
            // 3. Original part 2 (available)
            assertThat(savedAvailabilities.get(2).getStartDate()).isEqualTo(newEnd1.plusDays(1));
            assertThat(savedAvailabilities.get(2).getEndDate()).isEqualTo(originalEnd1);
            assertThat(savedAvailabilities.get(2).isAvailable()).isTrue();
            // 3. Original part 3 (available)
            assertThat(savedAvailabilities.get(3).getStartDate()).isEqualTo(originalStart2);
            assertThat(savedAvailabilities.get(3).getEndDate()).isEqualTo(originalEnd2);
            assertThat(savedAvailabilities.get(3).isAvailable()).isTrue();
            // 4. New part 2 (unavailable)
            assertThat(savedAvailabilities.get(4).getStartDate()).isEqualTo(newStart2);
            assertThat(savedAvailabilities.get(4).getEndDate()).isEqualTo(newEnd2.plusDays(1));
            assertThat(savedAvailabilities.get(4).isAvailable()).isFalse();
            // 5. Original part 4 (available)
            assertThat(savedAvailabilities.get(5).getStartDate()).isEqualTo(newEnd2.plusDays(1));
            assertThat(savedAvailabilities.get(5).getEndDate()).isEqualTo(newStart3);
            assertThat(savedAvailabilities.get(5).isAvailable()).isTrue();
            // 6. New part 3 (unavailable)
            assertThat(savedAvailabilities.get(6).getStartDate()).isEqualTo(newStart3);
            assertThat(savedAvailabilities.get(6).getEndDate()).isEqualTo(newEnd3.plusDays(1));
            assertThat(savedAvailabilities.get(6).isAvailable()).isFalse();
            // 7. Original part 5 (available)
            assertThat(savedAvailabilities.get(7).getStartDate()).isEqualTo(newEnd3.plusDays(1));
            assertThat(savedAvailabilities.get(7).getEndDate()).isEqualTo(originalEnd4);
            assertThat(savedAvailabilities.get(7).isAvailable()).isTrue();
            // 8. New part 4 (unavailable)
            assertThat(savedAvailabilities.get(8).getStartDate()).isEqualTo(newStart4);
            assertThat(savedAvailabilities.get(8).getEndDate()).isEqualTo(newEnd4.plusDays(1));
            assertThat(savedAvailabilities.get(8).isAvailable()).isFalse();
        }

        @Test
        @DisplayName("실패: 방의 호스트가 아닌 유저가 업데이트 시도")
        void givenNonHostUser_whenUpdate_thenThrowsUnauthorizedException() {
            // Given
            var request = new UpdateAvailabilityRequest(
                    List.of(new DateRangeRequest(LocalDate.now(), LocalDate.now().plusDays(1))), true
            );
            when(roomService.findById(ROOM_ID)).thenReturn(room);

            // When & Then
            assertThrows(UnauthorizedException.class,
                    () -> availabilityService.updateSelectedDatesAvailability(OTHER_USER_ID, ROOM_ID, request));

            verify(availabilityRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("updateAvailabilityToFalse Tests")
    class UpdateAvailabilityToFalse {
        @Test
        @DisplayName("성공: 예약으로 인해 특정 기간을 unavailable로 변경")
        void givenDateRange_whenUpdateToFalse_thenCorrectlyUpdatesAndSplits() {
            // Given
            LocalDate originalStart = LocalDate.now();
            LocalDate originalEnd = LocalDate.now().plusDays(30);
            Availability existingAvailability = new Availability(room, originalStart, originalEnd, true);

            LocalDate bookingStart = LocalDate.now().plusDays(10);
            LocalDate bookingEnd = LocalDate.now().plusDays(15); // exclusive

            when(availabilityRepository.findOrderedAvailabilitiesByDate(ROOM_ID, bookingStart, bookingEnd))
                    .thenReturn(List.of(existingAvailability));

            // When
            availabilityService.updateAvailabilityToFalse(room, bookingStart, bookingEnd);

            // Then
            verify(availabilityRepository).deleteAll(List.of(existingAvailability));
            verify(availabilityRepository).saveAll(availabilityListCaptor.capture());
            List<Availability> savedAvailabilities = availabilityListCaptor.getValue();
            savedAvailabilities.sort(Comparator.comparing(Availability::getStartDate));

            assertThat(savedAvailabilities).hasSize(3);
            assertThat(savedAvailabilities.get(0).isAvailable()).isTrue();
            assertThat(savedAvailabilities.get(0).getStartDate()).isEqualTo(originalStart);
            assertThat(savedAvailabilities.get(0).getEndDate()).isEqualTo(bookingStart);

            assertThat(savedAvailabilities.get(1).isAvailable()).isFalse();
            assertThat(savedAvailabilities.get(1).getStartDate()).isEqualTo(bookingStart);
            assertThat(savedAvailabilities.get(1).getEndDate()).isEqualTo(bookingEnd);

            assertThat(savedAvailabilities.get(2).isAvailable()).isTrue();
            assertThat(savedAvailabilities.get(2).getStartDate()).isEqualTo(bookingEnd);
            assertThat(savedAvailabilities.get(2).getEndDate()).isEqualTo(originalEnd);
        }
    }

    @Nested
    @DisplayName("findAvailabilitiesByMonth Tests")
    class FindAvailabilitiesByMonth {
        @Test
        @DisplayName("성공: 특정 월의 availability 데이터를 조회")
        void givenRoomIdAndMonth_whenFind_thenReturnsListOfAvailabilities() {
            // Given
            YearMonth yearMonth = YearMonth.now().plusMonths(1);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);

            List<Availability> expectedAvailabilities = List.of(
                    new Availability(room, startOfMonth, startOfMonth.plusDays(10), true),
                    new Availability(room, startOfMonth.plusDays(10), startOfMonth.plusDays(15), false)
            );
            when(availabilityRepository.findAvailabilitiesByDate(ROOM_ID, startOfMonth, startOfNextMonth))
                    .thenReturn(expectedAvailabilities);

            // When
            List<Availability> actualAvailabilities = availabilityService.findAvailabilitiesByMonth(ROOM_ID, yearMonth);

            // Then
            assertThat(actualAvailabilities).isEqualTo(expectedAvailabilities);
            verify(availabilityRepository).findAvailabilitiesByDate(ROOM_ID, startOfMonth, startOfNextMonth);
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailable {
        @Test
        @DisplayName("성공: 요청 기간이 단일 available 기간 내에 완전히 포함될 때 true 반환")
        void givenRangeFullyContainedInOneAvailability_whenCheck_thenReturnsTrue() {
            // Given
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(10);
            List<Availability> availabilities = List.of(
                    new Availability(room, LocalDate.now(), LocalDate.now().plusDays(20), true)
            );
            when(availabilityRepository.findTrueAvailabilitiesByDate(ROOM_ID, checkIn, checkOut)).thenReturn(availabilities);

            // When
            boolean result = availabilityService.isAvailable(ROOM_ID, checkIn, checkOut);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("성공: 요청 기간이 여러 available 기간에 걸쳐 이어질 때 true 반환")
        void givenRangeSpanningMultipleAvailabilities_whenCheck_thenReturnsTrue() {
            // Given
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(15);
            List<Availability> availabilities = List.of(
                    new Availability(room, LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), true),
                    new Availability(room, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), true)
            );
            when(availabilityRepository.findTrueAvailabilitiesByDate(ROOM_ID, checkIn, checkOut)).thenReturn(availabilities);

            // When
            boolean result = availabilityService.isAvailable(ROOM_ID, checkIn, checkOut);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("실패: 요청 기간 중간에 갭이 있을 때 false 반환")
        void givenGapInAvailability_whenCheck_thenReturnsFalse() {
            // Given
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(15);
            List<Availability> availabilities = List.of(
                    new Availability(room, LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), true), // Gap from 8th to 10th
                    new Availability(room, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), true)
            );
            when(availabilityRepository.findTrueAvailabilitiesByDate(ROOM_ID, checkIn, checkOut)).thenReturn(availabilities);

            // When
            boolean result = availabilityService.isAvailable(ROOM_ID, checkIn, checkOut);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("실패: 요청 기간이 available 기간을 벗어날 때 false 반환")
        void givenRangePartiallyAvailable_whenCheck_thenReturnsFalse() {
            // Given
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(15);
            List<Availability> availabilities = List.of(
                    new Availability(room, LocalDate.now().plusDays(5), LocalDate.now().plusDays(12), true) // Ends before checkout
            );
            when(availabilityRepository.findTrueAvailabilitiesByDate(ROOM_ID, checkIn, checkOut)).thenReturn(availabilities);

            // When
            boolean result = availabilityService.isAvailable(ROOM_ID, checkIn, checkOut);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("실패: 해당 기간에 available 데이터가 없을 때 false 반환")
        void givenNoAvailability_whenCheck_thenReturnsFalse() {
            // Given
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(15);
            when(availabilityRepository.findTrueAvailabilitiesByDate(ROOM_ID, checkIn, checkOut)).thenReturn(Collections.emptyList());

            // When
            boolean result = availabilityService.isAvailable(ROOM_ID, checkIn, checkOut);

            // Then
            assertFalse(result);
        }
    }

    // Tests for isAvailableForUpdate would be identical to isAvailable tests,
    // just mocking and verifying findTrueAvailabilitiesByDateForUpdate instead.
    // Here is one example.
    @Nested
    @DisplayName("isAvailableForUpdate Tests")
    class IsAvailableForUpdate {
        @Test
        @DisplayName("성공: 요청 기간이 완전히 available 할 때 true 반환")
        void givenRangeFullyAvailable_whenCheckForUpdate_thenReturnsTrue() {
            // Given
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(15);
            List<Availability> availabilities = List.of(
                    new Availability(room, checkIn, checkOut, true)
            );
            when(availabilityRepository.findTrueAvailabilitiesByDateForUpdate(ROOM_ID, checkIn, checkOut)).thenReturn(availabilities);

            // When
            boolean result = availabilityService.isAvailableForUpdate(ROOM_ID, checkIn, checkOut);

            // Then
            assertTrue(result);
            verify(availabilityRepository).findTrueAvailabilitiesByDateForUpdate(ROOM_ID, checkIn, checkOut);
        }
    }
}