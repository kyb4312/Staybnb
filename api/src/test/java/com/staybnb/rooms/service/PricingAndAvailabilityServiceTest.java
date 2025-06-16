package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.*;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.request.vo.DateRange;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.repository.AvailabilityRepository;
import com.staybnb.rooms.repository.PricingRepository;
import com.staybnb.rooms.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingAndAvailabilityServiceTest {

    @InjectMocks
    PricingAndAvailabilityService pricingAndAvailabilityService;

    @Mock
    PricingRepository pricingRepository;

    @Mock
    AvailabilityRepository availabilityRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    CurrencyService currencyService;

    @Captor
    ArgumentCaptor<Pricing> pricingCaptor;

    @Captor
    ArgumentCaptor<Availability> availabilityCaptor;

    @Test
    void getPricing() {
        // given
        long roomId = 1L;

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
                .build();

        LocalDate startDate = LocalDate.parse("2025-09-13");
        LocalDate endDate = LocalDate.parse("2025-09-15");
        String currencyCode = "KRW";

        SearchPricingRequest request = new SearchPricingRequest(startDate, endDate, currencyCode);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(currencyService.getByCode(currencyCode)).thenReturn(currency);
        when(currencyService.convert(currency, currency, 600_000)).thenReturn((double) 600_000);

        // when
        PricingResponse response = pricingAndAvailabilityService.getPricing(roomId, request);

        // then
        verify(roomRepository, times(1)).findById(roomId);
        verify(pricingRepository, times(1)).findPricingsByDate(roomId, startDate, endDate.minusDays(1));
        verify(currencyService, times(1)).getByCode(currencyCode);
        verify(currencyService, times(1)).convert(currency, currency, 600_000);

        PricingResponse expected = new PricingResponse(roomId, startDate, endDate, (double) 600_000, currencyCode);
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void updateSelectedDatesPricing() {
        // given
        long roomId = 1L;

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
                .build();

        LocalDate startDate = LocalDate.parse("2025-09-13");
        LocalDate endDate = LocalDate.parse("2025-09-15");

        UpdatePricingRequest request = new UpdatePricingRequest(List.of(new DateRange(startDate, endDate)), 400_000);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(pricingRepository.findPricingsByDate(roomId, startDate, endDate))
                .thenReturn(List.of(new Pricing(room, LocalDate.parse("2025-09-11"), LocalDate.parse("2025-09-16"), 500_000)));

        // when
        pricingAndAvailabilityService.updateSelectedDatesPricing(roomId, request);

        //then
        verify(roomRepository, times(1)).findById(roomId);
        verify(pricingRepository, times(1)).findPricingsByDate(roomId, startDate, endDate);
        verify(pricingRepository, times(1)).deleteAll(anyList());

        verify(pricingRepository, times(3)).save(pricingCaptor.capture());
        List<Pricing> allValues = pricingCaptor.getAllValues();

        Pricing expected0 = new Pricing(room, LocalDate.parse("2025-09-11"), LocalDate.parse("2025-09-12"), 500_000);
        Pricing expected1 = new Pricing(room, LocalDate.parse("2025-09-16"), LocalDate.parse("2025-09-16"), 500_000);
        Pricing expected2 = new Pricing(room, startDate, endDate, 400_000);

        assertThat(allValues.get(0)).usingRecursiveComparison().isEqualTo(expected0);
        assertThat(allValues.get(1)).usingRecursiveComparison().isEqualTo(expected1);
        assertThat(allValues.get(2)).usingRecursiveComparison().isEqualTo(expected2);
    }

    @Test
    void updateSelectedDatesAvailability() {
        // given
        long roomId = 1L;

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
                .build();

        LocalDate startDate = LocalDate.parse("2025-09-13");
        LocalDate endDate = LocalDate.parse("2025-09-15");

        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest(List.of(new DateRange(startDate, endDate)), true);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(availabilityRepository.findAvailabilitiesByDate(roomId, startDate, endDate))
                .thenReturn(List.of(new Availability(room, LocalDate.parse("2025-09-11"), LocalDate.parse("2025-09-16"), true)));

        // when
        pricingAndAvailabilityService.updateSelectedDatesAvailability(roomId, request);

        //then
        verify(roomRepository, times(1)).findById(roomId);
        verify(availabilityRepository, times(1)).findAvailabilitiesByDate(roomId, startDate, endDate);
        verify(availabilityRepository, times(1)).deleteAll(anyList());

        verify(availabilityRepository, times(3)).save(availabilityCaptor.capture());
        List<Availability> allValues = availabilityCaptor.getAllValues();

        Availability expected0 = new Availability(room, LocalDate.parse("2025-09-11"), LocalDate.parse("2025-09-12"), true);
        Availability expected1 = new Availability(room, LocalDate.parse("2025-09-16"), LocalDate.parse("2025-09-16"), true);
        Availability expected2 = new Availability(room, startDate, endDate, true);

        assertThat(allValues.get(0)).usingRecursiveComparison().isEqualTo(expected0);
        assertThat(allValues.get(1)).usingRecursiveComparison().isEqualTo(expected1);
        assertThat(allValues.get(2)).usingRecursiveComparison().isEqualTo(expected2);
    }

    @Test
    void getCalendar() {
        // given
        long roomId = 1L;

        User user = new User();
        user.setId(1L);

        PlaceType placeType = new PlaceType();
        placeType.setId(1);

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Amenity amenity = new Amenity();
        amenity.setId(1);
        Amenity amenity2 = new Amenity();
        amenity.setId(2);
        Set<Amenity> amenities = Set.of(amenity, amenity2);

        Currency currency = new Currency();
        currency.setCode("KRW");

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
                .currency(currency)
                .build();

        YearMonth yearMonth = YearMonth.parse("2025-09");

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // when
        pricingAndAvailabilityService.getCalendar(roomId, "KRW", yearMonth);

        //then
        verify(roomRepository, times(1)).findById(roomId);
        verify(pricingRepository, times(1)).findPricingsByMonth(roomId, yearMonth);
        verify(availabilityRepository, times(1)).findAvailabilitiesByMonth(roomId, yearMonth);
        verify(currencyService, times(30)).convert(any(), any(), anyInt());
    }
}