package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.*;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.repository.PricingRepository;
import com.staybnb.users.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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
    ArgumentCaptor<List<Pricing>> pricingCaptor;

    @Test
    void getTotalPrice() {
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

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);

        SearchPricingRequest request = new SearchPricingRequest(startDate, endDate, "KRW");

        when(roomService.findById(roomId)).thenReturn(room);
        when(exchangeRateService.convert(Currency.KRW, Currency.KRW, 600_000)).thenReturn((double) 600_000);

        // when
        PricingResponse response = pricingService.getTotalPricing(roomId, request);

        // then
        verify(roomService, times(1)).findById(roomId);
        verify(pricingRepository, times(1)).findPricingsByDate(roomId, startDate, endDate);
        verify(exchangeRateService, times(1)).convert(Currency.KRW, Currency.KRW, 600_000);

        PricingResponse expected = new PricingResponse(roomId, startDate, endDate, (double) 600_000, "KRW");
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void updateSelectedDatesPricing() {
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

        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(5);

        UpdatePricingRequest request = new UpdatePricingRequest(List.of(new DateRangeRequest(startDate, endDate)), 400_000);

        when(roomService.findById(roomId)).thenReturn(room);
        when(pricingRepository.findOrderedPricingsByDate(roomId, startDate, endDate.plusDays(1)))
                .thenReturn(List.of(new Pricing(room, LocalDate.now().plusDays(1), LocalDate.now().plusDays(8), 500_000)));

        // when
        pricingService.updateSelectedDatesPricing(1L, roomId, request);

        //then
        verify(roomService, times(1)).findById(roomId);
        verify(pricingRepository, times(1)).findOrderedPricingsByDate(roomId, startDate, endDate.plusDays(1));
        verify(pricingRepository, times(1)).deleteAll(anyList());

        verify(pricingRepository, times(1)).saveAll(pricingCaptor.capture());
        List<Pricing> allValues = pricingCaptor.getValue();

        Pricing expected0 = new Pricing(room, LocalDate.now().plusDays(3), LocalDate.now().plusDays(6), 400_000);
        Pricing expected1 = new Pricing(room, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 500_000);
        Pricing expected2 = new Pricing(room, LocalDate.now().plusDays(6), LocalDate.now().plusDays(8), 500_000);

        assertThat(allValues.get(0)).usingRecursiveComparison().isEqualTo(expected0);
        assertThat(allValues.get(1)).usingRecursiveComparison().isEqualTo(expected1);
        assertThat(allValues.get(2)).usingRecursiveComparison().isEqualTo(expected2);
    }
}
