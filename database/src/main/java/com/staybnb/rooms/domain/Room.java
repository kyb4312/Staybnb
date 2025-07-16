package com.staybnb.rooms.domain;

import com.staybnb.rooms.domain.embedded.Address;
import com.staybnb.rooms.domain.vo.*;
import com.staybnb.users.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_type_id", nullable = false)
    private PlaceType placeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private Integer maxNumberOfGuests;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Integer beds;

    @ManyToMany
    @JoinTable(
            name = "room_amenity",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer basePrice;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private Double basePriceInUsd;

    private boolean isDeleted;

    private LocalDateTime deletedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String timeZoneId;
}
