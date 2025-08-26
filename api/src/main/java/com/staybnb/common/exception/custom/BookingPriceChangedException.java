package com.staybnb.common.exception.custom;

public class BookingPriceChangedException extends RuntimeException {
    public BookingPriceChangedException(double oldPrice, double newPrice) {
        super("요금에 변동이 생겼습니다. oldPrice: " + oldPrice + ", newPrice: " + newPrice);
    }
}
