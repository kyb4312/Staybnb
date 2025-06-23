package com.staybnb.bookings.service;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.request.GetBookingPreviewRequest;
import com.staybnb.bookings.exception.InvalidStatusChangeException;
import com.staybnb.bookings.repository.BookingRepository;
import com.staybnb.rooms.domain.Amenity;
import com.staybnb.rooms.domain.PlaceType;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.User;
import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.domain.vo.RoomType;
import com.staybnb.rooms.service.AvailabilityService;
import com.staybnb.rooms.service.PricingService;
import com.staybnb.rooms.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    BookingService bookingService;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    RoomService roomService;

    @Mock
    AvailabilityService availabilityService;

    @Mock
    PricingService pricingService;

    @Test
    void getBookingPreview() {
        // given
        User host = new User();
        PlaceType placeType = new PlaceType(1, "HOUSE");
        Set<Amenity> amenities = Set.of();

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(1L)
                .host(host)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .build();

        long roomId = 1L;
        int numberOfGuests = 2;
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        String guestCurrency = "KRW";

        GetBookingPreviewRequest request = new GetBookingPreviewRequest(roomId, numberOfGuests, checkIn, checkOut, guestCurrency);


        when(roomService.findById(roomId)).thenReturn(room);
        when(availabilityService.isAvailable(roomId, checkIn, checkOut)).thenReturn(true);
        when(pricingService.getTotalPrice(room, checkIn, checkOut, Currency.KRW)).thenReturn(1_400_000.0);

        // when
        Booking bookingPreview = bookingService.getBookingPreview(request);

        // then
        verify(roomService, times(1)).findById(roomId);
        verify(availabilityService, times(1)).isAvailable(roomId, checkIn, checkOut);
        verify(pricingService, times(1)).getTotalPrice(room, checkIn, checkOut, Currency.KRW);

        Booking expected = new Booking(room, null, numberOfGuests, checkIn, checkOut, 1_400_000.0, Currency.KRW);
        assertThat(bookingPreview)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void createBooking() {
        // given
        User host = new User();
        PlaceType placeType = new PlaceType(1, "HOUSE");
        Set<Amenity> amenities = Set.of();

        Address address = Address.builder()
                .country("United States")
                .province("Kentucky")
                .city("Louisville")
                .street("610 W Magnolia Ave")
                .build();

        Room room = Room.builder()
                .id(1L)
                .host(host)
                .placeType(placeType)
                .roomType(RoomType.ENTIRE_PLACE)
                .address(address)
                .maxNumberOfGuests(2)
                .bedrooms(1)
                .beds(1)
                .amenities(amenities)
                .title("Modern building in Kentucky")
                .description("Modern building in Kentucky")
                .basePrice(700_000)
                .currency(Currency.KRW)
                .build();

        User guest = new User();
        int numberOfGuests = 2;
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);
        double totalPrice = 1_400_000.0;

        Booking booking = new Booking(room, guest, numberOfGuests, checkIn, checkOut, totalPrice, Currency.KRW);

        when(availabilityService.isAvailable(room.getId(), checkIn, checkOut)).thenReturn(true);
        when(pricingService.getTotalPrice(room, checkIn, checkOut, Currency.KRW)).thenReturn(totalPrice);
        when(bookingRepository.save(booking)).thenReturn(booking);

        // when
        bookingService.createBooking(booking);

        // then
        verify(availabilityService, times(1)).isAvailable(room.getId(), checkIn, checkOut);
        verify(pricingService, times(1)).getTotalPrice(room, checkIn, checkOut, Currency.KRW);
        verify(availabilityService, times(1)).updateAvailability(room, checkIn, checkOut.minusDays(1), false);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void getBooking() {
        // given
        long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(new Booking()));

        // when
        bookingService.getBooking(bookingId);

        // then
        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void cancelBooking() {
        // given
        long bookingId = 1L;
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.REQUESTED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // when
        Booking cancelledBooking = bookingService.cancelBooking(bookingId);

        // then
        verify(bookingRepository, times(1)).findById(bookingId);
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBookingFailed() {
        // given
        long bookingId = 1L;
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // when // then
        assertThrows(InvalidStatusChangeException.class, () -> bookingService.cancelBooking(bookingId));
        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void updateBooking() {
        // given
        long bookingId = 1L;
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.REQUESTED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // when
        Booking updatedBooking = bookingService.updateBooking(bookingId, BookingStatus.REJECTED);

        // then
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    void updateBookingFailed() {
        // given
        long bookingId = 1L;
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.RESERVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // when // then
        assertThrows(InvalidStatusChangeException.class, () -> bookingService.updateBooking(bookingId, BookingStatus.REJECTED));
        verify(bookingRepository, times(1)).findById(bookingId);
    }

}