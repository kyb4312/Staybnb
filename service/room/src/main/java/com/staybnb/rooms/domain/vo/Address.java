package com.staybnb.rooms.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
