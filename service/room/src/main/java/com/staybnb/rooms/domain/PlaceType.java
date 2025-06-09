package com.staybnb.rooms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlaceType {

    @Id
    private Integer id;

    private String name;
}
