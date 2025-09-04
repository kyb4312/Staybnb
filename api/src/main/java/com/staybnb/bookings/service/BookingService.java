package com.staybnb.bookings.service;

import com.staybnb.bookings.domain.Booking;
import com.staybnb.bookings.domain.vo.BookingStatus;
import com.staybnb.bookings.dto.request.GetBookingPreviewRequest;
import com.staybnb.bookings.repository.BookingRepository;
import com.staybnb.common.exception.custom.*;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.domain.vo.Currency;
import com.staybnb.rooms.service.AvailabilityService;
import com.staybnb.rooms.service.PricingService;
import com.staybnb.rooms.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.staybnb.bookings.domain.vo.BookingStatus.*;
import static com.staybnb.common.validation.business.AccessValidator.validateHost;
import static com.staybnb.common.validation.business.AccessValidator.validateHostOrGuest;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final RoomService roomService;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;

    private final Executor asyncExecutor;

    public CompletableFuture<Booking> getBookingPreview(GetBookingPreviewRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Room room = roomService.findById(request.getRoomId());
            Currency guestCurrency = Currency.valueOf(request.getGuestCurrency());

            checkAvailability(room, request.getCheckIn(), request.getCheckOut());
            checkNumberOfGuests(room, request.getNumberOfGuests());

            double bookingPrice = pricingService.getTotalPrice(room, request.getCheckIn(), request.getCheckOut(), guestCurrency);

            return new Booking(room, null, request.getNumberOfGuests(), request.getCheckIn(), request.getCheckOut(), bookingPrice, guestCurrency);
        }, asyncExecutor);
    }

    @Transactional
    public CompletableFuture<Booking> createBooking(Booking booking) {
        return CompletableFuture.supplyAsync(() -> {
            checkAvailabilityForUpdate(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut());
            checkNumberOfGuests(booking.getRoom(), booking.getNumberOfGuests());

            double bookingPrice = pricingService.getTotalPrice(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut(), booking.getCurrency());
            checkIfPriceChanged(booking.getBookingPrice(), bookingPrice);

            availabilityService.updateAvailabilityToFalse(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut());
            booking.setStatus(REQUESTED);

            return bookingRepository.save(booking);
        }, asyncExecutor);
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

    public CompletableFuture<Booking> getBooking(long userId, Long bookingId) {
        return CompletableFuture.supplyAsync(() -> {
            Booking booking = bookingRepository.findById(bookingId).orElseThrow(NoSuchBookingException::new);
            validateHostOrGuest(userId, booking);
            return booking;
        }, asyncExecutor);
    }

    @Transactional
    public CompletableFuture<Booking> cancelBooking(long userId, Long bookingId) {
        return CompletableFuture.supplyAsync(() -> {
            Booking booking = getBooking(userId, bookingId).join();
            if (!(booking.getStatus() == REQUESTED || booking.getStatus() == RESERVED)) {
                throw new InvalidStatusChangeException(booking.getStatus().toString());
            }
            return updateBookingStatus(booking, CANCELLED);
        }, asyncExecutor);
    }

    @Transactional
    public CompletableFuture<Booking> updateBooking(long userId, Long bookingId, BookingStatus status) {
        return CompletableFuture.supplyAsync(() -> {
            Booking booking = getBooking(userId, bookingId).join();
            if (!(status == RESERVED || status == REJECTED)) {
                throw new InvalidStatusChangeException();
            }
            if (booking.getStatus() != REQUESTED) {
                throw new InvalidStatusChangeException(booking.getStatus().toString());
            }
            return updateBookingStatus(booking, status);
        }, asyncExecutor);
    }

    private Booking updateBookingStatus(Booking booking, BookingStatus bookingStatus) {
        booking.setStatus(bookingStatus);
        return booking;
    }

    public CompletableFuture<Page<Booking>> findUpcomingBookings(Long userId, Pageable pageable) {
        return CompletableFuture.supplyAsync(
                () -> bookingRepository.findBookingsByGuestIdAndStatus(
                        pageable,
                        userId,
                        List.of(REQUESTED.toString(), RESERVED.toString())
                ),
                asyncExecutor
        );
    }

    public CompletableFuture<Page<Booking>> findPastBookings(Long userId, Pageable pageable) {
        return CompletableFuture.supplyAsync(
                () -> bookingRepository.findBookingsByGuestIdAndStatus(
                        pageable,
                        userId,
                        List.of(ENDED.toString())
                ),
                asyncExecutor
        );
    }

    public CompletableFuture<Page<Booking>> findCancelledBookings(Long userId, Pageable pageable) {
        return CompletableFuture.supplyAsync(
                () -> bookingRepository.findBookingsByGuestIdAndStatus(
                        pageable,
                        userId,
                        List.of(CANCELLED.toString(), REJECTED.toString())
                ),
                asyncExecutor
        );
    }

    public CompletableFuture<Page<Booking>> findBookingsByRoomId(long userId, Long roomId, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            Room room = roomService.findById(roomId);
            validateHost(userId, room);
            return bookingRepository.findByRoom(room, pageable);
        }, asyncExecutor);
    }

}
