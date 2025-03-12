package me.cho.snackball.location;

import me.cho.snackball.location.domain.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByProvinceAndCity(String province, String city);
}
