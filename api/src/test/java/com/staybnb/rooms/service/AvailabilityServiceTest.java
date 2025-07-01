package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.*;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.dto.request.UpdateAvailabilityRequest;
import com.staybnb.rooms.dto.request.vo.DateRangeRequest;
import com.staybnb.rooms.repository.AvailabilityRepository;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceTest {

    @InjectMocks
    AvailabilityService availabilityService;

    @Mock
    AvailabilityRepository availabilityRepository;

    @Mock
    RoomService roomService;

    @Captor
    ArgumentCaptor<List<Availability>> availabilityCaptor;

    @Test
    void updateSelectedDatesAvailability() {
        // given
        long roomId = 1L;

        User user = new User();
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

        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest(List.of(new DateRangeRequest(startDate, endDate)), true);

        when(roomService.findById(roomId)).thenReturn(room);
        when(availabilityRepository.findOrderedAvailabilitiesByDate(roomId, startDate, endDate.plusDays(1)))
                .thenReturn(List.of(new Availability(room, LocalDate.now().plusDays(1), LocalDate.now().plusDays(8), true)));

        // when
        availabilityService.updateSelectedDatesAvailability(roomId, request);

        //then
        verify(roomService, times(1)).findById(roomId);
        verify(availabilityRepository, times(1)).findOrderedAvailabilitiesByDate(roomId, startDate, endDate.plusDays(1));
        verify(availabilityRepository, times(1)).deleteAll(anyList());

        verify(availabilityRepository, times(1)).saveAll(availabilityCaptor.capture());
        List<Availability> allValues = availabilityCaptor.getValue();

        Availability expected0 = new Availability(room, LocalDate.now().plusDays(3), LocalDate.now().plusDays(6), true);
        Availability expected1 = new Availability(room, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), true);
        Availability expected2 = new Availability(room, LocalDate.now().plusDays(6), LocalDate.now().plusDays(8), true);

        assertThat(allValues.get(0)).usingRecursiveComparison().isEqualTo(expected0);
        assertThat(allValues.get(1)).usingRecursiveComparison().isEqualTo(expected1);
        assertThat(allValues.get(2)).usingRecursiveComparison().isEqualTo(expected2);
    }
}
