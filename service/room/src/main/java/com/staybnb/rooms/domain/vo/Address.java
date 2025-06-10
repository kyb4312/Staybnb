package com.staybnb.rooms.domain.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Embeddable
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @NotBlank
    String country;

    String province;

    @NotBlank
    String city;

    @NotBlank
    String street;

    String flat;
}
