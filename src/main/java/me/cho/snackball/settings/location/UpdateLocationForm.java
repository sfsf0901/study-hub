package me.cho.snackball.settings.location;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLocationForm {

    @NotBlank
    private String provinceAndCity;

    public String getProvince(){
        return provinceAndCity.substring(0, provinceAndCity.indexOf(" / "));
    }

    public String getCity(){
        return provinceAndCity.substring(provinceAndCity.indexOf(" / ") + 3);
    }
}
