package me.cho.snackball.location;

import lombok.RequiredArgsConstructor;
import me.cho.snackball.location.domain.Location;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    public List<String> getLocationNames() {
        return locationRepository.findAll().stream()
                .sorted(Comparator.comparing(Location::getProvince).thenComparing(Location::getCity))
                .map(Location::toString)
                .collect(Collectors.toList());
    }
}
