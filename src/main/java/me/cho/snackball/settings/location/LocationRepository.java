package me.cho.snackball.settings.location;

import me.cho.snackball.settings.SettingsController;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByProvinceAndCity(String province, String city);
}
