package com.staybnb.handler;

import com.staybnb.bookings.exception.BookingPriceChangedException;
import com.staybnb.bookings.exception.ExceededNumberOfGuestException;
import com.staybnb.bookings.exception.UnavailableDateException;
import com.staybnb.rooms.dto.response.ExceptionResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;
import java.util.NoSuchElementException;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("유효성 검사 실패");

        return new ExceptionResponse("ERROR", errorMessage);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIllegalArgumentException(IllegalArgumentException e) {
        return new ExceptionResponse("ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleNoSuchElementException(NoSuchElementException e) {
        return new ExceptionResponse("ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ExceptionResponse handleBookingPriceChangedException(BookingPriceChangedException e) {
        return new ExceptionResponse("ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleExceededNumberOfGuestsException(ExceededNumberOfGuestException e) {
        return new ExceptionResponse("ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleUnavailableDateException(UnavailableDateException e) {
        return new ExceptionResponse("ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleDateTimeException(DateTimeException e) {
        return new ExceptionResponse("ERROR", e.getMessage());
    }
}
