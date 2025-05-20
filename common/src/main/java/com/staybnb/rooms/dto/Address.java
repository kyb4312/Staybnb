package com.staybnb.rooms.dto;

import lombok.*;

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
