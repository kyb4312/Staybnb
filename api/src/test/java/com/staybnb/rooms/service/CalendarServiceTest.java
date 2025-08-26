package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.*;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.response.CalendarResponse;
import com.staybnb.users.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @InjectMocks
    CalendarService calendarService;

    @Mock
    PricingService pricingService;

    @Mock
    AvailabilityService availabilityService;

    @Mock
    RoomService roomService;

    @Mock
    ExchangeRateService currencyService;


    @Test
    void getCalendar() throws ExecutionException, InterruptedException {
        // given
        long roomId = 1L;

        User user = new User("user@gmail.com", "user", "password");
        user.setId(1L);

        PlaceType placeType = new PlaceType(1, "house");
        Set<Amenity> amenities = Set.of(new Amenity(1, "wifi"), new Amenity(2, "tv"));

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .host(user)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(300_000)
                .currency(Currency.KRW)
                .build();

        YearMonth yearMonth = YearMonth.now();

        when(roomService.findById(roomId)).thenReturn(room);

        // when
        CalendarResponse calendarResponse = calendarService.getCalendar(roomId, "KRW", yearMonth).get();

        //then
        verify(roomService, times(1)).findById(roomId);
        verify(pricingService, times(1)).findPricingsByMonth(roomId, yearMonth);
        verify(availabilityService, times(1)).findAvailabilitiesByMonth(roomId, yearMonth);
        verify(currencyService, times(YearMonth.now().lengthOfMonth())).convert(any(), any(), anyInt());

        assertThat(calendarResponse.getRoomId()).isEqualTo(roomId);
        assertThat(calendarResponse.getCurrency()).isEqualTo(Currency.KRW.toString());
        assertThat(calendarResponse.getDailyInfos()).hasSize(yearMonth.lengthOfMonth());
    }
}