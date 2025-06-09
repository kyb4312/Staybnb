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
}
