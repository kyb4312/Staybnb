package com.staybnb.bookings.service;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.request.GetBookingPreviewRequest;
import com.staybnb.bookings.exception.BookingPriceChangedException;
import com.staybnb.bookings.exception.ExceededNumberOfGuestException;
import com.staybnb.bookings.exception.NoSuchBookingException;
import com.staybnb.bookings.exception.UnavailableDateException;
import com.staybnb.bookings.repository.BookingRepository;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.service.AvailabilityService;
import com.staybnb.rooms.service.PricingService;
import com.staybnb.rooms.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final RoomService roomService;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;

    public Booking getBookingPreview(@Valid GetBookingPreviewRequest request) {
        Room room = roomService.findById(request.getRoomId());
        Currency guestCurrency = Currency.valueOf(request.getGuestCurrency());

        checkAvailability(room, request.getCheckIn(), request.getCheckOut());
        checkNumberOfGuests(room, request.getNumberOfGuests());

        double bookingPrice = pricingService.getTotalPrice(room, request.getCheckIn(), request.getCheckOut(), guestCurrency);

        return new Booking(room, null, request.getNumberOfGuests(), request.getCheckIn(), request.getCheckOut(), bookingPrice, guestCurrency);
    }

    public Booking createBooking(Booking booking) {
        checkAvailability(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut());
        checkNumberOfGuests(booking.getRoom(), booking.getNumberOfGuests());

        double bookingPrice = pricingService.getTotalPrice(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut(), booking.getCurrency());
        checkIfPriceChanged(booking.getBookingPrice(), bookingPrice);

        availabilityService.updateAvailability(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut().minusDays(1), false);
        booking.setStatus(BookingStatus.REQUESTED);

        return bookingRepository.save(booking);
    }

    private void checkAvailability(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!availabilityService.isAvailable(room.getId(), checkIn, checkOut)) {
            throw new UnavailableDateException(checkIn, checkOut);
        }
    }

    private void checkNumberOfGuests(Room room, int numberOfGuests) {
        if (room.getMaxNumberOfGuests() < numberOfGuests) {
            throw new ExceededNumberOfGuestException(room.getMaxNumberOfGuests(), numberOfGuests);
        }
    }

    private void checkIfPriceChanged(double previewTotalPrice, double totalPrice) {
        if (previewTotalPrice != totalPrice) {
            throw new BookingPriceChangedException(previewTotalPrice, totalPrice);
        }
    }

    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(NoSuchBookingException::new);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBooking(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        return booking;
    }

    public Page<Booking> findUpcomingBookings(Long userId, Pageable pageable) {
        return bookingRepository.findBookingsByStatus(pageable, userId, BookingStatus.REQUESTED, BookingStatus.RESERVED);
    }

    public Page<Booking> findPastBookings(Long userId, Pageable pageable) {
        return bookingRepository.findBookingsByStatus(pageable, userId, BookingStatus.ENDED);
    }

    public Page<Booking> findCanceledBookings(Long userId, Pageable pageable) {
        return bookingRepository.findBookingsByStatus(pageable, userId, BookingStatus.CANCELLED, BookingStatus.REJECTED);
    }
}
