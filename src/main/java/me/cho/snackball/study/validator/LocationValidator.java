package me.cho.snackball.study.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import me.cho.snackball.location.LocationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationValidator implements ConstraintValidator<ValidLocations, List<String>> {

    private final LocationRepository locationRepository;

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true; // 필수 입력이 아니므로 null 허용
        for (String name : value) {
            if (!name.contains(" / ")) return false;
            String province = name.substring(0, name.indexOf(" / "));
            String city = name.substring(name.indexOf(" / ") + 3);
            boolean exists = locationRepository.existsByProvinceAndCity(province, city);
            if (!exists) return false;
        }
        return true;
    }
}
