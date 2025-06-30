package com.staybnb.common.exception.custom;

public class SignupException extends RuntimeException {
    public SignupException(String email) {
        super("이미 가입된 이메일입니다. email: " + email);
    }
}
