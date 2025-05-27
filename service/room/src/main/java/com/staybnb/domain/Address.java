package com.staybnb.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Address {
    String country;
    String province;
    String city;
    String street;
    String flat;

    public boolean contains(String str) {
        return country.contains(str)
                || province.contains(str)
                || city.contains(str)
                || street.contains(str);
    }
}
