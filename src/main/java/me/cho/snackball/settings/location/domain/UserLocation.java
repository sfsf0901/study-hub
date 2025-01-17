package me.cho.snackball.settings.location.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.cho.snackball.user.domain.User;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private String province;

    @Column(nullable = false)
    private String city;

    @Override
    public String toString() {
        return String.format("%s / %s", province, city);
    }

    //==생성 메서드==//
    public static UserLocation createUserLocation(User user, Location location) {
        UserLocation userLocation = new UserLocation();
        userLocation.setUser(user);
        userLocation.setLocation(location);
        userLocation.setCity(location.getCity());
        userLocation.setProvince(location.getProvince());
        user.getUserLocations().add(userLocation);

        return userLocation;
    }
}
