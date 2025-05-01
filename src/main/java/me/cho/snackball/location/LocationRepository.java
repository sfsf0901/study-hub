package me.cho.snackball.location;

import me.cho.snackball.location.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByProvinceAndCity(String province, String city);

    boolean existsByProvinceAndCity(String province, String city);
}
