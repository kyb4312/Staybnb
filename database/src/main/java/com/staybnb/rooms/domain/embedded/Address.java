package com.staybnb.rooms.domain.embedded;

import jakarta.persistence.Column;
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
    @Column(nullable = false, length = 100)
    String country;

    @Column(length = 100)
    String province;

    @NotBlank
    @Column(nullable = false, length = 200)
    String city;

    @NotBlank
    @Column(nullable = false, length = 200)
    String street;

    @Column(length = 100)
    String flat;
}
