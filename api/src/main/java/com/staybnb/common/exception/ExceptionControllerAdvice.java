package com.staybnb.common.exception;

import com.staybnb.common.exception.custom.*;
import io.jsonwebtoken.ExpiredJwtException;
import org.hibernate.HibernateException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler({UnauthorizedException.class, ExpiredJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleUnauthorizedException(RuntimeException e) {
        return new ExceptionResponse("A004", e.getMessage()); // 인증 실패
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handlerConnectException(ConnectException e) {
        return new ExceptionResponse("S001", e.getMessage()); // 연결 실패 (ex. 레디스 소켓 연결 실패)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ExceptionResponse handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return new ExceptionResponse("C001", e.getMessage()); // 지원하지 않는 HTTP 메서드
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIllegalArgumentException(IllegalArgumentException e) {
        return new ExceptionResponse("C002", e.getMessage()); // 잘못된 파라미터
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleNoSuchElementException(NoSuchElementException e) {
        return new ExceptionResponse("C003", e.getMessage()); // 요청한 리소스를 찾을 수 없음
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleDateTimeException(DateTimeException e) {
        return new ExceptionResponse("C004", e.getMessage()); // 날짜/시간 형식 오류
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    } else {
                        return error.getDefaultMessage(); // 전역 오류
                    }
                })
                .orElse("유효성 검사 실패");

        return new ExceptionResponse("C005", errorMessage); // Validation 실패
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ExceptionResponse handleBookingPriceChangedException(BookingPriceChangedException e) {
        return new ExceptionResponse("B001", e.getMessage()); // 예약 중 가격 변경 발생
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleExceededNumberOfGuestsException(ExceededNumberOfGuestException e) {
        return new ExceptionResponse("B002", e.getMessage()); // 허용 인원 초과
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleUnavailableDateException(UnavailableDateException e) {
        return new ExceptionResponse("B003", e.getMessage()); // 선택 불가능한 날짜
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleSignupException(SignupException e) {
        return new ExceptionResponse("J001", e.getMessage()); // 회원가입 예외 (이메일 중복)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleSQLException(SQLException e) {
        return new ExceptionResponse("Q001", e.getMessage()); // SQL 문법 예외
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleHibernateException(HibernateException e) {
        return new ExceptionResponse("Q002", e.getMessage()); // JPA 예외 (ex. insert 시도 중 id 충돌 시)
    }
}
