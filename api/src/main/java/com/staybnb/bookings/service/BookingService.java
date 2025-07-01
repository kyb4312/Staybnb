package com.staybnb.bookings.service;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.request.GetBookingPreviewRequest;
import com.staybnb.bookings.exception.*;
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
import java.util.List;

import static com.staybnb.bookings.domain.vo.BookingStatus.*;

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

    @Transactional
    public Booking createBooking(Booking booking) {
        checkAvailabilityForUpdate(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut());
        checkNumberOfGuests(booking.getRoom(), booking.getNumberOfGuests());

        double bookingPrice = pricingService.getTotalPrice(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut(), booking.getCurrency());
        checkIfPriceChanged(booking.getBookingPrice(), bookingPrice);

        availabilityService.updateAvailabilityToFalse(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut());
        booking.setStatus(REQUESTED);

        return bookingRepository.save(booking);
    }

    private void checkAvailability(Room room, LocalDate checkInInclusive, LocalDate checkOutExclusive) {
        if (!availabilityService.isAvailable(room.getId(), checkInInclusive, checkOutExclusive)) {
            throw new UnavailableDateException(checkInInclusive, checkOutExclusive);
        }
    }

    private void checkAvailabilityForUpdate(Room room, LocalDate checkInInclusive, LocalDate checkOutExclusive) {
        if (!availabilityService.isAvailableForUpdate(room.getId(), checkInInclusive, checkOutExclusive)) {
            throw new UnavailableDateException(checkInInclusive, checkOutExclusive);
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
        if (!(booking.getStatus() == REQUESTED || booking.getStatus() == RESERVED)) {
            throw new InvalidStatusChangeException(booking.getStatus().toString());
        }
        return updateBookingStatus(booking, CANCELLED);
    }

    @Transactional
    public Booking updateBooking(Long bookingId, BookingStatus status) {
        Booking booking = getBooking(bookingId);
        if (!(status == RESERVED || status == REJECTED)) {
            throw new InvalidStatusChangeException();
        }
        if (booking.getStatus() != REQUESTED) {
            throw new InvalidStatusChangeException(booking.getStatus().toString());
        }
        return updateBookingStatus(booking, status);
    }

    private Booking updateBookingStatus(Booking booking, BookingStatus bookingStatus) {
        booking.setStatus(bookingStatus);
        return booking;
    }

    public Page<Booking> findUpcomingBookings(Long userId, Pageable pageable) {
        return bookingRepository.findBookingsByGuestIdAndStatus(pageable, userId, List.of(REQUESTED.toString(), RESERVED.toString()));
    }

    public Page<Booking> findPastBookings(Long userId, Pageable pageable) {
        return bookingRepository.findBookingsByGuestIdAndStatus(pageable, userId, List.of(ENDED.toString()));
    }

    public Page<Booking> findCancelledBookings(Long userId, Pageable pageable) {
        return bookingRepository.findBookingsByGuestIdAndStatus(pageable, userId, List.of(CANCELLED.toString(), REJECTED.toString()));
    }

    public Page<Booking> findBookingsByRoomId(Long roomId, Pageable pageable) {
        Room room = roomService.findById(roomId);
        return bookingRepository.findByRoom(room, pageable);
    }
}
