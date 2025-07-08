package com.staybnb.rooms.service;

import com.staybnb.common.exception.custom.InvalidDateRangeException;
import com.staybnb.common.exception.custom.UnauthorizedException;
import com.staybnb.rooms.domain.*;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.repository.PricingRepository;
import com.staybnb.users.domain.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PricingServiceTest {

    @InjectMocks
    PricingService pricingService;

    @Mock
    PricingRepository pricingRepository;

    @Mock
    RoomService roomService;

    @Mock
    ExchangeRateService exchangeRateService;

    @Captor
    ArgumentCaptor<List<Pricing>> pricingListCaptor;

    private Room room;
    private User host;
    private final long HOST_ID = 1L;
    private final long OTHER_USER_ID = 2L;
    private final long ROOM_ID = 101L;

    @BeforeEach
    void setUp() {
        host = new User("host@gmail.com", "host", "password");
        host.setId(HOST_ID);

        room = Room.builder()
                .id(ROOM_ID)
                .host(host)
                .basePrice(100)
                .currency(Currency.USD)
                .build();
    }

    @Nested
    @DisplayName("getTotalPricing: 숙박 총 가격 조회")
    class GetTotalPricingTests {

        @Test
        @DisplayName("성공: 기본가와 특별가가 혼합된 기간의 총 가격을 정확히 계산한다")
        void getTotalPricing_Success_WithMixedPricing() {
            // Given
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(11); // 10 nights
            SearchPricingRequest request = new SearchPricingRequest(startDate, endDate, "KRW");

            // Special pricing for 4 nights (Aug 5 to Aug 9)
            Pricing specialPricing = new Pricing(room, LocalDate.now().plusDays(5), LocalDate.now().plusDays(9), 150);
            List<Pricing> pricings = List.of(specialPricing);

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(pricingRepository.findPricingsByDate(ROOM_ID, startDate, endDate)).thenReturn(pricings);
            // 6 nights at base price (100) + 4 nights at special price (150) = 600 + 600 = 1200 USD
            // Assume 1 USD = 1350 KRW
            when(exchangeRateService.convert(Currency.USD, Currency.KRW, 1200)).thenReturn(1620000.0);

            // When
            PricingResponse response = pricingService.getTotalPricing(ROOM_ID, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getRoomId()).isEqualTo(ROOM_ID);
            assertThat(response.getTotalPrice()).isEqualTo(1620000.0);
            assertThat(response.getCurrency()).isEqualTo("KRW");
            verify(roomService).findById(ROOM_ID);
            verify(pricingRepository).findPricingsByDate(ROOM_ID, startDate, endDate);
            verify(exchangeRateService).convert(Currency.USD, Currency.KRW, 1200);
        }

        @Test
        @DisplayName("성공: 특별가 없이 기본가로만 총 가격을 계산한다")
        void getTotalPricing_Success_WithOnlyBasePrice() {
            // Given
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(6); // 5 nights
            SearchPricingRequest request = new SearchPricingRequest(startDate, endDate, "USD");

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(pricingRepository.findPricingsByDate(anyLong(), any(), any())).thenReturn(Collections.emptyList());
            // 5 nights * 100 (base price) = 500 USD
            // No conversion needed
            when(exchangeRateService.convert(Currency.USD, Currency.USD, 500)).thenReturn(500.0);

            // When
            PricingResponse response = pricingService.getTotalPricing(ROOM_ID, request);

            // Then
            assertThat(response.getTotalPrice()).isEqualTo(500.0);
        }

        @Test
        @DisplayName("실패: 시작일이 과거일 경우 InvalidDateRangeException을 던진다")
        void getTotalPricing_Fail_WhenStartDateIsInThePast() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(5);
            SearchPricingRequest request = new SearchPricingRequest(startDate, endDate, "USD");

            // When & Then
            assertThatThrownBy(() -> pricingService.getTotalPricing(ROOM_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("startDate가 과거 일자입니다.");
        }

        @Test
        @DisplayName("실패: 시작일이 종료일보다 늦을 경우 InvalidDateRangeException을 던진다")
        void getTotalPricing_Fail_WhenStartDateIsAfterEndDate() {
            // Given
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(1);
            SearchPricingRequest request = new SearchPricingRequest(startDate, endDate, "USD");

            // When & Then
            assertThatThrownBy(() -> pricingService.getTotalPricing(ROOM_ID, request))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("startDate는 endDate 보다 이전 일자여야 합니다.");
        }
    }

    @Nested
    @DisplayName("updateSelectedDatesPricing: 특정 기간 가격 업데이트")
    class UpdateSelectedDatesPricingTests {

        @Test
        @DisplayName("성공: 기존 가격 정보가 없을 때 새 가격을 추가한다")
        void updatePricing_Success_WhenNoConflicts() {
            // Given
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(5);
            int newPrice = 200;
            UpdatePricingRequest request = new UpdatePricingRequest(
                    List.of(new DateRangeRequest(startDate, endDate)), newPrice
            );

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(pricingRepository.findOrderedPricingsByDate(ROOM_ID, startDate, endDate.plusDays(1))).thenReturn(Collections.emptyList());

            // When
            pricingService.updateSelectedDatesPricing(HOST_ID, ROOM_ID, request);

            // Then
            verify(pricingRepository).saveAll(pricingListCaptor.capture());
            List<Pricing> savedPricings = pricingListCaptor.getValue();

            assertThat(savedPricings).hasSize(1);
            assertThat(savedPricings.getFirst().getStartDate()).isEqualTo(startDate);
            assertThat(savedPricings.getFirst().getEndDate()).isEqualTo(endDate.plusDays(1));
            assertThat(savedPricings.getFirst().getPricePerNight()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("성공: 새 가격 기간이 기존 가격 기간을 완전히 덮어쓴다")
        void updatePricing_Success_WhenNewRangeOverwritesExisting() {
            // Given
            LocalDate newStartDate = LocalDate.now().plusDays(1);
            LocalDate newEndDate = LocalDate.now().plusDays(10);
            int newPrice = 250;
            UpdatePricingRequest request = new UpdatePricingRequest(
                    List.of(new DateRangeRequest(newStartDate, newEndDate)), newPrice
            );

            Pricing existingPricing = new Pricing(room, LocalDate.now().plusDays(3), LocalDate.now().plusDays(7), 150);

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(pricingRepository.findOrderedPricingsByDate(ROOM_ID, newStartDate, newEndDate.plusDays(1))).thenReturn(List.of(existingPricing));

            // When
            pricingService.updateSelectedDatesPricing(HOST_ID, ROOM_ID, request);

            // Then
            verify(pricingRepository).deleteAll(List.of(existingPricing));
            verify(pricingRepository).flush();
            verify(pricingRepository).saveAll(pricingListCaptor.capture());

            List<Pricing> savedPricings = pricingListCaptor.getValue();
            assertThat(savedPricings).hasSize(1);
            assertThat(savedPricings.getFirst().getStartDate()).isEqualTo(newStartDate);
            assertThat(savedPricings.getFirst().getEndDate()).isEqualTo(newEndDate.plusDays(1));
            assertThat(savedPricings.getFirst().getPricePerNight()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("성공: 새 가격 기간이 기존 가격 기간 중간에 삽입되어 기존 기간을 둘로 나눈다")
        void updatePricing_Success_WhenNewRangeSplitsExisting() {
            // Given
            LocalDate newStartDate = LocalDate.now().plusDays(5);
            LocalDate newEndDate = LocalDate.now().plusDays(10);
            int newPrice = 300;
            UpdatePricingRequest request = new UpdatePricingRequest(
                    List.of(new DateRangeRequest(newStartDate, newEndDate)), newPrice
            );

            int oldPrice = 150;
            Pricing existingPricing = new Pricing(room, LocalDate.now().plusDays(1), LocalDate.now().plusDays(15), oldPrice);

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(pricingRepository.findOrderedPricingsByDate(ROOM_ID, newStartDate, newEndDate.plusDays(1))).thenReturn(List.of(existingPricing));

            // When
            pricingService.updateSelectedDatesPricing(HOST_ID, ROOM_ID, request);

            // Then
            verify(pricingRepository).saveAll(pricingListCaptor.capture());
            List<Pricing> savedPricings = pricingListCaptor.getValue();
            savedPricings.sort(Comparator.comparing(Pricing::getStartDate));

            assertThat(savedPricings).hasSize(3);

            // 1. New pricing
            assertThat(savedPricings.get(1).getStartDate()).isEqualTo(newStartDate);
            assertThat(savedPricings.get(1).getEndDate()).isEqualTo(newEndDate.plusDays(1));
            assertThat(savedPricings.get(1).getPricePerNight()).isEqualTo(newPrice);

            // 2. First part of old pricing
            assertThat(savedPricings.get(0).getStartDate()).isEqualTo(existingPricing.getStartDate());
            assertThat(savedPricings.get(0).getEndDate()).isEqualTo(newStartDate);
            assertThat(savedPricings.get(0).getPricePerNight()).isEqualTo(oldPrice);

            // 3. Second part of old pricing
            assertThat(savedPricings.get(2).getStartDate()).isEqualTo(newEndDate.plusDays(1));
            assertThat(savedPricings.get(2).getEndDate()).isEqualTo(existingPricing.getEndDate());
            assertThat(savedPricings.get(2).getPricePerNight()).isEqualTo(oldPrice);
        }

        @Test
        @DisplayName("성공: 새로운 가격 기간이 기존 가격 기간을 여러 구간으로 나눌 때")
        void givenPricingRange_whenSplitsExistingPricingRange_thenSavesMultiRanges() {
            // Given
            int oldPrice = 150;
            LocalDate originalStart1 = LocalDate.now().plusDays(1);
            LocalDate originalEnd1 = LocalDate.now().plusDays(7);
            LocalDate originalStart2 = LocalDate.now().plusDays(8);
            LocalDate originalEnd2 = LocalDate.now().plusDays(9);
            LocalDate originalStart3 = LocalDate.now().plusDays(12);
            LocalDate originalEnd3 = LocalDate.now().plusDays(13);
            LocalDate originalStart4 = LocalDate.now().plusDays(14);
            LocalDate originalEnd4 = LocalDate.now().plusDays(22);
            Pricing existingPricing1 = new Pricing(room, originalStart1, originalEnd1, oldPrice);
            Pricing existingPricing2 = new Pricing(room, originalStart2, originalEnd2, oldPrice);
            Pricing existingPricing3 = new Pricing(room, originalStart3, originalEnd3, oldPrice);
            Pricing existingPricing4 = new Pricing(room, originalStart4, originalEnd4, oldPrice);

            int newPrice = 300;
            LocalDate newStart1 = LocalDate.now().plusDays(3);
            LocalDate newEnd1 = LocalDate.now().plusDays(4);
            LocalDate newStart2 = LocalDate.now().plusDays(10);
            LocalDate newEnd2 = LocalDate.now().plusDays(15);
            LocalDate newStart3 = LocalDate.now().plusDays(18);
            LocalDate newEnd3 = LocalDate.now().plusDays(19);
            LocalDate newStart4 = LocalDate.now().plusDays(23);
            LocalDate newEnd4 = LocalDate.now().plusDays(24);
            var request = new UpdatePricingRequest(
                    List.of(
                            new DateRangeRequest(newStart1, newEnd1),
                            new DateRangeRequest(newStart2, newEnd2),
                            new DateRangeRequest(newStart3, newEnd3),
                            new DateRangeRequest(newStart4, newEnd4)
                    ), newPrice
            );

            when(roomService.findById(ROOM_ID)).thenReturn(room);
            when(pricingRepository.findOrderedPricingsByDate(ROOM_ID, newStart1, newEnd4.plusDays(1)))
                    .thenReturn(List.of(
                            existingPricing1,
                            existingPricing2,
                            existingPricing3,
                            existingPricing4
                    ));

            // When
            pricingService.updateSelectedDatesPricing(HOST_ID, ROOM_ID, request);

            // Then
            verify(pricingRepository).deleteAll(List.of(
                    existingPricing1,
                    existingPricing2,
                    existingPricing3,
                    existingPricing4)
            );
            verify(pricingRepository).saveAll(pricingListCaptor.capture());
            List<Pricing> savedPricings = pricingListCaptor.getValue();
            savedPricings.sort(Comparator.comparing(Pricing::getStartDate));

            assertThat(savedPricings).hasSize(9);
            // 1. Original part 1
            assertThat(savedPricings.get(0).getStartDate()).isEqualTo(originalStart1);
            assertThat(savedPricings.get(0).getEndDate()).isEqualTo(newStart1);
            assertThat(savedPricings.get(0).getPricePerNight()).isEqualTo(oldPrice);
            // 2. New part 1
            assertThat(savedPricings.get(1).getStartDate()).isEqualTo(newStart1);
            assertThat(savedPricings.get(1).getEndDate()).isEqualTo(newEnd1.plusDays(1));
            assertThat(savedPricings.get(1).getPricePerNight()).isEqualTo(newPrice);
            // 3. Original part 2
            assertThat(savedPricings.get(2).getStartDate()).isEqualTo(newEnd1.plusDays(1));
            assertThat(savedPricings.get(2).getEndDate()).isEqualTo(originalEnd1);
            assertThat(savedPricings.get(2).getPricePerNight()).isEqualTo(oldPrice);
            // 3. Original part 3
            assertThat(savedPricings.get(3).getStartDate()).isEqualTo(originalStart2);
            assertThat(savedPricings.get(3).getEndDate()).isEqualTo(originalEnd2);
            assertThat(savedPricings.get(3).getPricePerNight()).isEqualTo(oldPrice);
            // 4. New part 2
            assertThat(savedPricings.get(4).getStartDate()).isEqualTo(newStart2);
            assertThat(savedPricings.get(4).getEndDate()).isEqualTo(newEnd2.plusDays(1));
            assertThat(savedPricings.get(4).getPricePerNight()).isEqualTo(newPrice);
            // 5. Original part 4
            assertThat(savedPricings.get(5).getStartDate()).isEqualTo(newEnd2.plusDays(1));
            assertThat(savedPricings.get(5).getEndDate()).isEqualTo(newStart3);
            assertThat(savedPricings.get(5).getPricePerNight()).isEqualTo(oldPrice);
            // 6. New part 3
            assertThat(savedPricings.get(6).getStartDate()).isEqualTo(newStart3);
            assertThat(savedPricings.get(6).getEndDate()).isEqualTo(newEnd3.plusDays(1));
            assertThat(savedPricings.get(6).getPricePerNight()).isEqualTo(newPrice);
            // 7. Original part 5
            assertThat(savedPricings.get(7).getStartDate()).isEqualTo(newEnd3.plusDays(1));
            assertThat(savedPricings.get(7).getEndDate()).isEqualTo(originalEnd4);
            assertThat(savedPricings.get(7).getPricePerNight()).isEqualTo(oldPrice);
            // 8. New part 4
            assertThat(savedPricings.get(8).getStartDate()).isEqualTo(newStart4);
            assertThat(savedPricings.get(8).getEndDate()).isEqualTo(newEnd4.plusDays(1));
            assertThat(savedPricings.get(8).getPricePerNight()).isEqualTo(newPrice);
        }

        @Test
        @DisplayName("실패: 호스트가 아닌 사용자가 가격 업데이트 시 UnauthorizedException을 던진다")
        void updatePricing_Fail_WhenUserIsUnauthorized() {
            // Given
            UpdatePricingRequest request = new UpdatePricingRequest(
                    List.of(new DateRangeRequest(LocalDate.now(), LocalDate.now().plusDays(1))), 200
            );
            when(roomService.findById(ROOM_ID)).thenReturn(room);

            // When & Then
            assertThatThrownBy(() -> pricingService.updateSelectedDatesPricing(OTHER_USER_ID, ROOM_ID, request))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("findPricingsByMonth: 월별 가격 정보 조회")
    class FindPricingsByMonthTests {

        @Test
        @DisplayName("성공: 특정 월의 가격 정보를 정확히 조회한다")
        void findPricingsByMonth_Success() {
            // Given
            YearMonth yearMonth = YearMonth.now().plusMonths(1);
            LocalDate monthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            LocalDate nextMonthStart = LocalDate.now().plusMonths(2).withDayOfMonth(1);

            Pricing pricing1 = new Pricing(room, LocalDate.now().plusMonths(1).withDayOfMonth(5), LocalDate.now().plusMonths(1).withDayOfMonth(10), 120);
            Pricing pricing2 = new Pricing(room, LocalDate.now().plusMonths(1).withDayOfMonth(20), LocalDate.now().plusMonths(1).withDayOfMonth(25), 130);
            List<Pricing> expectedPricings = List.of(pricing1, pricing2);

            when(pricingRepository.findPricingsByDate(ROOM_ID, monthStart, nextMonthStart)).thenReturn(expectedPricings);

            // When
            List<Pricing> actualPricings = pricingService.findPricingsByMonth(ROOM_ID, yearMonth);

            // Then
            assertThat(actualPricings).hasSize(2);
            assertThat(actualPricings).containsExactlyInAnyOrder(pricing1, pricing2);
            verify(pricingRepository).findPricingsByDate(ROOM_ID, monthStart, nextMonthStart);
        }
    }
}
