package com.staybnb.common.exception.custom;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(long userId) {
        super("권한이 없는 유저입니다. userId: " + userId);
    }
}
