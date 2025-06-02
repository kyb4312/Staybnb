package com.staybnb.rooms.domain;

import com.staybnb.rooms.domain.vo.*;
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

    @Setter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Integer pricePerNight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    private boolean isDeleted;

    private LocalDateTime deletedAt;

    public void update(RoomUpdateInfo updateInfo) {
        if(updateInfo.getMaxNumberOfGuests() != null) {
            this.maxNumberOfGuests = updateInfo.getMaxNumberOfGuests();
        }
        if(updateInfo.getBedrooms() != null) {
            this.bedrooms = updateInfo.getBedrooms();
        }
        if(updateInfo.getBeds() != null) {
            this.beds = updateInfo.getBeds();
        }
        if (updateInfo.getAmenities() != null && !updateInfo.getAmenities().isEmpty()) {
            this.amenities = updateInfo.getAmenities();
        }
        if (updateInfo.getTitle() != null) {
            this.title = updateInfo.getTitle();
        }
        if (updateInfo.getDescription() != null) {
            this.description = updateInfo.getDescription();
        }
        if (updateInfo.getPricePerNight() != null) {
            this.pricePerNight = updateInfo.getPricePerNight();
        }
        if (updateInfo.getCurrency() != null) {
            this.currency = updateInfo.getCurrency();
        }
    }

    public void delete(LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedAt = deletedAt;
    }
}
